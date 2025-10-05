# Call Blocker Testing Guide

## 🧪 Testing Methods

### **1. Unit Testing**
Test individual components and business logic:

```bash
# Run unit tests
flutter test

# Run tests with coverage
flutter test --coverage
```

### **2. Widget Testing**
Test UI components:

```bash
# Run widget tests
flutter test test/widget_test.dart
```

### **3. Manual Testing on Device/Emulator**

#### **Setup for Testing:**
1. **Connect Android Device** or **Start Emulator**
2. **Enable Developer Options** on your device
3. **Enable USB Debugging**
4. **Install the app**

#### **Testing Commands:**
```bash
# Install dependencies
flutter pub get

# Run on connected device
flutter run

# Run in debug mode
flutter run --debug

# Run in release mode
flutter run --release

# Build APK for testing
flutter build apk --debug
```

### **4. Specific Feature Testing**

#### **Permission Testing:**
- Test permission requests on first launch
- Test permission denial scenarios
- Test battery optimization exemption

#### **Call Blocking Testing:**
- Set business hours (e.g., 9 AM - 5 PM)
- Test outside business hours (call should be blocked)
- Test during business hours (call should go through)

#### **SMS Testing:**
- Send SMS to your device outside business hours
- Verify auto-reply is sent
- Test custom message functionality

#### **Schedule Testing:**
- Test different schedules for each day
- Test overnight schedules (e.g., 10 PM - 6 AM)
- Test disabled days

#### **Background Service Testing:**
- Test service starts on boot
- Test service continues running in background
- Test notification appears

### **5. Testing Scenarios**

#### **Happy Path Testing:**
1. Install app
2. Grant all permissions
3. Set business hours (9 AM - 5 PM)
4. Enable call blocking and SMS reply
5. Test call blocking outside hours
6. Test SMS auto-reply outside hours

#### **Edge Case Testing:**
1. Test with no permissions granted
2. Test with partial permissions
3. Test battery optimization disabled
4. Test app restart scenarios
5. Test schedule changes while running

#### **Error Handling Testing:**
1. Test network connectivity issues
2. Test low memory scenarios
3. Test app crashes and recovery
4. Test permission revocation

### **6. Performance Testing**

```bash
# Profile the app
flutter run --profile

# Check performance
flutter run --trace-startup
```

### **7. Automated Testing Script**

Create automated tests for common scenarios:

```bash
# Run all tests
flutter test

# Run specific test files
flutter test test/models/
flutter test test/services/
flutter test test/widgets/
```

## 🔧 **Testing Tools**

### **Flutter Inspector**
- Use `flutter inspector` to debug UI
- Inspect widget tree
- Check layout issues

### **Debug Console**
- Use `flutter logs` to see app logs
- Monitor service status
- Debug permission issues

### **Device Logs**
```bash
# View device logs
adb logcat

# Filter for your app
adb logcat | grep "CallBlocker"
```

## 📱 **Device Testing Checklist**

### **Pre-Testing Setup:**
- [ ] Flutter installed and configured
- [ ] Android device/emulator connected
- [ ] Developer options enabled
- [ ] USB debugging enabled
- [ ] App installed successfully

### **Feature Testing Checklist:**
- [ ] App launches without crashes
- [ ] Permission requests work correctly
- [ ] Settings can be configured
- [ ] Business hours can be set
- [ ] Custom messages can be entered
- [ ] Service starts successfully
- [ ] Notification appears
- [ ] Call blocking works (outside hours)
- [ ] SMS auto-reply works (outside hours)
- [ ] App works after device restart
- [ ] Battery optimization exemption works

### **Error Testing Checklist:**
- [ ] App handles permission denial gracefully
- [ ] App handles service failures
- [ ] App handles invalid schedule data
- [ ] App handles low memory

## 🐛 **Common Testing Issues & Solutions**

### **Issue: App won't install**
**Solution:** Check device compatibility, enable unknown sources

### **Issue: Permissions not granted**
**Solution:** Go to Settings → Apps → Call Blocker → Permissions

### **Issue: Service not starting**
**Solution:** Check battery optimization, restart app

### **Issue: Calls not being blocked**
**Solution:** Verify phone permission, check business hours

### **Issue: SMS not sending**
**Solution:** Verify SMS permission, check message content

## 📊 **Testing Results Template**

```
Test Date: ___________
Device: ___________
Android Version: ___________

Feature Tests:
- [ ] App Installation
- [ ] Permission Requests
- [ ] Settings Configuration
- [ ] Call Blocking
- [ ] SMS Auto-Reply
- [ ] Background Service
- [ ] Battery Optimization

Issues Found:
1. ___________
2. ___________
3. ___________

Overall Result: [ ] PASS [ ] FAIL
Notes: ___________
```
