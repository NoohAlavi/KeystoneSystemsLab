@echo off
setlocal enabledelayedexpansion

:: ============================================================================
:: JAVA DETECTION
:: ============================================================================

:: Check if javac is already in PATH
where javac >nul 2>nul
if %errorlevel% equ 0 goto :FOUND_JAVAC

echo 'javac' command not found in global PATH.
echo Attempting to locate JDK automatically...

:: Try to find JDK in C:\Program Files\Java
for /d %%i in ("C:\Program Files\Java\jdk*") do (
    if exist "%%i\bin\javac.exe" (
        set "JAVA_HOME=%%i"
        set "PATH=%%i\bin;%PATH%"
        echo [INFO] Found JDK at: %%i
        goto :FOUND_JAVAC
    )
)

:: Try to find JDK in C:\Program Files (x86)\Java
for /d %%i in ("C:\Program Files (x86)\Java\jdk*") do (
    if exist "%%i\bin\javac.exe" (
        set "JAVA_HOME=%%i"
        set "PATH=%%i\bin;%PATH%"
        echo [INFO] Found JDK at: %%i
        goto :FOUND_JAVAC
    )
)

:: Try to find OpenJDK (common with some installs)
for /d %%i in ("C:\Program Files\OpenJDK\jdk*") do (
    if exist "%%i\bin\javac.exe" (
        set "JAVA_HOME=%%i"
        set "PATH=%%i\bin;%PATH%"
        echo [INFO] Found JDK at: %%i
        goto :FOUND_JAVAC
    )
)

:: If we get here, we couldn't find it
echo.
echo [ERROR] Could not find a Java Development Kit (JDK) installation.
echo.
echo Please ensure you have installed the JDK (not just the JRE).
echo If you have installed it to a custom location, please edit this
echo file (build.bat) and manually set the path to your bin folder.
pause
exit /b 1

:FOUND_JAVAC

:: ============================================================================
:: CONFIGURATION
:: ============================================================================
set SRC_DIR=src
set OUT_DIR=out\production\KeystoneSystemsLab
set MAIN_CLASS=inventory.Main

:: Create output directory if it doesn't exist
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

:: ============================================================================
:: 1. COMPILE
:: ============================================================================
echo.
echo [1/3] Compiling Project...
echo ------------------------------------------
:: Find java files
dir /s /b "%SRC_DIR%\*.java" > sources.txt

javac -d "%OUT_DIR%" @sources.txt
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed!
    echo Check your Java code for errors.
    del sources.txt
    pause
    exit /b 1
)
del sources.txt
echo Compilation successful.

:: ============================================================================
:: 2. COPY RESOURCES (CSV files)
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
java -cp "%OUT_DIR%" %MAIN_CLASS%

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Application crashed or failed to start.
)

echo.
pause
endlocal
