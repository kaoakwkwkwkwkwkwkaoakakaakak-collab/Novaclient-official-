#!/bin/bash
# Provision toolchain if needed, then run the unit tests.
set -u
cd /home/user/PojavXOptimizer

TOOLS=/home/user/tools
JDK=$TOOLS/jdk21
GRADLE=$TOOLS/gradle9

mkdir -p "$TOOLS"
if [ ! -f "$JDK/lib/libjli.so" ] || [ ! -x "$JDK/bin/javac" ]; then
  echo "### JDK provisioning"
  rm -rf "$JDK" "$TOOLS/jdk.tgz"
  curl -sL -m 900 -o "$TOOLS/jdk.tgz" "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.12.1%2B1/OpenJDK21U-jdk_x64_linux_hotspot_21.0.12.1_1.tar.gz"
  tar xzf "$TOOLS/jdk.tgz" -C "$TOOLS" && mv "$TOOLS/jdk-21.0.12.1+1" "$JDK"
  rm -f "$TOOLS/jdk.tgz"
fi
chmod -R +x "$JDK/bin" 2>/dev/null
export JAVA_HOME="$JDK"; export PATH="$JAVA_HOME/bin:$PATH"

if [ ! -f "$GRADLE/lib/gradle-gradle-cli-main-9.5.0.jar" ]; then
  echo "### Gradle provisioning"
  rm -rf "$GRADLE" "$TOOLS/gradle.zip"
  curl -sL -m 900 -o "$TOOLS/gradle.zip" "https://services.gradle.org/distributions/gradle-9.5.0-bin.zip"
  unzip -q -o "$TOOLS/gradle.zip" -d "$TOOLS" && mv "$TOOLS/gradle-9.5.0" "$GRADLE"
  rm -f "$TOOLS/gradle.zip"
fi
chmod -R +x "$GRADLE/bin" 2>/dev/null

echo "### TEST"
"$GRADLE/bin/gradle" --no-daemon --console=plain test 2>&1
echo "TEST_EXIT=$?"

echo "### report"
for f in build/test-results/test/*.xml; do
  [ -f "$f" ] || continue
  python3 - "$f" <<'PY'
import sys, xml.etree.ElementTree as ET
t = ET.parse(sys.argv[1]).getroot()
print(f"  {t.get('name')}: tests={t.get('tests')} failures={t.get('failures')} errors={t.get('errors')} skipped={t.get('skipped')} time={t.get('time')}s")
for tc in t.findall('testcase'):
    bad = tc.find('failure') is not None or tc.find('error') is not None
    print(f"    {'FAIL' if bad else 'pass'}  {tc.get('name')}")
PY
done
