# PojavXOptimizer

made for pojavers by pojaver

PojavXOptimizer is a performance mod for Minecraft 1.21.1 on Fabric, built only for Pojav Launcher on phones. It draws fewer of the things you cannot see, packs chunk geometry into half the memory so a 2 GB or 3 GB phone does not run out of room, and stops the game burning battery at full speed while you stand still or sit in a menu. When your phone gets hot it lowers the render distance for a few seconds instead of stuttering and recovering over and over. It supports the **MobileGlues** and **Krypton (NG-GL4ES)** renderers, plus LTW and HolyGL4ES. It needs nothing but Fabric Loader — drop it in and it tunes itself for your phone.

## Install

1. Install Fabric Loader for Minecraft 1.21.1 in Pojav Launcher
2. Put `pojavxoptimizer-1.0.3.jar` in `.minecraft/mods`
3. Launch

| | |
|---|---|
| **Minecraft** | 1.21.1 |
| **Loader** | Fabric 0.16.3 or newer |
| **Needs** | Nothing. No Fabric API. |
| **Java** | 21 |
| **Side** | Client only |

## Conflicts

This mod replaces parts of the renderer. It cannot run alongside another renderer-replacing mod — the loader will refuse to start and ask you to remove one. Everything else is fine.

## What it does

**Active now**

- Skips drawing entities that are not on screen.
- Caps how many particles spawn, so rain and mob farms do not tank the frame rate.
- Caps the frame rate when nothing is moving, which is where most of the battery saving comes from.
- Drops render distance for a few seconds when the phone gets hot, instead of letting it throttle.
- Frees chunk buffers when memory runs low.

**Built and unit-tested, not switched on yet**

- A 16-byte packed vertex format that halves chunk mesh memory. It is off by default because the shader that reads it has not been run on real hardware. Turning it on early means a black screen, so it stays off until it has been tested on a phone.
- A branchless frustum test for chunk sections, checked against a brute-force reference on 5000 random boxes.

**Not done**

No in-game testing has happened. There is no Android device in the build environment, so every number in here is designed rather than measured.

## Licence

**CC BY-NC-ND 4.0** — full text in [LICENSE](LICENSE).

You may download and use this mod. You may not copy it, fork it, modify it, reupload it, sell it, or present it as your own work. Credit `goodboysoul` if you link to it.
