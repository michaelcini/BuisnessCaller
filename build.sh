#!/bin/bash

# Call Blocker Build Script
# This script helps build and run the Call Blocker Android app

echo "🚀 Call Blocker Build Script"
echo "=============================="

# Check if Flutter is installed
if ! command -v flutter &> /dev/null; then
    echo "❌ Flutter is not installed. Please install Flutter first."
    echo "Visit: https://flutter.dev/docs/get-started/install"
    exit 1
fi

# Check Flutter version
echo "📱 Flutter version:"
flutter --version

# Get dependencies
echo "📦 Getting Flutter dependencies..."
flutter pub get

# Check for any issues
echo "🔍 Checking for issues..."
flutter analyze

# Build APK
echo "🔨 Building APK..."
flutter build apk --release

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo "📱 APK location: build/app/outputs/flutter-apk/app-release.apk"
    echo ""
    echo "🎉 Call Blocker is ready to install!"
    echo ""
    echo "To install on your device:"
    echo "1. Enable 'Install from unknown sources' in your device settings"
    echo "2. Transfer the APK to your device"
    echo "3. Install the APK"
    echo ""
    echo "⚠️  Important: Grant all required permissions when prompted!"
else
    echo "❌ Build failed. Please check the error messages above."
    exit 1
fi
