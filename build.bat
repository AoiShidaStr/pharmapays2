@echo off
REM PharmaPays Build Script for Windows Command Prompt

echo.
echo ========================================
echo   PharmaPays Build Script
echo ========================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] Maven found in PATH
    set MAVEN_CMD=mvn
) else (
    echo [INFO] Maven not in PATH, checking for wrapper...
    if exist mvnw.cmd (
        echo [OK] Found Maven wrapper (mvnw.cmd)
        set MAVEN_CMD=mvnw.cmd
    ) else (
        echo [ERROR] Neither 'mvn' nor Maven wrapper found!
        echo Please install Maven or ensure Maven wrapper exists.
        exit /b 1
    )
)

echo.
echo Running: %MAVEN_CMD% clean install
echo.

REM Run Maven clean install
call %MAVEN_CMD% clean install

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo [SUCCESS] Build completed successfully!
    echo ========================================
    echo.
    echo To run the application, execute:
    echo   %MAVEN_CMD% javafx:run
    echo.
) else (
    echo.
    echo ========================================
    echo [ERROR] Build failed with exit code: %ERRORLEVEL%
    echo ========================================
    exit /b %ERRORLEVEL%
)
