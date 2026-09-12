#!/bin/bash
set -u
cd /home/user/NovaClient
TOOLS=/home/user/tools
JDK=$TOOLS/jdk21
GRADLE=$TOOLS/gradle9
JDK_URL="https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.12.1%2B1/OpenJDK21U-jdk_x64_linux_hotspot_21.0.12.1_1.tar.gz"
GRADLE_URL="https://services.gradle.org/distributions/gradle-9.5.0-bin.zip"
mkdir -p "$TOOLS"
if [ ! -f "$JDK/lib/libjli.so" ] || [ ! -x "$JDK/bin/javac" ]; then
  rm -rf "$JDK" "$TOOLS/jdk.tgz"
  curl -sL -m 900 -o "$TOOLS/jdk.tgz" "$JDK_URL"
  tar xzf "$TOOLS/jdk.tgz" -C "$TOOLS" && mv "$TOOLS/jdk-21.0.12.1+1" "$JDK"
  rm -f "$TOOLS/jdk.tgz"
fi
chmod -R +x "$JDK/bin" 2>/dev/null
export JAVA_HOME="$JDK"
export PATH="$JAVA_HOME/bin:$PATH"
if [ ! -x "$GRADLE/bin/gradle" ]; then
  rm -rf "$GRADLE" "$TOOLS/gradle.zip"
  curl -sL -m 900 -o "$TOOLS/gradle.zip" "$GRADLE_URL"
  unzip -qo "$TOOLS/gradle.zip" -d "$TOOLS"
  mv "$TOOLS/gradle-9.5.0" "$GRADLE" 2>/dev/null
  rm -f "$TOOLS/gradle.zip"
fi
chmod -R +x "$GRADLE/bin" 2>/dev/null
rm -rf build/test-results
bash "$GRADLE/bin/gradle" --no-daemon --console=plain test
echo "TEST_EXIT=$?"
echo "=== report ==="
python3 - <<'PY'
import glob, os, xml.etree.ElementTree as ET
total = failures = errors = 0
for path in sorted(glob.glob('build/test-results/test/*.xml')):
    root = ET.parse(path).getroot()
    name = os.path.basename(path).replace('TEST-', '').replace('.xml', '')
    t = int(root.get('tests', 0)); f = int(root.get('failures', 0)); e = int(root.get('errors', 0))
    total += t; failures += f; errors += e
    print(f"  {name}: tests={t} failures={f} errors={e}")
    for tc in root.iter('testcase'):
        bad = list(tc.iter('failure')) + list(tc.iter('error'))
        print(('    FAIL  ' if bad else '    pass  ') + tc.get('name'))
        for b in bad:
            print('          ' + (b.get('message') or '').split('\n')[0])
print(f"  TOTAL tests={total} failures={failures} errors={errors}")
PY
