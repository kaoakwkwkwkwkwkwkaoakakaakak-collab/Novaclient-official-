# GUI and Keybinds

## Opening the GUI

Press **Right Shift** (GLFW key 347). It works in game and in most menus.

The GUI does not pause the game.

## Layout

```
+------------------------------------------------------+
|  NovaClient 0.1.0                        12 active   |
+------------+-----------------------------------------+
|            |                                         |
| Performance|  Frame Limiter              [-] [==]    |
| Render     |  Idle Frame Limit           [-] [  ]    |
| Visual     |  Entity Culling             [+] [==]    |
| HUD        |    Distance              64             |
| Player     |    Cull Behind Walls              on    |
| Misc       |                                         |
+------------+-----------------------------------------+
|  Skips drawing entities that are not on screen.      |
+------------------------------------------------------+
```

**Header** — client name and version on the left, the number of currently enabled modules on the
right. An accent underline sits below it.

**Left column** — the six tabs. Each shows how many of its modules are currently enabled, so you
can see at a glance which tab you changed.

**Right column** — the modules in the selected tab. Scroll to see more.

**Footer** — the description of whatever module the cursor is over. When nothing is hovered it
shows the close hint.

## Using it

| Action | Effect |
|---|---|
| Click a tab | switch category |
| Click a module row | toggle it on or off |
| Click `+` on a module | expand its settings |
| Drag a slider | change its value |
| Click a mode value | cycle to the next option |
| **Right Shift** | close |

A module with settings shows `+` or `-` on the right of its row. Modules without settings have no
arrow — there is nothing to expand.

Enabled modules get a thin accent bar on the left edge of their row, and their toggle switch fills
with the accent colour.

## Chroma

If the **Chroma** module is on, every accent-coloured element in the GUI and the HUD cycles through
the spectrum. That includes the tab highlight, the enabled-row bars, the toggle switches, the
slider fills and the header underline.

## Notifications

If **Notifications** is on, toggling a module pushes a short HUD notification saying what changed.
The same channel is used by Screenshot Manager.

## Search

The GUI filters modules by name, description and category as you type. Useful once you have 71
modules to scroll through.

## Keybinds

Modules can be bound to keys. The Right Shift GUI toggle is fixed and cannot be rebound — it is
polled directly rather than going through Minecraft's key mapping system, so it keeps working even
if you rebind everything else.
