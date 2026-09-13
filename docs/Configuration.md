# Configuration

## The config file

Settings are saved to `.minecraft/config/novaclient.json`.

It is written when you close the GUI, and read when the client starts. You can edit it by hand
with the game closed, but the GUI is easier.

## What is stored

Per module:

| Field | Meaning |
|---|---|
| `enabled` | whether the module is on at startup |
| `key` | bound key, or `-1` for none |
| `settings` | each setting's value, keyed by setting name |

Sliders store their numeric value. Toggles store `true`/`false`. Modes store the mode string.

## Presets

Four one-click presets are built in. Applying one changes many modules at once.

| Preset | Aimed at |
|---|---|
| **Performance** | maximum FPS — aggressive culling and distance cuts |
| **Visual** | looks — blur, overlays off, cosmetic modules on |
| **Competitive** | clarity for PvP — HUD on, distractions off |
| **Default** | a clean baseline |

Presets are applied through the Config Profiles module in the Misc tab.

## Restoring your own settings

Every module that changes a vanilla option stores the value it found and puts it back when you
switch the module off. So turning NovaClient off mid-session should leave your video settings
where they were.

If a module is left on when you quit, its override is not restored — Minecraft saves whatever value
was active. Turning modules off before quitting avoids this.

## Resetting

Delete `novaclient.json`. The next launch starts from defaults.

## Where nothing else is written

NovaClient writes that one file and nothing else. No telemetry, no network calls, no logs of its
own beyond what Minecraft already produces.
