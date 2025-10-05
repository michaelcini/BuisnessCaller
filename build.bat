@echo off
REM Call Blocker Build Script for Windows
REM This script helps build and run the Call Blocker Android app

echo 🚀 Call Blocker Build Script
echo ==============================

REM Check if Flutter is installed
where flutter >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Flutter is not installed. Please install Flutter first.
    echo Visit: https://flutter.dev/docs/get-started/install
    pause
    exit /b 1
)

REM Check Flutter version
echo 📱 Flutter version:
flutter --version

REM Get dependencies
echo 📦 Getting Flutter dependencies...
flutter pub get

REM Check for any issues
echo 🔍 Checking for issues...
flutter analyze

REM Build APK
echo 🔨 Building APK...
flutter build apk --release

REM Check if build was successful
if %ERRORLEVEL% EQU 0 (
    echo ✅ Build successful!
    echo 📱 APK location: build\app\outputs\flutter-apk\app-release.apk
    echo.
    echo 🎉 Call Blocker is ready to install!
    echo.
    echo To install on your device:
    echo 1. Enable 'Install from unknown sources' in your device settings
    echo 2. Transfer the APK to your device
    echo 3. Install the APK
    echo.
    echo ⚠️  Important: Grant all required permissions when prompted!
) else (
    echo ❌ Build failed. Please check the error messages above.
    pause
    exit /b 1
)

pause

