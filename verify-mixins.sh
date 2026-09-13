#!/usr/bin/env bash
set -uo pipefail
JAR=$(find "$HOME/.gradle" -name "minecraft-merged-*.jar" ! -name "*intermediary*" 2>/dev/null | head -1)
JAVAP=$(find "$HOME/tools" -name javap -type f 2>/dev/null | head -1)
[ -z "$JAVAP" ] && JAVAP=$(command -v javap)
if [ -z "$JAR" ] || [ -z "$JAVAP" ]; then
  echo "cannot verify: need the minecraft-merged jar and javap" >&2
  echo "run 'bash build-bootstrap.sh' once first" >&2
  exit 2
fi
python3 - "$JAR" "$JAVAP" <<'PY'
import subprocess, re, sys, pathlib
jar, javap = sys.argv[1], sys.argv[2]
md = pathlib.Path('src/main/java/dev/novaclient/mixin')
cache = {}
def target_is_static(cls, meth):
    if cls not in cache:
        r = subprocess.run([javap, '-p', '-cp', jar, cls], capture_output=True, text=True)
        cache[cls] = r.stdout
    for line in cache[cls].splitlines():
        if re.search(r'\b' + re.escape(meth) + r'\s*\(', line):
            return 'static' in line
    return None
TARGETS = {
    'MinecraftMixin':              'net.minecraft.client.Minecraft',
    'GuiMixin':                    'net.minecraft.client.gui.Gui',
    'FogRendererMixin':            'net.minecraft.client.renderer.FogRenderer',
    'ScreenEffectRendererMixin':   'net.minecraft.client.renderer.ScreenEffectRenderer',
    'EntityRenderDispatcherMixin': 'net.minecraft.client.renderer.entity.EntityRenderDispatcher',
    'ItemInHandRendererMixin':     'net.minecraft.client.renderer.ItemInHandRenderer',
    'ItemEntityRendererMixin':     'net.minecraft.client.renderer.entity.ItemEntityRenderer',
    'LevelRendererMixin':          'net.minecraft.client.renderer.LevelRenderer',
    'ScreenMixin':                 'net.minecraft.client.gui.screens.Screen',
    'PlayerMixin':                 'net.minecraft.world.entity.player.Player',
    'MinecraftAttackMixin':        'net.minecraft.client.Minecraft',
    'ScreenshotMixin':             'net.minecraft.client.KeyboardHandler',
}
checked = bad = missing = 0
for stem, cls in TARGETS.items():
    p = md / (stem + '.java')
    if not p.exists():
        print(f"  MISSING FILE  {stem}.java")
        missing += 1
        continue
    src = p.read_text()
    for block in re.split(r'(?=@Inject|@Redirect)', src):
        mm = re.search(r'method\s*=\s*"([A-Za-z0-9_]+)', block)
        if not mm:
            continue
        meth = mm.group(1)
        hm = re.search(r'(?:private|public|protected)\s+(static\s+)?[\w<>\[\]]+\s+(nova[\w$]*)\s*\(', block)
        if not hm:
            continue
        handler_static = bool(hm.group(1))
        target_static = target_is_static(cls, meth)
        checked += 1
        if target_static is None:
            print(f"  TARGET NOT FOUND  {stem}  {cls}#{meth}")
            bad += 1
        elif target_static != handler_static:
            print(f"  MISMATCH  {stem}#{hm.group(2)}  target_static={target_static} handler_static={handler_static}")
            bad += 1
print(f"\n  injections checked: {checked}   problems: {bad}   missing files: {missing}")
sys.exit(1 if (bad or missing) else 0)
PY
