#!/usr/bin/env bash

# Gradle wrapper for irtax

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-17-openjdk}"
export ANDROID_HOME="$DIR/android-sdk"
export PATH="$JAVA_HOME/bin:$PATH"

cd "$DIR"

if [ -f "./bin/gradle" ]; then
    exec ./bin/gradle "$@"
else
    echo "Error: Gradle not found"
    exit 1
fi
