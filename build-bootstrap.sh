#!/bin/bash
# PojavXOptimizer build. Self-healing: re-provisions JDK 21 / Gradle 9.5.0 if the sandbox
# truncated them, verifies each before use, then builds.
set -u
cd /home/user/PojavXOptimizer

TOOLS=/home/user/tools
JDK=$TOOLS/jdk21
GRADLE=$TOOLS/gradle9
JDK_URL="https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.12.1%2B1/OpenJDK21U-jdk_x64_linux_hotspot_21.0.12.1_1.tar.gz"
GRADLE_URL="https://services.gradle.org/distributions/gradle-9.5.0-bin.zip"

mkdir -p "$TOOLS"

echo "### [1/4] JDK 21"
if [ ! -f "$JDK/lib/libjli.so" ] || [ ! -x "$JDK/bin/javac" ]; then
  echo "    provisioning (previous copy missing or truncated)"
  rm -rf "$JDK" "$TOOLS/jdk.tgz"
  curl -sL -m 900 -o "$TOOLS/jdk.tgz" "$JDK_URL"
  tar xzf "$TOOLS/jdk.tgz" -C "$TOOLS" && mv "$TOOLS/jdk-21.0.12.1+1" "$JDK"
  rm -f "$TOOLS/jdk.tgz"
fi
chmod -R +x "$JDK/bin" 2>/dev/null
export JAVA_HOME="$JDK"; export PATH="$JAVA_HOME/bin:$PATH"
if ! "$JDK/bin/java" -version 2>&1 | head -1; then echo "FATAL: JDK unusable"; exit 1; fi

echo "### [2/4] Gradle 9.5.0"
if [ ! -f "$GRADLE/lib/gradle-gradle-cli-main-9.5.0.jar" ]; then
  echo "    provisioning (previous copy missing or truncated)"
  rm -rf "$GRADLE" "$TOOLS/gradle.zip"
  curl -sL -m 900 -o "$TOOLS/gradle.zip" "$GRADLE_URL"
  unzip -q -o "$TOOLS/gradle.zip" -d "$TOOLS" && mv "$TOOLS/gradle-9.5.0" "$GRADLE"
  rm -f "$TOOLS/gradle.zip"
fi
chmod -R +x "$GRADLE/bin" 2>/dev/null
if [ ! -f "$GRADLE/lib/gradle-gradle-cli-main-9.5.0.jar" ]; then echo "FATAL: Gradle unusable"; exit 1; fi
echo "    ok"

echo "### [3/4] wrapper"
"$GRADLE/bin/gradle" wrapper --gradle-version 9.5.0 --no-daemon --console=plain -q 2>&1 | tail -3

echo "### [4/4] BUILD"
"$GRADLE/bin/gradle" --no-daemon --console=plain build 2>&1
echo "BUILD_EXIT=$?"

echo "### artifacts"
find build/libs -type f 2>/dev/null | sort
