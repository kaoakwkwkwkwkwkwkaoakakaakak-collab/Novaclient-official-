# Troubleshooting

## The game crashes at startup

Most likely a mixin failed to apply. Look for a line like this near the top of the log:

```
Mixin apply for mod novaclient failed novaclient.mixins.json:<Something>
```

That line names the exact mixin and the reason. Copy it into an issue — it is the single most
useful piece of information.

### A crash that already happened and was fixed

Version 0.1.0 shipped once with this:

```
InvalidInjectionException: non-static callback method
net/minecraft/class_758::novaSuppressFog targets a static method which is not supported
```

`FogRenderer.setupFog` is static, and the handler was not. Mixin refuses that and aborts startup.

This is worth understanding because **it compiles without error**. `javac` cannot see that a mixin
handler's static-ness disagrees with its target, so a green build proves nothing about it. The
repository now has `verify-mixins.sh` to catch exactly this before a build ships.

If you are on the original 0.1.0 jar, re-download from the release page.

## Finding the logs

| Launcher | Location |
|---|---|
| Official | `.minecraft/logs/latest.log` |
| Official crash report | `.minecraft/crash-reports/` |
| Pojav / Turtle (Android) | `Android/data/<launcher>/files/.minecraft/logs/latest.log` |

The crash report path is also printed at the very end of the log, after `Game crashed!`.

## Reporting a crash

Include:

1. The full `latest.log`, or at least everything from the first `ERROR` line down.
2. The crash report if one was written.
3. Your mod list (it is in the crash report under `Resource Packs` / the mod list section).
4. Minecraft version and launcher.

Do not paraphrase the exception. The raw Mixin error names the target method, which is what makes
it fixable.

## Motion Blur looks wrong

- **Screen smears and never clears** — the Amount is too high. Drop it; the shader clamps at 0.95
  but a high value still leaves a long trail.
- **Blur stops working after looking at a creeper or end portal** — this was a real bug and is
  fixed. The module reloads its post chain when vanilla replaces it. Update if you see it.
- **Blur behaves oddly with a shader pack** — Iris owns the post pipeline when a pack is active.
  Turn off Motion Blur or the shader pack.
- **Blur does nothing at all** — check the log for a shader load failure. The module records one
  rather than crashing.

## A module toggle does nothing

Check the [Modules](Modules.md) page — the descriptions there match the source. If a module claims
something the game does not do, that is a bug worth reporting.

Historically several modules wrote to internal state that no renderer hook read. Those have been
wired or removed, but the pattern is worth knowing if something looks dead.

## The GUI will not open

Right Shift is polled directly, not through Minecraft's keybind system, so rebinding other keys
will not break it. If it does not open:

- Confirm NovaClient appears in the mod list on the title screen.
- Check the log for a NovaClient initialisation error.
- Some launchers remap Right Shift. Try an external keyboard if you are on Android.

## Settings reset between sessions

Config saves when the GUI closes. If you quit while the GUI is still open, the last change may not
be written. Close the GUI with Right Shift before quitting.

## Still stuck

Open an issue with the log attached. A real runtime log has caught bugs that static checks missed
more than once — it is genuinely the fastest route to a fix.
