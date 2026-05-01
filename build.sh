#!/bin/bash
# ============================================================
# build.sh – Compile and run the St Mary's Library System
# Requires: JDK 22+  and  lib/sqlite-jdbc.jar
#
# Usage:
#   ./build.sh          – launch console UI (Basic Requirement)
#   ./build.sh --gui    – launch Swing GUI  (Medium Requirement)
#   ./build.sh --test   – compile and run JUnit tests
# ============================================================

MAIN_CLASS="Main"
OUT_DIR="out"
LIB="lib/sqlite-jdbc.jar"

echo "============================================"
echo " St Mary's Digital Library System – Build  "
echo "============================================"

if ! command -v javac &> /dev/null; then
    echo "ERROR: javac not found. Please install JDK 22+."
    exit 1
fi

if [ ! -f "$LIB" ] || [ ! -s "$LIB" ]; then
    echo "ERROR: $LIB not found."
    echo "Download sqlite-jdbc-3.45.1.0.jar from:"
    echo "  https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.1.0/"
    echo "Place it in the lib/ folder and re-run."
    exit 1
fi

# ── Test mode ────────────────────────────────────────────────────────────────
if [ "$1" = "--test" ]; then
    JUNIT_JAR="lib/junit-platform-console-standalone.jar"
    if [ ! -f "$JUNIT_JAR" ]; then
        echo "ERROR: $JUNIT_JAR not found."
        echo "Download junit-platform-console-standalone-1.10.x.jar from:"
        echo "  https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/"
        echo "Also place mockito-core and byte-buddy jars in lib/ then re-run."
        exit 1
    fi
    TEST_OUT="out_test"
    mkdir -p "$TEST_OUT"
    echo "Compiling main sources..."
    find . -name "*.java" -not -path "./out*" -not -path "./test/*" > sources_main.txt
    javac --release 22 -cp "$LIB" -d "$OUT_DIR" @sources_main.txt
    if [ $? -ne 0 ]; then echo "ERROR: Main compile failed."; rm sources_main.txt; exit 1; fi
    rm sources_main.txt

    echo "Compiling test sources..."
    TEST_CP="$LIB:$OUT_DIR:lib/junit-platform-console-standalone.jar:lib/mockito-core.jar:lib/byte-buddy.jar:lib/byte-buddy-agent.jar:lib/objenesis.jar"
    find test -name "*.java" > sources_test.txt
    javac --release 22 -cp "$TEST_CP" -d "$TEST_OUT" @sources_test.txt
    if [ $? -ne 0 ]; then echo "ERROR: Test compile failed."; rm sources_test.txt; exit 1; fi
    rm sources_test.txt

    echo "Running tests..."
    java -cp "$TEST_CP:$TEST_OUT" org.junit.platform.console.ConsoleLauncher \
         --scan-class-path="$TEST_OUT" --include-package="test"
    exit $?
fi

# ── Application mode ─────────────────────────────────────────────────────────
mkdir -p "$OUT_DIR"
echo "Compiling..."
find . -name "*.java" -not -path "./out/*" -not -path "./test/*" > sources.txt
javac --release 22 -cp "$LIB" -d "$OUT_DIR" @sources.txt
if [ $? -ne 0 ]; then
    echo "ERROR: Compilation failed."
    rm sources.txt; exit 1
fi
rm sources.txt
echo "Compilation successful. Launching..."
if [ "$1" = "--gui" ]; then
    echo "(GUI mode)"
    java -cp "$OUT_DIR:$LIB" "$MAIN_CLASS" --gui
else
    echo "(Console mode — use --gui for the Swing interface)"
    java -cp "$OUT_DIR:$LIB" "$MAIN_CLASS"
fi
