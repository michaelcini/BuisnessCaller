import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'providers/app_settings_provider.dart';
import 'screens/home_screen.dart';
import 'screens/settings_screen.dart';
import 'screens/about_screen.dart';
import 'screens/permission_screen.dart';
import 'screens/privacy_policy_screen.dart';
import 'screens/log_screen.dart';
import 'screens/setup_screen.dart';
import 'screens/super_log_screen.dart';
import 'screens/enhanced_log_screen.dart';
import 'services/call_blocker_service.dart';
import 'services/log_service.dart';
import 'services/super_log_service.dart';
import 'services/enhanced_super_log_service.dart';
import 'domain/app_settings_use_case.dart';
import 'repositories/app_settings_repository.dart';
import 'utils/permission_manager.dart';
import 'utils/battery_optimization_manager.dart';

void main() {
  runApp(const CallBlockerApp());
}

class CallBlockerApp extends StatelessWidget {
  const CallBlockerApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        Provider<AppSettingsRepository>(
          create: (_) => AppSettingsRepository(),
        ),
        Provider<AppSettingsUseCase>(
          create: (context) => AppSettingsUseCase(
            context.read<AppSettingsRepository>(),
          ),
        ),
        ChangeNotifierProvider<AppSettingsProvider>(
          create: (context) => AppSettingsProvider(
            context.read<AppSettingsUseCase>(),
          ),
        ),
        Provider<CallBlockerService>(
          create: (context) {
            final service = CallBlockerService(
              context.read<AppSettingsUseCase>(),
            );
            // Connect SuperLog service
            WidgetsBinding.instance.addPostFrameCallback((_) {
              service.setSuperLogService(context.read<SuperLogService>());
            });
            return service;
          },
        ),
        Provider<LogService>(
          create: (_) => LogService(),
        ),
        ChangeNotifierProvider<SuperLogService>(
          create: (_) => SuperLogService(),
        ),
        ChangeNotifierProvider<EnhancedSuperLogService>(
          create: (_) => EnhancedSuperLogService(),
        ),
      ],
      child: MaterialApp(
        title: 'Call Blocker',
        theme: ThemeData(
          useMaterial3: true,
          colorScheme: ColorScheme.fromSeed(
            seedColor: Colors.blue,
            brightness: Brightness.light,
          ),
          appBarTheme: const AppBarTheme(
            centerTitle: true,
            elevation: 0,
          ),
          cardTheme: CardTheme(
            elevation: 2,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(12),
            ),
          ),
          elevatedButtonTheme: ElevatedButtonThemeData(
            style: ElevatedButton.styleFrom(
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(8),
              ),
              padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
            ),
          ),
        ),
        darkTheme: ThemeData(
          useMaterial3: true,
          colorScheme: ColorScheme.fromSeed(
            seedColor: Colors.blue,
            brightness: Brightness.dark,
          ),
          appBarTheme: const AppBarTheme(
            centerTitle: true,
            elevation: 0,
          ),
          cardTheme: CardTheme(
            elevation: 2,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(12),
            ),
          ),
          elevatedButtonTheme: ElevatedButtonThemeData(
            style: ElevatedButton.styleFrom(
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(8),
              ),
              padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
            ),
          ),
        ),
        home: const MainScreen(),
        routes: {
          '/settings': (context) => const SettingsScreen(),
          '/about': (context) => const AboutScreen(),
          '/permissions': (context) => const PermissionScreen(),
          '/privacy': (context) => const PrivacyPolicyScreen(),
          '/logs': (context) => const LogScreen(),
          '/setup': (context) => const SetupScreen(),
            '/superlog': (context) => const SuperLogScreen(),
            '/enhancedlog': (context) => const EnhancedLogScreen(),
        },
      ),
    );
  }
}

class MainScreen extends StatefulWidget {
  const MainScreen({super.key});

  @override
  State<MainScreen> createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
  int _currentIndex = 0;

  final List<Widget> _screens = [
    const HomeScreen(),
    const SettingsScreen(),
    const AboutScreen(),
  ];

  @override
  void initState() {
    super.initState();
    _initializeServices();
    _checkPermissionsAndBatteryOptimization();
  }

  Future<void> _initializeServices() async {
    final logService = context.read<LogService>();
    await logService.initialize();
  }

  Future<void> _checkPermissionsAndBatteryOptimization() async {
    // Check permissions first
    final hasPermissions = await PermissionManager.checkAllPermissions();
    if (!hasPermissions) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        Navigator.pushNamed(context, '/permissions');
      });
      return;
    }

    // Check battery optimization
    await BatteryOptimizationManager.showBatteryOptimizationDialog(context);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: _screens[_currentIndex],
      bottomNavigationBar: NavigationBar(
        selectedIndex: _currentIndex,
        onDestinationSelected: (index) {
          setState(() {
            _currentIndex = index;
          });
        },
        destinations: const [
          NavigationDestination(
            icon: Icon(Icons.home_outlined),
            selectedIcon: Icon(Icons.home),
            label: 'Home',
          ),
          NavigationDestination(
            icon: Icon(Icons.settings_outlined),
            selectedIcon: Icon(Icons.settings),
            label: 'Settings',
          ),
          NavigationDestination(
            icon: Icon(Icons.info_outlined),
            selectedIcon: Icon(Icons.info),
            label: 'About',
          ),
        ],
      ),
    );
  }
}
