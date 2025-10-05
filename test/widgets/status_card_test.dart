import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:call_blocker/widgets/status_card.dart';

void main() {
  group('StatusCard Widget Tests', () {
    testWidgets('should display enabled status correctly', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: StatusCard(
              isEnabled: true,
              isServiceRunning: true,
            ),
          ),
        ),
      );

      expect(find.text('Status'), findsOneWidget);
      expect(find.text('Enabled'), findsOneWidget);
      expect(find.text('Running'), findsOneWidget);
      expect(find.byIcon(Icons.check_circle), findsOneWidget);
    });

    testWidgets('should display disabled status correctly', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: StatusCard(
              isEnabled: false,
              isServiceRunning: false,
            ),
          ),
        ),
      );

      expect(find.text('Status'), findsOneWidget);
      expect(find.text('Disabled'), findsOneWidget);
      expect(find.text('Stopped'), findsOneWidget);
      expect(find.byIcon(Icons.cancel), findsOneWidget);
    });

    testWidgets('should show monitoring message when enabled and running', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: StatusCard(
              isEnabled: true,
              isServiceRunning: true,
            ),
          ),
        ),
      );

      expect(find.textContaining('Call Blocker is actively monitoring'), findsOneWidget);
    });
  });
}

