@echo off
echo ==========================================
echo Building DEBUG APK for Simulator/Testing
echo ==========================================

where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Java is not installed or not in PATH.
    echo Please install JDK 17 or open this project in Android Studio.
    echo Download: https://adoptium.net/temurin/releases/?version=17
    pause
    exit /b 1
)

echo Java found. Starting Gradle build...
echo.

if not exist "gradlew.bat" (
    echo [ERROR] gradlew.bat not found. Make sure you are in the project root.
    pause
    exit /b 1
)

call gradlew.bat clean assembleDebug --stacktrace

if %errorlevel% equ 0 (
    echo.
    echo ==========================================
    echo SUCCESS! Debug APK built.
    echo Location:
    echo   app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo You can drag this APK onto your emulator.
    echo ==========================================
) else (
    echo.
    echo [ERROR] Build failed. Check the output above.
)

pause