import json
import os
import re
import subprocess
from pathlib import Path


VERSION = re.compile(r"\d+\.\d+\.\d+(?:-[0-9A-Za-z.-]+)?")
BRANCH = re.compile(r"\d+\.\d+(?:\.\d+)?")


def sections(text):
    headings = list(re.finditer(r"^## (.+?)\s*$", text, re.MULTILINE))
    result = {}
    for index, heading in enumerate(headings):
        version = heading[1]
        if not VERSION.fullmatch(version):
            continue
        if version in result:
            raise ValueError(f"Duplicate changelog version: {version}")
        end = headings[index + 1].start() if index + 1 < len(headings) else len(text)
        result[version] = text[heading.end():end].strip()
    return result


def detect_release(branch, properties, current, previous):
    if not BRANCH.fullmatch(branch) or branch != properties["minecraft_version"]:
        return None
    entries = sections(current)
    if not entries:
        return None
    version = next(iter(entries))
    if version in sections(previous):
        return None
    if version != properties["version"]:
        raise ValueError("The newest CHANGES.md version must match version in gradle.properties.")
    if not entries[version]:
        raise ValueError("The new changelog version needs release notes.")
    return {
        "tag": f"v{version}",
        "title": f"{properties['mod_name']} {version} (Minecraft {branch})",
        "prerelease": str("-" in version).lower(),
        "notes": entries[version],
    }


def previous_changelog(before):
    if not re.fullmatch(r"[0-9a-f]{40}", before):
        raise ValueError("Missing or invalid push baseline commit.")
    if before == "0" * 40:
        return ""
    if subprocess.run(["git", "cat-file", "-e", f"{before}^{{commit}}"], capture_output=True).returncode:
        subprocess.run(["git", "fetch", "--no-tags", "origin", before], check=True)
    paths = subprocess.check_output(["git", "ls-tree", "--name-only", before, "--", "CHANGES.md"], text=True)
    if not paths.strip():
        return ""
    return subprocess.check_output(["git", "show", f"{before}:CHANGES.md"], encoding="utf-8")


def main():
    release = None
    ref = os.environ["GITHUB_REF"]
    if os.environ["GITHUB_EVENT_NAME"] == "push" and ref.startswith("refs/heads/"):
        branch = ref.removeprefix("refs/heads/")
        properties = dict(re.findall(r"^([a-z_]+)[ \t]*=[ \t]*([^\r\n]*)",
            Path("gradle.properties").read_text(encoding="utf-8"), re.MULTILINE))
        properties = {key: value.strip() for key, value in properties.items()}
        if BRANCH.fullmatch(branch) and branch == properties["minecraft_version"] and Path("CHANGES.md").exists():
            event = json.loads(Path(os.environ["GITHUB_EVENT_PATH"]).read_text(encoding="utf-8"))
            if not event.get("deleted", False):
                release = detect_release(branch, properties, Path("CHANGES.md").read_text(encoding="utf-8"),
                    previous_changelog(event["before"]))

    with Path(os.environ["GITHUB_OUTPUT"]).open("a", encoding="utf-8") as output:
        output.write(f"publish={str(release is not None).lower()}\n")
        if release:
            Path(os.environ["RUNNER_TEMP"], "release-notes.md").write_text(release.pop("notes") + "\n", encoding="utf-8")
            for key, value in release.items():
                output.write(f"{key}={value}\n")
    print(f"Release: {release['tag']}" if release else "No new release entry on a matching Minecraft version branch.")


if __name__ == "__main__":
    main()
