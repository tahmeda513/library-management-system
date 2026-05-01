@echo off
:: ============================================================
:: run-console.bat  --  St Mary's Library: Console (CLI) mode
:: Requires: JDK 22+  and  lib\sqlite-jdbc.jar
:: ============================================================
set LIB=lib\sqlite-jdbc.jar
set OUT=out

if not exist "%LIB%" (
    echo ERROR: %LIB% not found. Place sqlite-jdbc.jar in lib\ and retry.
    pause & exit /b 1
)

if not exist "%OUT%" mkdir "%OUT%"
echo Compiling...
dir /s /b *.java | findstr /v "\\out\\" | findstr /v "\\test\\" > _src.txt
javac --release 22 -cp "%LIB%" -d "%OUT%" @_src.txt
if %ERRORLEVEL% neq 0 ( echo ERROR: Compilation failed. & del _src.txt & pause & exit /b 1 )
del _src.txt

echo Launching console UI...
java -cp "%OUT%;%LIB%" Main
pause
