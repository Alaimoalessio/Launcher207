#!/bin/zsh
set -eu
cd "${0:A:h}"
if [[ -z "${JAVA_HOME:-}" ]]; then
  if [[ -d "/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home" ]]; then
    export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
  else
    export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
  fi
fi
./gradlew assembleDebug lintDebug testDebugUnitTest
mkdir -p ../apk
cp app/build/outputs/apk/debug/app-debug.apk ../apk/PeugeotLauncher-0.1.3-demo.apk
(
  cd ../apk
  shasum -a 256 PeugeotLauncher-0.1.3-demo.apk > PeugeotLauncher-0.1.3-demo.apk.sha256
)
