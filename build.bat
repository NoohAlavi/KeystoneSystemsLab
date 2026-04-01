@echo off
setlocal enabledelayedexpansion

:: ============================================================================
:: JAVA DETECTION
:: ============================================================================
echo Detecting Java environment...

if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\javac.exe" (
        set "JDK_HOME=%JAVA_HOME%"
        goto :FOUND_JDK
    )
)

for /d %%i in ("C:\Program Files\Java\jdk*") do set "JDK_HOME=%%i"
if defined JDK_HOME goto :FOUND_JDK

echo [ERROR] Could not find a JDK installation.
pause
exit /b 1

:FOUND_JDK
set "JAVAC_CMD=!JDK_HOME!\bin\javac.exe"
set "JAVA_CMD=!JDK_HOME!\bin\java.exe"

:: ============================================================================
:: CONFIGURATION
:: ============================================================================
set "SRC_DIR=src"
set "OUT_DIR=out\production\KeystoneSystemsLab"
set "MAIN_CLASS=inventory.Main"
set "LIB_DIR=libs"
set "CLASSPATH=%LIB_DIR%\json-20230227.jar;%OUT_DIR%"
set "LAST_BUILD_MARKER=%OUT_DIR%\.last_build"

:: ============================================================================
:: 0. CLEAN STAGE
:: ============================================================================
if /I "%1"=="clean" (
    echo [0/3] Cleaning Project...
    if exist "out" rd /s /q "out"
    echo Successfully deleted 'out' directory.
)

if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

:: ============================================================================
:: 1. SMART COMPILE
:: ============================================================================
echo.
echo [1/3] Checking for changes...
echo ------------------------------------------

set NEEDS_RECOMPILE=false

:: If the marker file doesn't exist, we must compile
if not exist "%LAST_BUILD_MARKER%" (
    set NEEDS_RECOMPILE=true
) else (
    :: Check if any .java file is newer than our last build marker
    for /f "delims=" %%i in ('xcopy /D /L /S "%SRC_DIR%\*.java" "%LAST_BUILD_MARKER%" ^| findstr /C:"File(s)"') do (
        set "FILES_CHANGED=%%i"
    )
    :: If xcopy found files to copy (in list mode), it means they are newer
    if not "!FILES_CHANGED!"=="0 File(s)" set NEEDS_RECOMPILE=true
)

if "%NEEDS_RECOMPILE%"=="true" (
    echo Changes detected. Compiling...
    dir /s /b "%SRC_DIR%\*.java" > sources.txt
    "!JAVAC_CMD!" -cp "%CLASSPATH%" -d "%OUT_DIR%" @sources.txt
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Compilation failed!
        del sources.txt
        pause
        exit /b 1
    )
    del sources.txt
    echo. > "%LAST_BUILD_MARKER%"
    echo Compilation successful.
) else (
    echo Everything up-to-date. Skipping compilation.
)

:: ============================================================================
:: 2. COPY RESOURCES
:: ============================================================================
echo.
echo [2/3] Updating Data Resources...
echo ------------------------------------------
if not exist "%OUT_DIR%\inventory\data" mkdir "%OUT_DIR%\inventory\data"
:: /D only copies if source is newer than destination
xcopy /Y /S /I /D "%SRC_DIR%\inventory\data\*.csv" "%OUT_DIR%\inventory\data" > nul
echo Resources updated.

:: ============================================================================
:: 3. RUN
:: ============================================================================
echo.
echo [3/3] Running Application...
echo ------------------------------------------
"!JAVA_CMD!" -cp "%CLASSPATH%" %MAIN_CLASS%

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Application execution failed.
)

echo.
pause
endlocal