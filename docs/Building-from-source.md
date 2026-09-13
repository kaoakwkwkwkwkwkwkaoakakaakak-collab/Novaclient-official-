# Building from source

## Requirements

| | |
|---|---|
| JDK | **21** |
| Gradle | **9.5.0** (the wrapper will fetch it) |
| Loom | `fabric-loom 1.17.20`, set in `build.gradle` |

## Clone and build

```bash
git clone https://github.com/kaoakwkwkwkwkwkwkaoakakaakak-collab/Novaclient-official-.git
cd Novaclient-official-
./gradlew build
```

The jar lands in `build/libs/novaclient-0.1.0.jar`.

The repository also ships `build-bootstrap.sh`, which downloads the JDK and Gradle if they are
missing and then builds. Useful in a container with no toolchain.

## Tests

```bash
./gradlew test
```

17 tests. They cover module name uniqueness, the advertised module and tab counts, category
grouping, effect application in both directions, self-disabling when an effect throws, keybind
lookup, search, slider clamping and snapping, mode cycling, and setting serialisation.

The census test asserts the exact per-tab counts. If you add or remove a module it will fail until
you update it — that is deliberate, it keeps the advertised number honest.

## Verifying mixins before you ship

```bash
bash verify-mixins.sh
```

This javaps every `@Inject` and `@Redirect` target and fails if a handler's static-ness disagrees
with it. Run `./gradlew build` once first so the Minecraft jar is in the Loom cache.

**Do not skip this.** A mixin shape mismatch compiles cleanly and crashes the game at startup.
Version 0.1.0 shipped with exactly that bug.

## Layout

```
src/main/java/dev/novaclient/
  NovaClient.java          entrypoint, Right Shift polling, tick dispatch
  module/                  Module, ActiveModule, ModuleManager, Category, the six *Modules classes
  setting/                 ToggleSetting, SliderSetting, ModeSetting
  effect/                  OptionAccess + the *Effects classes that do the real work
  state/                   atomic state the mixins read
  hud/                     HUD widgets and the renderer
  gui/                     Theme and ClickGuiScreen
  config/                  ConfigManager (Gson to novaclient.json)
  mixin/                   12 mixins
src/main/resources/
  fabric.mod.json
  novaclient.mixins.json
  novaclient.accesswidener
  assets/novaclient/shaders/   the motion blur post chain
src/test/java/dev/novaclient/
```

## How the layers fit

```
module  ->  effect  ->  state  ->  mixin reads state
   |           |
   |           +-> OptionAccess  ->  writes a real vanilla OptionInstance
   |
   +-> hud (widgets read state directly)
```

A module never touches Minecraft directly. It calls an effect, the effect either writes a real
option through `OptionAccess` or sets an atomic in `state/`, and a mixin reads that atomic during
rendering.

This is why `verify-mixins.sh` matters: the state layer can be correct and the mixin still fail to
apply, leaving a module that silently does nothing.

## Access widener

Three entries, all private members:

```
accessible method net/minecraft/client/renderer/GameRenderer loadEffect (...)V
accessible method net/minecraft/client/renderer/GameRenderer shutdownShaders ()V
accessible field  net/minecraft/client/Minecraft rightClickDelay I
accessible field  net/minecraft/client/gui/components/DebugScreenOverlay renderDebug Z
```

Loom remaps these to intermediary at build time. If you add one, use mojmap names — Loom does the
translation.

## Licence note for contributors

The project is **CC BY-NC-ND 4.0**. The ND term means no derivatives, so this repository does not
accept pull requests that modify the code. Issues and bug reports with logs are very welcome.

Do not paste code from GPL mods into this project. The licences are incompatible — that is why
Motion Blur is an independent implementation rather than a port of the GPL-licensed mod it was
researched against.
