# Installation

## Requirements

| | |
|---|---|
| Minecraft | **1.21.1** (Java Edition) |
| Loader | **Fabric Loader 0.16.3 or newer** |
| Java | **21** |
| Fabric API | **not required** |

NovaClient has no mod dependencies. It declares `fabricloader`, `minecraft` and `java` only.

## Install

1. Download `novaclient-0.1.0.jar` from the
   [latest release](https://github.com/kaoakwkwkwkwkwkwkaoakakaakak-collab/Novaclient-official-/releases).
2. Put it in your `.minecraft/mods` folder.
3. Launch the `1.21.1 Fabric` profile.
4. Press **Right Shift** in game to open the GUI.

That is the whole install. No config file to create, no dependency chain.

## Using it with Sodium

Just install both. NovaClient does not replace the renderer, does not use an access widener that
touches Sodium's classes, and does not declare any conflict with it.

The one access widener NovaClient ships opens three private members:

| Target | Why |
|---|---|
| `GameRenderer.loadEffect` | to load the motion blur post chain |
| `GameRenderer.shutdownShaders` | to unload it again |
| `Minecraft.rightClickDelay` | for the Fast Placement module |

None of these are renderer-replacement surfaces, so Sodium is unaffected.

## On Android (Pojav, Turtle and similar launchers)

The client has been run on Android launchers. If it crashes there, the crash report is in
`versions/<profile>/crash-reports/` — see [Troubleshooting](Troubleshooting.md).

## Verifying your download

The release page lists the SHA-256 for the jar. To check yours:

```bash
sha256sum novaclient-0.1.0.jar
```

Compare it against the digest shown on the release asset.

## Uninstall

Delete `novaclient-0.1.0.jar` from `mods/`, and delete `novaclient.json` from
`.minecraft/config/` if you want your saved settings gone too. Nothing else is written.
