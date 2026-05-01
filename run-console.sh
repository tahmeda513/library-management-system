#!/bin/bash
# ============================================================
# run-console.sh  –  St Mary's Library: Console (CLI) mode
# Requires: JDK 22+  and  lib/sqlite-jdbc.jar
# ============================================================
LIB="lib/sqlite-jdbc.jar"
OUT="out"

if [ ! -f "$LIB" ]; then
  echo "ERROR: $LIB not found. Place sqlite-jdbc.jar in lib/ and retry."
  exit 1
fi

mkdir -p "$OUT"
echo "Compiling..."
find . -name "*.java" -not -path "./out*" -not -path "./test/*" > _src.txt
javac --release 22 -cp "$LIB" -d "$OUT" @_src.txt
STATUS=$?
rm -f _src.txt
if [ $STATUS -ne 0 ]; then echo "ERROR: Compilation failed."; exit 1; fi

echo "Launching console UI..."
java -cp "$OUT:$LIB" Main
