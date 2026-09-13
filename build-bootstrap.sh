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
  echo "provisioning JDK 21"
  rm -rf "$JDK" "$TOOLS/jdk.tgz"
  curl -sL -m 900 -o "$TOOLS/jdk.tgz" "$JDK_URL"
  tar xzf "$TOOLS/jdk.tgz" -C "$TOOLS" && mv "$TOOLS/jdk-21.0.12.1+1" "$JDK"
  rm -f "$TOOLS/jdk.tgz"
fi
chmod -R +x "$JDK/bin" 2>/dev/null
export JAVA_HOME="$JDK"
export PATH="$JAVA_HOME/bin:$PATH"
if ! "$JDK/bin/java" -version 2>&1 | head -1; then echo "FATAL: JDK unusable"; exit 1; fi
if [ ! -x "$GRADLE/bin/gradle" ]; then
  echo "provisioning Gradle 9.5.0"
  rm -rf "$GRADLE" "$TOOLS/gradle.zip"
  curl -sL -m 900 -o "$TOOLS/gradle.zip" "$GRADLE_URL"
  unzip -qo "$TOOLS/gradle.zip" -d "$TOOLS"
  mv "$TOOLS/gradle-9.5.0" "$GRADLE" 2>/dev/null
  rm -f "$TOOLS/gradle.zip"
fi
chmod -R +x "$GRADLE/bin" 2>/dev/null
rm -rf build/libs
bash "$GRADLE/bin/gradle" --no-daemon --console=plain build
echo "BUILD_EXIT=$?"
for j in build/libs/*.jar; do
  [ -f "$j" ] && echo "$(stat -c%s "$j") $j"
done
