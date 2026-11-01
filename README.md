# Create: Visible Clipboard

A client-side Create add-on for **Minecraft 1.21.1 / NeoForge**. Read the actual
contents on placed and held clipboards, check tasks, and turn pages directly on
placed clipboards.

## Installation

Install the NeoForge JAR from `neoforge/build/libs/` on the client alongside
Create **6.0.10** and its required dependencies. The server only needs Create.
Players without this add-on can still use the same clipboard through Create's editor.

The initial dependency target is NeoForge **21.1.219** and Create **6.0.10-280**.

## Controls

| Action | Result |
| --- | --- |
| Look at a placed clipboard | Read its currently selected page |
| Right-click a checkbox | Toggle that task |
| Right-click `<` or `>` at the bottom | Move between existing pages |
| Right-click a placed clipboard while holding another clipboard | Copy its entries into the held clipboard using Create's normal behavior |
| Shift-right-click a placed clipboard | Open Create's original editor |
| Left-click a placed clipboard | Create's normal pickup behavior |
| Hold a clipboard | See its saved page on a 3D clipboard model |
| Use a held clipboard | Create's original behavior, including its editor and placement |

Holding another clipboard gives Create's copy action priority on normal right-click.
Otherwise, a placed clipboard's surface controls take priority over held-item use;
right-clicking elsewhere on its surface does nothing. Shift-right-click opens the
editor even while holding a clipboard. Add/edit text and create pages in the original
editor. Read-only clipboards retain Create's behavior: text is locked, but checkboxes
can still be toggled.

The selected page is saved and shared through Create's existing clipboard component.
This add-on uses Create's full-content update packet, so simultaneous editors can
overwrite each other's changes. There is no new server-side protocol or saved-data format.

## Development

Use **JDK 21** for Gradle and your IDE. The Gradle 8.10 wrapper cannot run on JDK 25.

```powershell
$env:JAVA_HOME = 'C:\path\to\jdk-21'
.\gradlew.bat build
.\gradlew.bat :neoforge:runClient
```

On Linux/macOS, use `./gradlew` instead. `build` runs the common layout/transform
tests and NeoForge clipboard-edit tests, and creates the distributable NeoForge JAR.
The `common` JAR is not a standalone mod.

- `common/`: loader-independent page geometry, layout rules, and resources.
- `neoforge/`: Create data access, rendering, interaction, and a client item-render mixin.
- `fabric/`: disabled template for a future port; see [fabric/README.md](fabric/README.md).

Fabric is not currently supported: Create Fabric has no published 1.21.1 release
at scaffolding time. The disabled module does not produce an advertised Fabric build.

Project/repository ID: `create-visible-clipboard`. NeoForge mod ID:
`create_visible_clipboard` (NeoForge forbids hyphens). Java uses 4-space indentation
and Create-style names, imports, and control flow.

## Automatic releases

GitHub Actions builds and tests pushes and pull requests. A successful push also
publishes the NeoForge JAR to **GitHub Releases** when:

- The branch is a Minecraft version such as `1.21.1`, matching `minecraft_version`
  in `gradle.properties`.
- The newest version heading in `CHANGES.md` was absent before that push.
- That heading matches `version` in `gradle.properties` and has release notes.

To release a new version, update `version` and prepend an entry like this, then
commit and push to the matching Minecraft branch:

```markdown
## 0.1.1

- Describe the changes in this release.
```

The tag is `v0.1.1`; the entry's body becomes the release notes. Versions
such as `0.2.0-beta.1` are published as prereleases. Editing an existing entry does
not publish again. When a push adds several entries, only the newest version is
published. The initial `0.1.0` entry is eligible on its first push too.

Publication uses GitHub's built-in `GITHUB_TOKEN` with `contents: write` in the
release job. It uploads the tested JAR to a draft before making the release public.
Retries can finish an incomplete draft; an already published release is skipped.
An existing tag pointing at another commit fails instead of being overwritten.
Tags are repository-wide: use distinct mod versions for releases from different
Minecraft branches.

Run the release-detection checks locally with:

```powershell
python -B -m unittest discover -s .github/scripts -p 'test_*.py'
```

## In-game verification

Verified locally: a Gradle build, nine automated layout/geometry/data tests, and
client startup with Create 6.0.10 on NeoForge 21.1.219. The startup smoke test was
time-limited; it does not verify gameplay interactions.

The geometry and data tests cannot verify GPU rendering or live multiplayer input.
Before releasing, check:

- Wall/floor/ceiling clipboards in all four directions, including waterlogged ones.
- Empty, single-page, and multi-page content; wrapped and multilingual text; item icons.
- Checkboxes and arrows align with the crosshair; holding Use only activates once.
- Shift-right-click opens the original GUI without removing the block.
- Main/offhand rendering, left-handed player settings, and first/third-person views.
- Placement, pickup, inventory icons, native editing, and resource reload.
- Copy into a held clipboard with existing entries; verify the source stays intact and repeated copies do not duplicate entries.
- Two clients on a Create-only server; updates, reconnects, chunk reloads, and concurrent editing.

## Template and license

Scaffolded from [Jared's MultiLoader Template](https://github.com/jaredlll08/MultiLoader-Template/tree/1.21.1),
revision `7bd74e027936c3ecefb38a518c5d7ac71476d543`. Licensed under **CC0-1.0**.
Models reference Create's existing assets; Create textures are not redistributed.
