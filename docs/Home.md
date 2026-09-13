# NovaClient

A lightweight FPS client for **Minecraft 1.21.1 on Fabric**.

NovaClient does not replace Minecraft's renderer. It sits alongside **Sodium** and adjusts real
game settings, draws HUD widgets, and hooks a small number of renderer methods. If Sodium is
working, NovaClient should not change that.

**71 modules** across six tabs, opened with **Right Shift**.

---

## Quick links

| | |
|---|---|
| [Installation](Installation.md) | Getting it running, and what to install it with |
| [Modules](Modules.md) | Every module, what it does, and its settings |
| [GUI and Keybinds](GUI-and-Keybinds.md) | Using the click GUI |
| [Configuration](Configuration.md) | `novaclient.json` and the built-in presets |
| [Motion Blur](Motion-Blur.md) | How the post-processing effect works |
| [Compatibility](Compatibility.md) | Sodium, other mods, and what was left out |
| [Troubleshooting](Troubleshooting.md) | Crashes, logs, and how to report them |
| [Building from source](Building-from-source.md) | Compiling it yourself |

---

## What it actually does

Three kinds of thing:

**Writes real vanilla settings.** Frame limiter, render/view/simulation distance, particles,
graphics mode, clouds, biome blend, smooth lighting, entity shadows, view bobbing, vignette,
distortion, damage tilt, full bright, name tags, and more. Each one stores your original value and
restores it when you switch the module off.

**Draws HUD widgets.** 14 of them — FPS, CPS, ping, coordinates, direction, biome, clock, memory,
server IP, keystrokes, module list, armor, potions and reach — rendered through a mixin into
`Gui.render`.

**Hooks the renderer.** Entity culling, fog, low fire, hand FOV, item physics, the water, pumpkin
and portal overlays, hide sky, hit boxes, chunk borders, inventory move, auto tool and motion blur.

## What it does not do

No PvP cheats. No x-ray, kill aura, fly, reach or velocity. This is an FPS and utility client.

Two modules were removed rather than shipped as switches that do nothing — see
[Compatibility](Compatibility.md#removed-on-purpose).

## Licence

**CC BY-NC-ND 4.0.** You may download and use this client. You may not copy, fork, modify,
reupload, sell, or present it as your own work. Full text in the `LICENSE` file in the repository.

## Honest status

Nothing here has been benchmarked on a device. The mixin layer is verified against the real
Minecraft class files (`verify-mixins.sh`, 19 injections checked), and one startup crash was
reported by a user and fixed — but visual quality has not been confirmed on real hardware.
