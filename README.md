# Call Blocker Android App

A complete Android application that automatically blocks calls and sends SMS replies outside of configured business hours.

## Features

- **Automatic Call Blocking**: Blocks incoming calls outside business hours
- **SMS Auto-Reply**: Sends automatic SMS replies when outside business hours
- **Customizable Business Hours**: Set different hours for each day of the week
- **Custom SMS Messages**: Personalize your auto-reply messages
- **Background Service**: Runs continuously using foreground service
- **Battery Optimization**: Handles battery optimization exemption
- **Material 3 Design**: Modern, clean UI using Flutter
- **Privacy-Focused**: All data stored locally, no external data collection

## Technical Implementation

### Architecture
- **MVVM Pattern**: Clean separation of concerns
- **Flutter**: Cross-platform mobile development
- **Provider**: State management
- **SharedPreferences**: Local data persistence
- **Foreground Service**: Background execution compliance
- **Material 3**: Modern design system

### Key Components

#### Data Layer
- `AppSettings` entity for storing user preferences
- `AppSettingsRepository` for data access using SharedPreferences

#### Domain Layer
- `WeeklySchedule` model for business logic
- `AppSettingsUseCase` for business operations

#### Service Layer
- `CallBlockerService` foreground service
- `PhoneStateReceiver` for call detection
- `SMSReceiver` for SMS handling
- `BootReceiver` for auto-start functionality

#### UI Layer
- `MainActivity` with main controls
- `SettingsScreen` for configuration
- `AboutScreen` and `PermissionScreen`
- Custom Flutter widgets

## Permissions Required

- `READ_PHONE_STATE`: Detect incoming calls
- `RECEIVE_SMS`: Receive SMS messages
- `SEND_SMS`: Send automatic replies
- `POST_NOTIFICATIONS`: Foreground service notifications
- `READ_CALL_LOG`: Call detection (Android 10+)
- `FOREGROUND_SERVICE`: Background service execution
- `RECEIVE_BOOT_COMPLETED`: Auto-start on boot
- `WAKE_LOCK`: Keep device awake for service
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`: Battery optimization exemption
- `SYSTEM_ALERT_WINDOW`: System-level call blocking

## Installation

1. Clone the repository
2. Install Flutter dependencies:
   ```bash
   flutter pub get
   ```
3. Build and run the app:
   ```bash
   flutter run
   ```

## Usage

1. **First Launch**: The app will request all necessary permissions
2. **Battery Optimization**: Grant battery optimization exemption for background operation
3. **Configure Schedule**: Set business hours for each day of the week
4. **Customize Message**: Set your preferred auto-reply message
5. **Enable Service**: Toggle the main switch to start call blocking

## Configuration

### Business Hours
- Set different start and end times for each day
- Enable/disable specific days
- Support for overnight schedules (e.g., 22:00 to 06:00)

### Custom Messages
- Default professional message included
- Custom message editor with character limit
- Quick preset buttons for common messages

### Permissions
- Automatic permission request on first launch
- Manual permission management screen
- Battery optimization exemption handling

## Privacy

This app is designed with privacy in mind:
- All settings stored locally on device
- No data collection or transmission
- No external service dependencies
- Open source and transparent

## Troubleshooting

### Common Issues

1. **Service Not Running**
   - Check if all permissions are granted
   - Verify battery optimization exemption
   - Restart the app

2. **Calls Not Blocked**
   - Ensure phone permission is granted
   - Check if business hours are configured correctly
   - Verify the service is running

3. **SMS Not Sent**
   - Check SMS permissions
   - Verify custom message is set
   - Check if outside business hours

### Support

For issues or suggestions, please contact support@callblocker.app

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

# Trigger fresh build

<!-- Build trigger Sun Oct  5 10:00:08 PM UTC 2025 -->
