#!/bin/bash

# ============================================================================
# CONFIGURATION
# ============================================================================
APP_NAME="KeystoneSystems"
VERSION="1.0.0"
OUT_DIR="./package_out"
TEMP_DIR="./temp_build"
MAIN_CLASS="inventory.Main"
LIBS_DIR="./libs"
SRC_DIR="./src"

# Detect Java (JDK_HOME)
if [ -n "$JAVA_HOME" ]; then
    JDK_HOME="$JAVA_HOME"
else
    # Try to find java location on Unix
    JDK_HOME=$(dirname $(dirname $(readlink -f $(which javac))))
fi

if [ ! -f "$JDK_HOME/bin/jpackage" ]; then
    echo "[ERROR] jpackage not found. Ensure you have JDK 14+ installed."
    exit 1
fi

JAR_CMD="$JDK_HOME/bin/jar"
JPACKAGE_CMD="$JDK_HOME/bin/jpackage"
JAVAC_CMD="$JDK_HOME/bin/javac"

echo ""
echo "=========================================="
echo "PACKAGING KEYSTONE SYSTEMS (UNIX)"
echo "=========================================="

# 1. Clean and setup
rm -rf "$OUT_DIR"
rm -rf "$TEMP_DIR"
mkdir -p "$TEMP_DIR"
mkdir -p "$OUT_DIR"

# 2. Compile with dependencies
echo "[1/4] Compiling source..."
find "$SRC_DIR" -name "*.java" > sources.txt

# Build Classpath
CP="$TEMP_DIR"
for jar in "$LIBS_DIR"/*.jar; do
    CP="$CP:$jar"
done

"$JAVAC_CMD" -cp "$CP" -d "$TEMP_DIR" @sources.txt
rm sources.txt

# 3. Create a unified JAR
echo "[2/4] Creating executable JAR..."
mkdir -p "$TEMP_DIR/libs"
cp "$LIBS_DIR"/*.jar "$TEMP_DIR/libs/"
"$JAR_CMD" cfe "$OUT_DIR/$APP_NAME.jar" "$MAIN_CLASS" -C "$TEMP_DIR" .

# 4. Run jpackage
echo "[3/4] Creating Application Package..."
# Note: On Unix, this creates a .deb, .rpm, or .dmg depending on the OS
"$JPACKAGE_CMD" --type app-image --name "$APP_NAME" --input "$OUT_DIR" --main-jar "$APP_NAME.jar" --main-class "$MAIN_CLASS" --dest "$OUT_DIR" --vendor "KeystoneSystems"

# 5. Copy data folder
echo "[4/4] Adding default data..."
mkdir -p "$OUT_DIR/data"
cp "$SRC_DIR/inventory/data/"*.csv "$OUT_DIR/data/"

echo ""
echo "=========================================="
echo "SUCCESS! Your package is in: $OUT_DIR"
echo "=========================================="
