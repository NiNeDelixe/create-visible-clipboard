import json
import os
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

from release import detect_release


class ReleaseTest(unittest.TestCase):
    properties = {"version": "0.1.0", "minecraft_version": "1.21.1", "mod_name": "Create: Visible Clipboard"}

    def test_new_version_uses_only_its_notes(self):
        current = "# Changes\n\n## Unreleased\nFuture work.\n\n## 0.1.0\n- New feature.\n\n## 0.0.1\n- Old feature.\n"
        release = detect_release("1.21.1", self.properties, current, "## 0.0.1\n- Old feature.\n")
        self.assertEqual("v0.1.0", release["tag"])
        self.assertEqual("- New feature.", release["notes"])
        self.assertEqual("false", release["prerelease"])

    def test_existing_version_edits_and_other_branches_do_not_release(self):
        for branch, previous in [
            ("main", ""), ("feature/1.21.1", ""), ("1.20.1", ""),
            ("1.21.1", "## 0.1.0\nOriginal notes.\n"),
        ]:
            with self.subTest(branch=branch, previous=previous):
                self.assertIsNone(detect_release(branch, self.properties, "## 0.1.0\nEdited notes.\n", previous))

    def test_multiple_new_entries_release_only_the_current_version(self):
        release = detect_release("1.21.1", self.properties,
            "## 0.1.0\nNewest.\n\n## 0.0.2\nOlder.\n\n## 0.0.1\nOldest.\n", "## 0.0.1\nOldest.\n")
        self.assertEqual("Newest.", release["notes"])

    def test_bad_release_entries_fail_instead_of_publishing(self):
        for current in ["## 0.2.0\nMismatch.\n", "## 0.1.0\n", "## 0.1.0\nFirst.\n## 0.1.0\nDuplicate.\n"]:
            with self.subTest(current=current), self.assertRaises(ValueError):
                detect_release("1.21.1", self.properties, current, "")

    def test_prerelease_is_marked(self):
        release = detect_release("1.21.1", self.properties | {"version": "0.2.0-beta.1"},
            "## 0.2.0-beta.1\nPreview.\n", "")
        self.assertEqual("true", release["prerelease"])

    def test_cli_compares_the_entire_push_and_ignores_pull_requests_and_tags(self):
        script = Path(__file__).with_name("release.py").resolve()
        with tempfile.TemporaryDirectory(dir=os.environ.get("RUNNER_TEMP")) as directory:
            root = Path(directory)

            def git(*args):
                return subprocess.check_output(["git", "-c", "user.name=Test", "-c", "user.email=test@example.invalid",
                    *args], cwd=root, text=True).strip()

            git("init", "--quiet")
            (root / "gradle.properties").write_text("\n".join(f"{key}={value}" for key, value in self.properties.items()))
            git("add", ".")
            git("commit", "--quiet", "-m", "Before changelog")
            before = git("rev-parse", "HEAD")
            (root / "CHANGES.md").write_text("## 0.1.0\n- Initial release.\n")
            git("add", "CHANGES.md")
            git("commit", "--quiet", "-m", "Add release entry")
            entry_commit = git("rev-parse", "HEAD")
            (root / "README.md").write_text("A later commit in the same push.\n")
            git("add", "README.md")
            git("commit", "--quiet", "-m", "Update docs")

            for event, ref, baseline, deleted, publish in [
                ("push", "refs/heads/1.21.1", before, False, True),
                ("push", "refs/heads/1.21.1", entry_commit, False, False),
                ("push", "refs/heads/1.21.1", "0" * 40, False, True),
                ("push", "refs/heads/main", before, False, False),
                ("pull_request", "refs/pull/1/merge", before, False, False),
                ("push", "refs/tags/v0.1.0", before, False, False),
                ("push", "refs/heads/1.21.1", before, True, False),
            ]:
                with self.subTest(event=event, ref=ref, baseline=baseline, deleted=deleted):
                    output = root / "output"
                    output.write_text("")
                    (root / "event.json").write_text(json.dumps({"before": baseline, "deleted": deleted}))
                    env = os.environ | {
                        "GITHUB_EVENT_NAME": event, "GITHUB_REF": ref,
                        "GITHUB_EVENT_PATH": str(root / "event.json"), "GITHUB_OUTPUT": str(output),
                        "RUNNER_TEMP": str(root),
                    }
                    subprocess.run([sys.executable, "-B", str(script)], cwd=root, env=env,
                        check=True, capture_output=True, text=True)
                    self.assertIn(f"publish={str(publish).lower()}\n", output.read_text())
                    if publish:
                        self.assertEqual("- Initial release.\n", (root / "release-notes.md").read_text())


if __name__ == "__main__":
    unittest.main()
