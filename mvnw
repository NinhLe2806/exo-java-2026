#!/usr/bin/env sh
set -eu

MAVEN_VERSION=3.9.9
BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
WRAPPER_DIR="$BASE_DIR/.mvn"
MAVEN_HOME="$WRAPPER_DIR/apache-maven-$MAVEN_VERSION"
MAVEN_CMD="$MAVEN_HOME/bin/mvn"
MAVEN_TGZ="$WRAPPER_DIR/apache-maven-$MAVEN_VERSION-bin.tar.gz"

if [ ! -x "$MAVEN_CMD" ]; then
  mkdir -p "$WRAPPER_DIR"
  URL="https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$URL" -o "$MAVEN_TGZ"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$URL" -O "$MAVEN_TGZ"
  else
    echo "curl or wget is required to download Maven $MAVEN_VERSION" >&2
    exit 1
  fi
  tar -xzf "$MAVEN_TGZ" -C "$WRAPPER_DIR"
fi

exec "$MAVEN_CMD" "$@"
