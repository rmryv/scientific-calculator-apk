@echo off
echo ==========================================
echo Building RELEASE AAB (for Play Store)
echo ==========================================

where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Java is not installed or not in PATH.
    echo Please install JDK 17 or use Android Studio.
    pause
    exit /b 1
)

if not exist "keystore.properties" (
    echo [WARNING] keystore.properties not found!
    echo You need to set up signing first.
    echo.
    echo 1. Copy keystore.properties.example to keystore.properties
    echo 2. Edit it with your keystore details
    echo 3. Or use Android Studio "Generate Signed Bundle"
    echo.
    pause
)

echo Starting release build...
call gradlew.bat clean bundleRelease --stacktrace

if %errorlevel% equ 0 (
    echo.
    echo ==========================================
    echo SUCCESS! Release AAB built.
    echo Location:
    echo   app\build\outputs\bundle\release\app-release.aab
    echo.
    echo Upload this .aab file to Google Play Console.
    echo ==========================================
) else (
    echo.
    echo [ERROR] Build failed. See messages above.
)

pause