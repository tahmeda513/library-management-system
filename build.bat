@echo off
:: ============================================================
:: build.bat – Compile and run the St Mary's Library System
:: Requires: JDK 22+  and  lib\sqlite-jdbc.jar
::
:: Usage:
::   build.bat          -- launch console UI (Basic Requirement)
::   build.bat --gui    -- launch Swing GUI  (Medium Requirement)
::   build.bat --test   -- compile and run JUnit tests
:: ============================================================

set MAIN_CLASS=Main
set OUT_DIR=out
set LIB=lib\sqlite-jdbc.jar

echo ============================================
echo  St Mary's Digital Library System - Build
echo ============================================

if not exist "%LIB%" (
    echo ERROR: %LIB% not found.
    echo Download sqlite-jdbc-3.45.1.0.jar and place in lib\
    pause & exit /b 1
)

:: Test mode
if "%1"=="--test" (
    set JUNIT_JAR=lib\junit-platform-console-standalone.jar
    if not exist "%JUNIT_JAR%" (
        echo ERROR: %JUNIT_JAR% not found. Place JUnit and Mockito jars in lib\
        pause & exit /b 1
    )
    set TEST_OUT=out_test
    if not exist "%TEST_OUT%" mkdir "%TEST_OUT%"
    echo Compiling main sources...
    dir /s /b *.java | findstr /v "\\out" | findstr /v "\\test\\" > sources_main.txt
    javac --release 22 -cp "%LIB%" -d "%OUT_DIR%" @sources_main.txt
    if %ERRORLEVEL% neq 0 ( echo ERROR: Main compile failed. & del sources_main.txt & pause & exit /b 1 )
    del sources_main.txt
    echo Compiling test sources...
    set TEST_CP=%LIB%;%OUT_DIR%;lib\junit-platform-console-standalone.jar;lib\mockito-core.jar;lib\byte-buddy.jar;lib\byte-buddy-agent.jar;lib\objenesis.jar
    dir /s /b test\*.java > sources_test.txt
    javac --release 22 -cp "%TEST_CP%" -d "%TEST_OUT%" @sources_test.txt
    if %ERRORLEVEL% neq 0 ( echo ERROR: Test compile failed. & del sources_test.txt & pause & exit /b 1 )
    del sources_test.txt
    echo Running tests...
    java -cp "%TEST_CP%;%TEST_OUT%" org.junit.platform.console.ConsoleLauncher --scan-class-path="%TEST_OUT%" --include-package=test
    pause & exit /b
)

:: Application mode
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"
echo Compiling...
dir /s /b *.java | findstr /v "\\out\\" | findstr /v "\\test\\" > sources.txt
javac --release 22 -cp "%LIB%" -d "%OUT_DIR%" @sources.txt
if %ERRORLEVEL% neq 0 (
    echo ERROR: Compilation failed.
    del sources.txt & pause & exit /b 1
)
del sources.txt
echo Done. Launching...
if "%1"=="--gui" (
    echo [GUI mode]
    java -cp "%OUT_DIR%;%LIB%" %MAIN_CLASS% --gui
) else (
    echo [Console mode -- use --gui for Swing interface]
    java -cp "%OUT_DIR%;%LIB%" %MAIN_CLASS%
)
pause
