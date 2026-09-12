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

## Modules

73 modules across 6 tabs: **Performance**, **Render**, **Visual**, **HUD**, **Player**, **Misc**.

Includes Motion Blur, FPS and CPS counters, Keystrokes, Coordinates, Module List, Frame Limiter,
Entity Culling, Particle Limiter, Chunk Culling, Full Bright, Time Changer, Name Tags, Auto Sprint
and Config Profiles.

## What actually works

72 modules across 6 tabs (Performance 14, Render 15, Visual 15, HUD 13, Player 8, Misc 7),
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

**A few modules are still interface-only** and need a renderer mixin that is not written yet:
Item Physics, Custom Sky, Chroma, No Slowdown, Screenshot Manager, Resolution Scale and the
pumpkin/portal overlay removers. They toggle and save but change nothing in game.

Fast Math was removed rather than shipped as a stub: there is no API for it without a coremod,
so the toggle could never have done anything.

Nothing here has been measured on a device or in a real client session.

## Licence

**CC BY-NC-ND 4.0** — full text in [LICENSE](LICENSE).

You may download and use this client. You may not copy, fork, modify, reupload, sell, or present it
as your own work.
