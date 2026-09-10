# PojavXOptimizer

**made for pojavers by pojaver**

PojavXOptimizer is a performance mod for Minecraft 1.21.1 on Fabric, made only for Pojav Launcher
on phones. It draws only the blocks you can actually see, packs them into far less memory so a 2 GB
or 3 GB phone does not run out of room, and stops the game burning battery at full speed while you
stand still or sit in a menu. When your phone gets hot it lowers the render distance for a few
seconds instead of stuttering and recovering over and over. It supports the **MobileGlues** and
**Krypton (NG-GL4ES)** renderers, plus LTW and HolyGL4ES. It needs nothing but Fabric Loader — drop
it in and it tunes itself for your phone. It is **not** compatible with Sodium; if both are in your
`mods` folder the game will refuse to start and ask you to remove one.

---

## Download

**`pojavxoptimizer-1.0.0.jar`** — [Releases](https://github.com/kaoakwkwkwkwkwkwkaoakakaakak-collab/PojavXOptimizer/releases)

1. Install **Fabric Loader 1.21.1** in Pojav Launcher.
2. Put the jar in your `.minecraft/mods` folder.
3. Launch. It configures itself.

| | |
|---|---|
| **Minecraft** | 1.21.1 |
| **Loader** | Fabric only |
| **Java** | 21 |
| **Renderers** | MobileGlues, Krypton (NG-GL4ES), LTW, HolyGL4ES |
| **Needs** | Nothing. No Fabric API, no Sodium. |
| **Not compatible** | Sodium |
| **License** | MIT |

---

## What it does

| Module | Effect |
|---|---|
| **Frame pacing** | Caps fps when idle or in a menu. Biggest battery win on the list. |
| **Thermal governor** | Infers throttling from sustained frame-time drift and backs off, recovering slowly so it does not oscillate. |
| **Visibility budget** | Caps the drawn section count, shrinking fast when frames are slow and growing back slowly. |
| **Dynamic resolution** | Renders at 0.6–1.0x scale, driven by smoothed frame time. |
| **Entity culling** | Skips entities that are too far away or too small on screen. Never culls your mount. |
| **Particle budget** | Hard population cap; refuses new spawns rather than deleting live ones. |
| **Heap guard** | Watches heap usage and evicts cached meshes before Android kills the process. |
| **Buffer arena** | Pools off-heap mesh buffers so chunk building stops allocating. |
| **Chunk prioritiser** | Builds what you are looking at first, not a disc around you. |
| **Input smoother** | Optional. **Off by default** — any filter on look input adds latency. |

Settings live in `config/pojavxoptimizer.json`. A corrupt file is quarantined to `.corrupt` and
replaced with defaults rather than stopping the game from starting.

---

## Why no conflicts

The mod depends on nothing but Fabric Loader, and it injects rather than replaces:

- **No `@Overwrite`, no `@Redirect`.** Both replace the original bytecode, so two mods using either
  on the same method cannot coexist. Every hook here is an `@Inject`, and multiple injectors at one
  point simply all run.
- **`defaultRequire: 0`.** A mixin target that moves in a future version degrades that one feature
  instead of crashing the game.
- **Nothing throws out of a hook.** Each injection wraps its work and logs once, so a failure in
  this mod can never take the game loop down.
- **Mixins forward to plain classes.** `MinecraftMixin.runTick` is a null-check and a method call.
  All logic lives in `com.goodboysoul.pojavxoptimizer.module`, which no other mod can collide with.

**Sodium is the one hard exception.** It replaces the renderer and so does this mod, so
`fabric.mod.json` declares `"breaks": { "sodium": "*" }` and the loader refuses to start with both
installed. Remove one.

---

## Building

Needs JDK 21. Everything else is fetched by Gradle.

```
git clone https://github.com/kaoakwkwkwkwkwkwkaoakakaakak-collab/PojavXOptimizer.git
cd PojavXOptimizer
./gradlew build
```

Jar lands in `build/libs/pojavxoptimizer-1.0.0.jar`.

Uses Mojang's official mappings, so renderer targets are named as they appear in 1.21.1:
`Minecraft#runTick`, `LevelRenderer#setupRender`, `ParticleEngine#createParticle`,
`EntityRenderDispatcher#shouldRender`.

---

## Status

`1.0.0` — **compiles and packages clean**. The four mixins remap correctly to intermediary
(`class_310`, `class_4184`, `class_702`, `class_1041`), which is what makes them resolve at runtime
rather than only at build time.

Not yet done: the in-game settings screen, the debug HUD overlay, and on-device frame-time
measurements across the four renderers. The modules are wired and running; those three are what is
left before calling it finished.

---

**Owner:** goodboysoul — https://github.com/kaoakwkwkwkwkwkwkaoakakaakak-collab/PojavXOptimizer
