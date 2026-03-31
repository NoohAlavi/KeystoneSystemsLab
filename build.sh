#!/usr/bin/env bash

set -e

# ============================================================================
# JAVA DETECTION
# ============================================================================

if command -v javac >/dev/null 2>&1; then
    echo "[INFO] javac found in PATH."
else
    echo "'javac' command not found in PATH."
    echo "Attempting to locate JDK automatically..."
    for dir in /usr/lib/jvm/* /Library/Java/JavaVirtualMachines/*; do
        if [ -d "$dir" ] && [ -x "$dir/bin/javac" ]; then
            export JAVA_HOME="$dir"
            export PATH="$JAVA_HOME/bin:$PATH"
            echo "[INFO] Found JDK at: $dir"
            break
        fi
    done
    if ! command -v javac >/dev/null 2>&1; then
        echo "[ERROR] Could not find a Java Development Kit (JDK)."
        echo "Please install a JDK and ensure javac is in PATH."
        exit 1
    fi
fi

# ============================================================================
# CONFIGURATION
# ============================================================================
SRC_DIR="src"
OUT_DIR="out/production/KeystoneSystemsLab"
MAIN_CLASS="inventory.Main"
LIB_DIR="libs"

# ============================================================================
# 0. CLEAN STAGE
# ============================================================================
# Triggered by running: ./build.sh clean
if [ "$1" == "clean" ]; then
    echo
    echo "[0/3] Cleaning Project..."
    echo "------------------------------------------"
    if [ -d "out" ]; then
        rm -rf "out"
        echo "Successfully deleted 'out' directory."
    else
        echo "Nothing to clean (out directory doesn't exist)."
    fi
fi

# Ensure output directory exists after potential clean
mkdir -p "$OUT_DIR"

# ============================================================================
# 1. COMPILE
# ============================================================================
echo
echo "[1/3] Compiling Project..."
echo "------------------------------------------"

find "$SRC_DIR" -name "*.java" > sources.txt

# Classpath includes your JSON library and the production output folder
CLASSPATH="$LIB_DIR/json-20230227.jar:$OUT_DIR"

if ! javac -cp "$CLASSPATH" -d "$OUT_DIR" @sources.txt; then
    echo
    echo "[ERROR] Compilation failed!"
    rm -f sources.txt
    exit 1
fi

rm -f sources.txt
echo "Compilation successful."

# ============================================================================
# 2. COPY RESOURCES (CSV files)
# ============================================================================
echo
echo "[2/3] Copying Data Resources..."
echo "------------------------------------------"

mkdir -p "$OUT_DIR/inventory/data"
# Copying CSVs from your data folder to the production folder
cp -r "$SRC_DIR/inventory/data/"*.csv "$OUT_DIR/inventory/data/" 2>/dev/null || true

echo "Resources copied."

# ============================================================================
# 3. RUN
# ============================================================================
echo
echo "[3/3] Running Application..."
echo "------------------------------------------"
echo

if ! java -cp "$CLASSPATH" "$MAIN_CLASS"; then
    echo
    echo "[ERROR] Application crashed or failed to start."
fi

echo "Process finished."