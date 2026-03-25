# Build and Run Instructions

This project includes a **smart** Windows batch script (`build.bat`) that attempts to automatically find your Java Development Kit (JDK) and run the application without manual configuration.

## Prerequisites

1.  **Java Development Kit (JDK) 11 or higher** must be installed.
    *   If you don't have it, download and install it from Oracle.
    *   **Note:** You need the **JDK**, not just the JRE. The JRE only runs Java programs; the JDK includes the compiler (`javac`) needed to build them.

## How to Run (The Easy Way)

1.  Double-click `build.bat` in the project root folder.
2.  **That's it!** The script will:
    *   Automatically search for your JDK installation in common locations.
    *   Compile the source code.
    *   Launch the application.

## Troubleshooting

### "javac is not recognized..." or "Could not find a JDK installation"
If the script fails with this error, it means it couldn't find your JDK in the standard installation folders (`C:\Program Files\Java`, etc.).

**Solution 1: Reinstall JDK to Default Location**
Uninstall your current JDK and reinstall it, accepting the default installation path (usually `C:\Program Files\Java\...`).

**Solution 2: Manually Set JAVA_HOME (Advanced)**
1.  Find where your JDK is installed (look for a folder containing `bin\javac.exe`).
2.  Open `build.bat` in a text editor (Notepad).
3.  Find the section labeled `:: JAVA DETECTION`.
4.  Add a line at the top of that section:
    ```bat
    set JAVA_HOME=C:\Path\To\Your\JDK
    set PATH=%JAVA_HOME%\bin;%PATH%
    goto :FOUND_JAVAC
    ```

### "Compilation failed!"
This means there are errors in the Java code itself (e.g., syntax errors). The script will pause and display the error messages.
*   Check the error output in the console window.
*   Open the corresponding `.java` file in your IDE to fix the issue.

### "File not found" errors
Ensure you are running the script from the **root** of the project folder (the folder containing `src` and `build.bat`). Do not move the script to another folder.
