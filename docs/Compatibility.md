# Compatibility

## Sodium

NovaClient is designed to run alongside Sodium. It does not replace the renderer.

What it touches, and why none of it conflicts:

| Touch point | Purpose |
|---|---|
| `Gui.render` (TAIL inject) | draw HUD widgets |
| `LevelRenderer.renderSky` | Hide Sky |
| `FogRenderer.setupFog` | fog control |
| `ScreenEffectRenderer` | low fire, water overlay |
| `EntityRenderDispatcher.shouldRender` | entity culling |
| `ItemEntityRenderer.render` | item physics |
| `ItemInHandRenderer.renderArmWithItem` | hand FOV |
| `Gui.renderPortalOverlay` / `renderCameraOverlays` | overlay removal |

These are HUD and entity-facing hooks. Sodium replaces chunk building and terrain rendering, which
is a different set of methods.

**Resolution Scale is the reason this stays narrow.** Implementing it properly means hooking
`RenderTarget`, `Window`, `PostChain` and the GL command encoder — the exact layer Sodium owns.
The reference mod for it (RenderScale, MIT, 1.67M downloads) needs eight mixins plus dedicated
`compat/sodium/` and `compat/iris/` packages to avoid breaking those mods. A half version would
break Sodium, which is the opposite of what this client is for.

## Iris / shader packs

Motion Blur uses vanilla's `PostChain`. Iris takes over the post pipeline when a shader pack is
active, so the two may fight. If Motion Blur misbehaves with a shader pack on, turn one of them
off.

## Fabric API

Not required. NovaClient declares only `fabricloader`, `minecraft` and `java`.

## Mixin verification

Mixin bugs of the wrong shape compile fine and then crash the game at startup. To stop that class
of bug shipping, the repository includes `verify-mixins.sh`, which javaps every `@Inject` and
`@Redirect` target and fails if a handler's static-ness disagrees with its target.

```
injections checked: 19   problems: 0   missing files: 0
```

Run it before any build you intend to distribute. It needs the Minecraft jar in the Loom cache, so
run `bash build-bootstrap.sh` once first.

## Removed on purpose

Two modules were deleted rather than shipped as toggles that silently do nothing.

**Fast Math.** There is no API for cheaper trigonometry in Minecraft's hot paths without a coremod
that rewrites bytecode. A toggle for it would have been decoration.

**Resolution Scale.** See the Sodium section above.

Both removals reduced the module count — from 73 to 71 — rather than inflating it with stubs.

## Known limitations

- **No Pumpkin Overlay** also removes the spyglass overlay. Both are drawn by the same method.
- **Motion Blur** may conflict with Iris shader packs.
- **Nothing has been benchmarked.** No frame-time measurements exist for any module.

## Mod list it was tested against

One user ran it alongside Fabric API, Sodium-family mods (`lithium`, `immediatelyfast`,
`entityculling`), `modmenu`, `placeholder-api` and `trender` on an Android launcher. The startup
crash from that session was fixed; see [Troubleshooting](Troubleshooting.md).
