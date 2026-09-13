# Modules

NovaClient ships **71 modules** across six tabs. Open the GUI with **Right Shift**.

Every entry below is generated from the module source, so the names, descriptions and
settings match exactly what the client registers.


## Performance

14 modules.

| Module | What it does | Settings |
|---|---|---|
| **Frame Limiter** | Caps the frame rate so the GPU stops running flat out. | Max FPS (slider 10–260, step 5) |
| **Idle Frame Limit** | Drops the frame rate while you are not moving. | Idle FPS (slider 5–120, step 5) |
| **Entity Culling** | Skips drawing entities that are not on screen. | Distance (slider 8–256, step 8)<br>Cull Behind Walls (toggle, default on) |
| **Particle Limiter** | Caps how many particles can exist at once. | Budget (slider 0–4000, step 50) |
| **Chunk Culling** | Skips chunk sections hidden behind other geometry. | — |
| **Render Distance** | Overrides the render distance without touching your options menu. | Chunks (slider 2–32, step 1) |
| **Lazy Chunk Loading** | Spreads chunk builds over more frames to reduce stutter. | Budget (slider 1.0–20.0, step 0.5) |
| **Disable Cinematic Camera** | Turns off the smooth camera, which costs frames. | — |
| **No Weather** | Stops drawing rain and snow. | — |
| **No Clouds** | Stops drawing clouds. | — |
| **No Fog** | Removes distance fog. | Mode (Off/Reduced) |
| **Biome Blend** | Lowers biome blending, which is expensive on chunk build. | Radius (slider 0–7, step 1) |
| **No Entity Shadows** | Stops drawing the shadow disc under entities. | — |
| **Memory Guard** | Releases cached buffers before the heap runs out. | Trigger (slider 50–95, step 1)<br>On World Change (toggle, default on) |

## Render

15 modules.

| Module | What it does | Settings |
|---|---|---|
| **Fog Control** | Adjusts or removes distance fog. | Mode (Off/Reduced/Vanilla) |
| **Clouds** | Chooses how clouds are drawn. | Quality (Off/Fast/Fancy) |
| **Hide Sky** | Hides the skybox, sun, moon and stars. | — |
| **Weather** | Controls rain and snow rendering. | Mode (Clear/Reduced/Vanilla) |
| **Entity Shadows** | Draws shadows under entities. | — |
| **Particles** | Chooses the particle quality. | Quality (All/Decreased/Minimal) |
| **Graphics Mode** | Switches fast and fancy. | Mode (Fast/Fancy/Fabulous) |
| **Smooth Lighting** | Chooses the lighting quality. | Mode (Off/Minimum/Maximum) |
| **View Distance** | Overrides the render distance. | Chunks (slider 2–32, step 1) |
| **Simulation Distance** | Overrides how far the world ticks. | Chunks (slider 2–32, step 1) |
| **View Bobbing** | Toggles the walking sway. | — |
| **Vignette** | Toggles the dark screen edges. | — |
| **Distortion Effects** | Toggles the nausea and portal wobble. | Amount (slider 0–100, step 5) |
| **Damage Tilt** | Toggles the hit shake. | — |
| **Dark Screen** | Toggles the darkness overlay. | — |

## Visual

14 modules.

| Module | What it does | Settings |
|---|---|---|
| **Motion Blur** | Blends previous frames so fast movement looks smooth. | Amount (slider 5–90, step 5)<br>In Menus Only (toggle, default off) |
| **Full Bright** | Lights every block as if it were daylight. | — |
| **Name Tags** | Redraws player name plates. | Range (slider 8–128, step 4)<br>Show Health (toggle, default on)<br>Show Ping (toggle, default off)<br>Background (toggle, default on) |
| **Item Physics** | Makes dropped items lie flat on the ground. | Rotation (slider 0–90, step 5) |
| **Hit Boxes** | Draws entity bounding boxes. | Show Eyes (toggle, default on) |
| **Chunk Borders** | Draws the section outlines. | — |
| **Low Fire** | Lowers the fire overlay so you can see. | Height (slider 10–100, step 5) |
| **No Hurt Cam** | Removes the red flash when you take damage. | — |
| **No Pumpkin Overlay** | Removes the pumpkin blur. | — |
| **No Water Overlay** | Removes the blue water tint. | — |
| **No Portal Overlay** | Removes the purple portal swirl. | — |
| **Time Changer** | Draws the world at a fixed time of day. | Hour (slider 0–23, step 1) |
| **Hand FOV** | Scales how large the held item appears. | Scale (slider 50–200, step 5) |
| **Chroma** | Cycles the accent colour. | Speed (slider 5–200, step 5)<br>Saturation (slider 10–100, step 5) |

## HUD

13 modules.

| Module | What it does | Settings |
|---|---|---|
| **FPS Counter** | Draws the current frame rate. | — |
| **CPS Counter** | Draws your clicks per second. | — |
| **Ping** | Draws your latency to the server. | — |
| **Direction** | Draws the compass direction you face. | — |
| **Biome** | Draws the biome you are standing in. | — |
| **Armor Status** | Draws your armour and its durability. | — |
| **Potion Status** | Draws active effects and time left. | — |
| **Clock** | Draws the world time. | — |
| **Memory** | Draws heap usage. | — |
| **Server IP** | Draws the address you are connected to. | — |
| **Coordinates** | Draws your X, Y and Z. | Position (TopLeft/TopRight/BottomLeft/BottomRight)<br>Scale (slider 0.5–2.0, step 0.1)<br>Decimals (toggle, default off)<br>Show Facing (toggle, default on)<br>Shadow (toggle, default on) |
| **Keystrokes** | Draws the keys you are pressing. | Position (TopLeft/TopRight/BottomLeft/BottomRight)<br>Scale (slider 0.5–2.0, step 0.1)<br>Show Mouse (toggle, default on)<br>Show CPS (toggle, default on)<br>Style (Rounded/Flat/Outline) |
| **Module List** | Draws the active modules on screen. | Position (TopLeft/TopRight/BottomLeft/BottomRight)<br>Scale (slider 0.5–2.0, step 0.1)<br>Sort (Length/Alphabetical/Category)<br>Show Keybinds (toggle, default off)<br>Shadow (toggle, default on) |

## Player

8 modules.

| Module | What it does | Settings |
|---|---|---|
| **Auto Sprint** | Keeps you sprinting. | Only Moving Forward (toggle, default on) |
| **Sneak Toggle** | Makes sneak a toggle. | Mode (Toggle/Hold) |
| **No Slowdown** | Keeps full speed while eating or blocking. | — |
| **Auto Tool** | Switches to the best tool for the block. | — |
| **Perspective Hold** | Keeps the camera behind you in third person. | Reset On Release (toggle, default on) |
| **Fast Placement** | Removes the delay between placing blocks. | Delay (slider 0–4, step 1) |
| **Reach Display** | Shows your block reach distance. | — |
| **Inventory Move** | Lets you walk while an inventory is open. | — |

## Misc

7 modules.

| Module | What it does | Settings |
|---|---|---|
| **Watermark** | Draws the client name in the corner. | Position (TopLeft/TopRight/BottomLeft/BottomRight)<br>Show Version (toggle, default on) |
| **Notifications** | Shows a toast when a module is toggled. | Duration (slider 0.5–8.0, step 0.5) |
| **Rich Presence** | Publishes your game state to Discord. | Show Server (toggle, default off)<br>Show Health (toggle, default off) |
| **Screenshot Manager** | Saves screenshots without the chat message. | Copy To Clipboard (toggle, default on) |
| **Config Profiles** | Switches between saved module sets. | Profile (Default/Performance/Visual/Competitive) |
| **Anti AFK** | Keeps you from being kicked for idling. | Interval (slider 10–300, step 10) |
| **Debug Info** | Shows renderer and timing details. | Frame Times (toggle, default on)<br>Draw Calls (toggle, default off) |

## Notes on specific modules

**Motion Blur** is a genuine post-processing effect, not a toggle on a flag.
See [Motion Blur](Motion-Blur.md) for how it works.

**Hide Sky** cancels `LevelRenderer.renderSky` entirely, which removes the skybox, sun, moon
and stars together. An earlier build advertised a brightness slider that could not dim
anything, so the module was reduced to an honest on/off.

**No Pumpkin Overlay** also suppresses the spyglass overlay. Both are drawn by the same
`Gui.renderCameraOverlays` method, and there is no way to skip one without reimplementing it.

**Screenshot Manager** silences the "Saved screenshot as..." chat message and pushes a HUD
notification in its place.

## Removed on purpose

Two modules were deleted rather than shipped as toggles that do nothing:

- **Fast Math** — there is no API for cheaper trigonometry without a coremod.
- **Resolution Scale** — requires renderer hooks that collide with Sodium. The reference
  implementation (RenderScale) needs eight mixins plus dedicated Sodium and Iris
  compatibility layers.
