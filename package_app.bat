@echo off
setlocal enabledelayedexpansion

:: ============================================================================
:: CONFIGURATION
:: ============================================================================
set "ROOT=%~dp0"
set "APP_NAME=KeystoneSystems"
set "VERSION=1.0.0"
set "OUT_DIR=%ROOT%package_out"
set "TEMP_DIR=%ROOT%temp_build"
set "MAIN_CLASS=inventory.Main"
set "LIBS_DIR=%ROOT%libs"
set "SRC_DIR=%ROOT%src"

:: Detect Java (using same logic as build.bat)
for /d %%i in ("C:\Program Files\Java\jdk*") do set "JDK_HOME=%%i"
if not defined JDK_HOME (
    echo [ERROR] JDK not found.
    pause
    exit /b 1
)
set "JAR_CMD="!JDK_HOME!\bin\jar.exe""
set "JPACKAGE_CMD="!JDK_HOME!\bin\jpackage.exe""
set "JAVAC_CMD="!JDK_HOME!\bin\javac.exe""

echo.
echo ==========================================
echo PACKAGING KEYSTONE SYSTEMS AS AN EXE
echo ==========================================

:: 1. Clean and setup
if exist "%OUT_DIR%" rd /s /q "%OUT_DIR%"
if exist "%TEMP_DIR%" rd /s /q "%TEMP_DIR%"
mkdir "%TEMP_DIR%"
mkdir "%OUT_DIR%"

:: 2. Compile with dependencies
echo [1/4] Compiling source...
dir /s /b "%SRC_DIR%\*.java" > sources.txt
set "CP=%TEMP_DIR%"
for %%i in ("%LIBS_DIR%\*.jar") do set "CP=!CP!;%%i"

%JAVAC_CMD% -cp "%CP%" -d "%TEMP_DIR%" @sources.txt
del sources.txt

:: 3. Create a unified JAR
echo [2/4] Creating executable JAR...
mkdir "%TEMP_DIR%\libs"
copy "%LIBS_DIR%\*.jar" "%TEMP_DIR%\libs" > nul
:: (Note: Simple way for this level, just bundling)
%JAR_CMD% cfe "%OUT_DIR%\%APP_NAME%.jar" %MAIN_CLASS% -C "%TEMP_DIR%" .

:: 4. Run jpackage
echo [3/4] Creating Windows EXE...
%JPACKAGE_CMD% --type exe --name "%APP_NAME%" --input "%OUT_DIR%" --main-jar "%APP_NAME%.jar" --main-class %MAIN_CLASS% --dest "%OUT_DIR%" --win-dir-chooser --win-menu --win-shortcut --vendor "KeystoneSystems"

:: 5. Copy data folder (so users have defaults)
echo [4/4] Adding default data...
mkdir "%OUT_DIR%\data"
copy "%SRC_DIR%\inventory\data\*.csv" "%OUT_DIR%\data" > nul

echo.
echo ==========================================
echo SUCCESS! Your installer is in: %OUT_DIR%
echo ==========================================
pause
endlocal
