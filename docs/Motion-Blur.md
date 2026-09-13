# Motion Blur

Motion Blur is the one module that does real graphics work, so it is worth explaining how.

## What it is

Frame accumulation. Each rendered frame is blended with the frame before it, so fast camera
movement leaves a short trail instead of strobing.

The blend is:

```glsl
fragColor = mix(current, previous, clamp(Retention, 0.0, 0.95));
```

`Retention` is how much of the previous frame survives. Higher means a longer, smoother trail.
The **Amount** slider in the GUI maps straight onto it.

## How it is wired

NovaClient uses Minecraft's own post-processing chain — the same system vanilla uses for the
creeper and end-portal vision effects. No third-party library, no Fabric API.

Three resources ship inside the jar:

| File | Purpose |
|---|---|
| `assets/novaclient/shaders/post/nova_blur.json` | the post chain: blend, store as previous, blit back |
| `assets/novaclient/shaders/program/nova_blur.json` | the program: samplers and uniform declarations |
| `assets/novaclient/shaders/program/nova_blur.fsh` | the fragment shader above |

The Java side:

```java
gameRenderer.loadEffect(BLUR_EFFECT);
PostChain chain = gameRenderer.currentEffect();
chain.setUniform("Retention", retentionFor(amount));
```

`loadEffect` and `shutdownShaders` are private in `GameRenderer`, so NovaClient ships a small
access widener to open them.

## Two details that matter

**The chain gets replaced.** `GameRenderer.checkEntityPostEffect` overwrites `postEffect` when you
look at a creeper, a spider, or an end portal. If the chain were loaded once and forgotten, motion
blur would silently die for the rest of the session. So every tick the module checks whether
`currentEffect()` still returns the chain it loaded, and reloads if it does not.

**In Menus Only.** When that toggle is on, the module loads the chain when a screen opens and
unloads it when you close one, rather than blurring the world.

## Settings

| Setting | Range | Default |
|---|---|---|
| Amount | 5–90%, step 5 | 40% |
| In Menus Only | on / off | off |

Amount is clamped to 0.95 in the shader regardless of the slider, so it can never freeze the
screen on a stale frame.

## Performance

A post chain costs one extra full-screen pass per frame plus the two blits. On a weak GPU that is
measurable. Turn it off if you are chasing frames — the rest of the Performance tab is where the
actual FPS comes from.

## Why this is not copied

The technique is standard and used by several mods. The reference implementation on Modrinth
(`Noryea/motionblur-fabric`) is **GPL-3.0-only**, which is incompatible with this project's
CC BY-NC-ND licence, so no code was taken from it. The shader and the Java here are independent
implementations of the same idea, and NovaClient avoids the Satin dependency that mod requires.

The shader schema was checked against that project's 1.21 branch to confirm the format is correct
for this Minecraft version.
