#!/usr/bin/env bash

set -e

# ============================================================================
# JAVA DETECTION
# ============================================================================
if ! command -v javac >/dev/null 2>&1; then
    echo "[INFO] Attempting to locate JDK automatically..."
    for dir in /usr/lib/jvm/* /Library/Java/JavaVirtualMachines/*; do
        if [ -d "$dir" ] && [ -x "$dir/bin/javac" ]; then
            export JAVA_HOME="$dir"
            export PATH="$JAVA_HOME/bin:$PATH"
            break
        fi
    done
    if ! command -v javac >/dev/null 2>&1; then
        echo "[ERROR] Could not find a JDK. Please install one."
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
CLASSPATH="$LIB_DIR/json-20230227.jar:$OUT_DIR"

# ============================================================================
# 0. CLEAN STAGE
# ============================================================================
if [ "$1" == "clean" ]; then
    echo -e "\n[0/3] Cleaning Project..."
    rm -rf "out"
    echo "Successfully deleted 'out' directory."
    # If we are only cleaning, we can exit here or continue
fi

mkdir -p "$OUT_DIR"

# ============================================================================
# 1. SMART COMPILE
# ============================================================================
echo -e "\n[1/3] Checking Dependencies..."
echo "------------------------------------------"

NEEDS_RECOMPILE=false

# Check if output directory is empty
if [ -z "$(ls -A "$OUT_DIR" 2>/dev/null)" ]; then
    NEEDS_RECOMPILE=true
else
    # Find the newest source file and newest class file
    # We use 'find' with 'stat' to get modification times
    NEWEST_SRC=$(find "$SRC_DIR" -name "*.java" -printf '%T@ %p\n' | sort -n | tail -1 | cut -f1 -d' ')
    NEWEST_OUT=$(find "$OUT_DIR" -name "*.class" -printf '%T@ %p\n' | sort -n | tail -1 | cut -f1 -d' ')

    # If source is newer than output, or output doesn't exist
    if [[ -z "$NEWEST_OUT" ]] || (( $(echo "$NEWEST_SRC > $NEWEST_OUT" | bc -l) )); then
        NEEDS_RECOMPILE=true
    fi
fi

if [ "$NEEDS_RECOMPILE" = true ]; then
    echo "Changes detected. Compiling..."
    find "$SRC_DIR" -name "*.java" > sources.txt
    if ! javac -cp "$CLASSPATH" -d "$OUT_DIR" @sources.txt; then
        echo -e "\n[ERROR] Compilation failed!"
        rm -f sources.txt
        exit 1
    fi
    rm -f sources.txt
    echo "Compilation successful."
else
    echo "Everything up-to-date. Skipping compilation."
fi

# ============================================================================
# 2. COPY RESOURCES
# ============================================================================
# Only copy if source CSVs are newer than the destination or destination is missing
# Simplified: We'll just sync them.
mkdir -p "$OUT_DIR/inventory/data"
cp -ru "$SRC_DIR/inventory/data/"*.csv "$OUT_DIR/inventory/data/" 2>/dev/null || true

# ============================================================================
# 3. RUN
# ============================================================================
echo -e "\n[3/3] Running Application..."
echo "------------------------------------------"
java -cp "$CLASSPATH" "$MAIN_CLASS"