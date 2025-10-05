@echo off
REM Call Blocker Testing Script for Windows
REM This script helps test the Call Blocker Android app

echo 🧪 Call Blocker Testing Script
echo ===============================

REM Check if Flutter is installed
where flutter >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Flutter is not installed. Please install Flutter first.
    echo Visit: https://flutter.dev/docs/get-started/install
    pause
    exit /b 1
)

echo 📱 Flutter version:
flutter --version

echo.
echo 🔍 Running Tests...
echo ===================

REM Run unit tests
echo 📋 Running Unit Tests...
flutter test

if %ERRORLEVEL% EQU 0 (
    echo ✅ Unit tests passed!
) else (
    echo ❌ Unit tests failed!
    pause
    exit /b 1
)

echo.
echo 🔍 Running Analysis...
echo ======================

REM Run analysis
flutter analyze

if %ERRORLEVEL% EQU 0 (
    echo ✅ Analysis passed!
) else (
    echo ⚠️  Analysis found issues (check above)
)

echo.
echo 🚀 Testing Options:
echo ===================
echo 1. Run on connected device: flutter run
echo 2. Run on emulator: flutter run
echo 3. Build debug APK: flutter build apk --debug
echo 4. Build release APK: flutter build apk --release
echo.
echo 📱 Manual Testing Checklist:
echo - [ ] App launches without crashes
echo - [ ] Permission requests work
echo - [ ] Settings can be configured
echo - [ ] Business hours can be set
echo - [ ] Custom messages work
echo - [ ] Service starts successfully
echo - [ ] Call blocking works (outside hours)
echo - [ ] SMS auto-reply works (outside hours)
echo.

set /p choice="Choose testing option (1-4) or press Enter to exit: "

if "%choice%"=="1" (
    echo 🚀 Running on connected device...
    flutter run
) else if "%choice%"=="2" (
    echo 🚀 Running on emulator...
    flutter run
) else if "%choice%"=="3" (
    echo 🔨 Building debug APK...
    flutter build apk --debug
    echo ✅ Debug APK built: build\app\outputs\flutter-apk\app-debug.apk
) else if "%choice%"=="4" (
    echo 🔨 Building release APK...
    flutter build apk --release
    echo ✅ Release APK built: build\app\outputs\flutter-apk\app-release.apk
)

echo.
echo 🎉 Testing complete!
pause
