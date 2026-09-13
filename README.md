# NovaClient

A lightweight client for Minecraft 1.21.1 on Fabric. Built to raise frame rates and add quality-of-life features without replacing the renderer, so it runs alongside Sodium instead of fighting it.

Press **Right Shift** to open the menu.

## Install

1. Fabric Loader for 1.21.1
2. Put `novaclient-0.1.0.jar` in `.minecraft/mods`
3. Launch and press Right Shift

| | |
|---|---|
| **Minecraft** | 1.21.1 |
| **Loader** | Fabric 0.16.3+ |
| **Needs** | Nothing. No Fabric API. |
| **Works with** | Sodium, Lithium and other performance mods |

## Documentation

Full documentation lives in [`docs/`](docs/README.md):

[Install](docs/Installation.md) · [Modules](docs/Modules.md) · [GUI](docs/GUI-and-Keybinds.md) ·
[Config](docs/Configuration.md) · [Motion Blur](docs/Motion-Blur.md) ·
[Compatibility](docs/Compatibility.md) · [Troubleshooting](docs/Troubleshooting.md) ·
[Building](docs/Building-from-source.md)

## Modules

73 modules across 6 tabs: **Performance**, **Render**, **Visual**, **HUD**, **Player**, **Misc**.

Includes Motion Blur, FPS and CPS counters, Keystrokes, Coordinates, Module List, Frame Limiter,
Entity Culling, Particle Limiter, Chunk Culling, Full Bright, Time Changer, Name Tags, Auto Sprint
and Config Profiles.

## What actually works

71 modules across 6 tabs (Performance 14, Render 15, Visual 14, HUD 13, Player 8, Misc 7),
52 of them with adjustable settings. Open with **Right Shift**, config saves to `novaclient.json`.

**Motion Blur is real.** It ships a vanilla post-processing chain
(`assets/novaclient/shaders/post/nova_blur.json` plus a fragment shader) that blends each frame with
the previous one, driven through `GameRenderer.loadEffect` and `PostChain.setUniform`. An access
widener opens the two private `GameRenderer` methods it needs. The Amount slider maps to the
shader's `Retention` uniform live.

Other modules write real vanilla state: frame limiter, render/view/simulation distance, particles,
graphics mode, clouds, biome blend, smooth lighting, entity shadows, view bobbing, vignette,
distortion, damage tilt, full bright, name tags, hit boxes, chunk borders, hurt cam, lazy chunk
loading, chunk culling, particle limiter, weather, time of day, debug info, sneak toggle,
fast placement, reach display, watermark and notifications.

14 HUD widgets draw through a mixin into `Gui.render`.

**Every module now does something, or has been removed. **Fast Math** and **Resolution Scale** were
deleted rather than shipped as stubs: Fast Math has no API without a coremod, and Resolution Scale
needs renderer hooks that collide with Sodium — the reference mod (RenderScale) requires eight
mixins plus dedicated Sodium and Iris compatibility layers to avoid breaking those mods.

Nothing here has been measured on a device or in a real client session.

## Licence

**CC BY-NC-ND 4.0** — full text in [LICENSE](LICENSE).

You may download and use this client. You may not copy, fork, modify, reupload, sell, or present it
as your own work.
