@echo off
setlocal enabledelayedexpansion

:: ============================================================================
:: JAVA DETECTION
:: ============================================================================

echo Detecting Java environment...

:: 1. Check if JAVA_HOME is already set and valid
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\javac.exe" (
        echo [INFO] Using JAVA_HOME: %JAVA_HOME%
        set "JDK_HOME=%JAVA_HOME%"
        goto :FOUND_JDK
    )
)

:: 2. Try to find JDK in standard locations
:: We look for the HIGHEST version number by sorting names in reverse order roughly
for /d %%i in ("C:\Program Files\Java\jdk*") do set "JDK_HOME=%%i"
if defined JDK_HOME (
    echo [INFO] Found JDK at: !JDK_HOME!
    goto :FOUND_JDK
)

for /d %%i in ("C:\Program Files (x86)\Java\jdk*") do set "JDK_HOME=%%i"
if defined JDK_HOME (
    echo [INFO] Found JDK at: !JDK_HOME!
    goto :FOUND_JDK
)

:: If we get here, we couldn't find a JDK
echo.
echo [ERROR] Could not find a JDK installation.
echo Please ensure JDK 11 or newer is installed.
pause
exit /b 1

:FOUND_JDK
set "JAVAC_CMD=!JDK_HOME!\bin\javac.exe"
set "JAVA_CMD=!JDK_HOME!\bin\java.exe"

:: ============================================================================
:: VERSION CHECK
:: ============================================================================
echo.
echo [INFO] Compiler Version:
"!JAVAC_CMD!" -version
echo.
echo [INFO] Runtime Version:
"!JAVA_CMD!" -version

:: ============================================================================
:: CONFIGURATION
:: ============================================================================
set SRC_DIR=src
set OUT_DIR=out\production\KeystoneSystemsLab
set MAIN_CLASS=inventory.Main

:: Create output directory
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

:: ============================================================================
:: 1. COMPILE
:: ============================================================================
echo.
echo [1/3] Compiling Project...
echo ------------------------------------------
dir /s /b "%SRC_DIR%\*.java" > sources.txt

"!JAVAC_CMD!" -d "%OUT_DIR%" @sources.txt
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed!
    del sources.txt
    pause
    exit /b 1
)
del sources.txt
echo Compilation successful.

:: ============================================================================
:: 2. COPY RESOURCES
:: ============================================================================
echo.
echo [2/3] Copying Data Resources...
echo ------------------------------------------
if not exist "%OUT_DIR%\inventory\data" mkdir "%OUT_DIR%\inventory\data"
xcopy /Y /S /I "%SRC_DIR%\inventory\data\*.csv" "%OUT_DIR%\inventory\data" > nul
echo Resources copied.

:: ============================================================================
:: 3. RUN
:: ============================================================================
echo.
echo [3/3] Running Application...
echo ------------------------------------------
echo.
"!JAVA_CMD!" -cp "%OUT_DIR%" %MAIN_CLASS%

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Application execution failed.
)

echo.
pause
endlocal
