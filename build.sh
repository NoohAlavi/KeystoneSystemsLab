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

    # Common Linux/macOS locations
    for dir in /usr/lib/jvm/* /Library/Java/JavaVirtualMachines/*; do
        if [ -d "$dir" ] && [ -x "$dir/bin/javac" ]; then
            export JAVA_HOME="$dir"
            export PATH="$JAVA_HOME/bin:$PATH"
            echo "[INFO] Found JDK at: $dir"
            break
        fi
    done

    # Check again
    if ! command -v javac >/dev/null 2>&1; then
        echo
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

mkdir -p "$OUT_DIR"

# ============================================================================
# 1. COMPILE
# ============================================================================
echo
echo "[1/3] Compiling Project..."
echo "------------------------------------------"

find "$SRC_DIR" -name "*.java" > sources.txt

if ! javac -d "$OUT_DIR" @sources.txt; then
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

cp -r "$SRC_DIR/inventory/data/"*.csv "$OUT_DIR/inventory/data/" 2>/dev/null || true

echo "Resources copied."

# ============================================================================
# 3. RUN
# ============================================================================
echo
echo "[3/3] Running Application..."
echo "------------------------------------------"
echo

if ! java -cp "$OUT_DIR" "$MAIN_CLASS"; then
    echo
    echo "[ERROR] Application crashed or failed to start."
fi

echo