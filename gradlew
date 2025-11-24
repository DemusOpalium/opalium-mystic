#!/usr/bin/env sh

DIR="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_JAR="$DIR/gradle/wrapper/gradle-wrapper.jar"
PROPS_FILE="$DIR/gradle/wrapper/gradle-wrapper.properties"

# Download the wrapper JAR if it is missing to avoid committing binaries
if [ ! -f "$WRAPPER_JAR" ]; then
  if [ ! -f "$PROPS_FILE" ]; then
    echo "Gradle wrapper properties not found at $PROPS_FILE" >&2
    exit 1
  fi

  distributionUrl=$(grep '^distributionUrl=' "$PROPS_FILE" | cut -d'=' -f2-)
  if [ -z "$distributionUrl" ]; then
    echo "distributionUrl is not defined in $PROPS_FILE" >&2
    exit 1
  fi

  echo "Downloading Gradle wrapper JAR from $distributionUrl ..."
  TMP_DIR=$(mktemp -d)
  ZIP_PATH="$TMP_DIR/gradle-distribution.zip"

  if command -v curl >/dev/null 2>&1; then
    curl -L "$distributionUrl" -o "$ZIP_PATH"
  elif command -v wget >/dev/null 2>&1; then
    wget "$distributionUrl" -O "$ZIP_PATH"
  else
    echo "Neither curl nor wget is available to download Gradle." >&2
    rm -rf "$TMP_DIR"
    exit 1
  fi

  # Extract the wrapper JAR from the distribution
  if command -v unzip >/dev/null 2>&1; then
    WRAPPER_FROM_ZIP=$(unzip -Z1 "$ZIP_PATH" "*/lib/gradle-wrapper-*.jar" | head -n1)
    if [ -z "$WRAPPER_FROM_ZIP" ]; then
      echo "Could not find gradle-wrapper jar inside the distribution." >&2
      rm -rf "$TMP_DIR"
      exit 1
    fi
    unzip -p "$ZIP_PATH" "$WRAPPER_FROM_ZIP" > "$WRAPPER_JAR"
  else
    echo "The 'unzip' command is required to extract the Gradle wrapper jar." >&2
    rm -rf "$TMP_DIR"
    exit 1
  fi

  rm -rf "$TMP_DIR"
fi

JAVA_HOME="${JAVA_HOME:-}"
if [ -n "$JAVA_HOME" ]; then
  JAVA_BIN="$JAVA_HOME/bin/java"
else
  JAVA_BIN="java"
fi

exec "$JAVA_BIN" -Dfile.encoding=UTF-8 -jar "$WRAPPER_JAR" "$@"
