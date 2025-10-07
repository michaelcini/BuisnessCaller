import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../services/enhanced_super_log_service.dart';

class EnhancedLogScreen extends StatefulWidget {
  const EnhancedLogScreen({Key? key}) : super(key: key);

  @override
  State<EnhancedLogScreen> createState() => _EnhancedLogScreenState();
}

class _EnhancedLogScreenState extends State<EnhancedLogScreen> {
  final TextEditingController _searchController = TextEditingController();
  final ScrollController _scrollController = ScrollController();
  bool _isAutoScroll = true;
  bool _isLogcatEnabled = false;
  bool _isSystemEventsEnabled = false;

  @override
  void initState() {
    super.initState();
    _initializeLogging();
  }

  Future<void> _initializeLogging() async {
    final logService = Provider.of<EnhancedSuperLogService>(context, listen: false);
    await logService.startLogcat();
    await logService.startSystemEvents();
    setState(() {
      _isLogcatEnabled = true;
      _isSystemEventsEnabled = true;
    });
  }

  @override
  void dispose() {
    _searchController.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Enhanced Log Viewer'),
        backgroundColor: Colors.deepPurple,
        foregroundColor: Colors.white,
        actions: [
          IconButton(
            icon: Icon(_isAutoScroll ? Icons.pause : Icons.play_arrow),
            onPressed: () {
              setState(() {
                _isAutoScroll = !_isAutoScroll;
              });
            },
            tooltip: _isAutoScroll ? 'Pause Auto-scroll' : 'Resume Auto-scroll',
          ),
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () {
              Provider.of<EnhancedSuperLogService>(context, listen: false).getLogStats();
            },
            tooltip: 'Refresh Stats',
          ),
          PopupMenuButton<String>(
            onSelected: (value) {
              switch (value) {
                case 'clear':
                  Provider.of<EnhancedSuperLogService>(context, listen: false).clearLogs();
                  break;
                case 'export':
                  _exportLogs();
                  break;
                case 'toggle_logcat':
                  _toggleLogcat();
                  break;
                case 'toggle_system_events':
                  _toggleSystemEvents();
                  break;
              }
            },
            itemBuilder: (context) => [
              const PopupMenuItem(
                value: 'clear',
                child: Row(
                  children: [
                    Icon(Icons.clear_all),
                    SizedBox(width: 8),
                    Text('Clear Logs'),
                  ],
                ),
              ),
              const PopupMenuItem(
                value: 'export',
                child: Row(
                  children: [
                    Icon(Icons.download),
                    SizedBox(width: 8),
                    Text('Export Logs'),
                  ],
                ),
              ),
              const PopupMenuItem(
                value: 'toggle_logcat',
                child: Row(
                  children: [
                    Icon(Icons.terminal),
                    SizedBox(width: 8),
                    Text('Toggle Logcat'),
                  ],
                ),
              ),
              const PopupMenuItem(
                value: 'toggle_system_events',
                child: Row(
                  children: [
                    Icon(Icons.event),
                    SizedBox(width: 8),
                    Text('Toggle System Events'),
                  ],
                ),
              ),
            ],
          ),
        ],
      ),
      body: Column(
        children: [
          _buildFilterBar(),
          _buildStatsBar(),
          Expanded(child: _buildLogList()),
        ],
      ),
    );
  }

  Widget _buildFilterBar() {
    return Container(
      padding: const EdgeInsets.all(8.0),
      color: Colors.grey[100],
      child: Column(
        children: [
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: _searchController,
                  decoration: const InputDecoration(
                    hintText: 'Search logs...',
                    prefixIcon: Icon(Icons.search),
                    border: OutlineInputBorder(),
                    contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                  ),
                  onChanged: (value) {
                    Provider.of<EnhancedSuperLogService>(context, listen: false)
                        .setSearchQuery(value);
                  },
                ),
              ),
              const SizedBox(width: 8),
              _buildFilterDropdown('Level', 'levelFilter', [
                'ALL', 'VERBOSE', 'DEBUG', 'INFO', 'WARNING', 'ERROR', 'FATAL'
              ]),
              const SizedBox(width: 8),
              _buildFilterDropdown('Source', 'sourceFilter', [
                'ALL', 'APP', 'LOGCAT', 'SYSTEM'
              ]),
            ],
          ),
          const SizedBox(height: 8),
          Row(
            children: [
              _buildStatusChip('Logcat', _isLogcatEnabled, Colors.blue),
              const SizedBox(width: 8),
              _buildStatusChip('System Events', _isSystemEventsEnabled, Colors.green),
              const Spacer(),
              TextButton(
                onPressed: () {
                  Provider.of<EnhancedSuperLogService>(context, listen: false).clearFilters();
                  _searchController.clear();
                },
                child: const Text('Clear Filters'),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildFilterDropdown(String label, String filterType, List<String> options) {
    return Consumer<EnhancedSuperLogService>(
      builder: (context, logService, child) {
        String currentValue;
        void Function(String?)? onChanged;
        
        if (filterType == 'levelFilter') {
          currentValue = logService.levelFilter;
          onChanged = (String? value) {
            if (value != null) logService.setLevelFilter(value);
          };
        } else {
          currentValue = logService.sourceFilter;
          onChanged = (String? value) {
            if (value != null) logService.setSourceFilter(value);
          };
        }

        return DropdownButton<String>(
          value: currentValue,
          hint: Text(label),
          onChanged: onChanged,
          items: options.map((option) => DropdownMenuItem(
            value: option,
            child: Text(option),
          )).toList(),
        );
      },
    );
  }

  Widget _buildStatusChip(String label, bool isEnabled, Color color) {
    return Chip(
      label: Text(label),
      backgroundColor: isEnabled ? color.withOpacity(0.2) : Colors.grey[300],
      labelStyle: TextStyle(
        color: isEnabled ? color : Colors.grey[600],
        fontWeight: FontWeight.bold,
      ),
    );
  }

  Widget _buildStatsBar() {
    return Consumer<EnhancedSuperLogService>(
      builder: (context, logService, child) {
        final stats = logService.logStats;
        final systemStats = logService.systemEventStats;
        
        return Container(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          color: Colors.blue[50],
          child: Row(
            children: [
              Text('App Logs: ${stats['totalLogs'] ?? 0}'),
              const SizedBox(width: 16),
              Text('Logcat: ${stats['totalLogs'] ?? 0}'),
              const SizedBox(width: 16),
              Text('System Events: ${systemStats['totalEvents'] ?? 0}'),
              const Spacer(),
              if (logService.isLogcatEnabled)
                const Icon(Icons.terminal, color: Colors.green, size: 16),
              if (logService.isSystemEventsEnabled)
                const Icon(Icons.event, color: Colors.green, size: 16),
            ],
          ),
        );
      },
    );
  }

  Widget _buildLogList() {
    return Consumer<EnhancedSuperLogService>(
      builder: (context, logService, child) {
        final logs = logService.filteredLogs;
        
        if (logs.isEmpty) {
          return const Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(Icons.info_outline, size: 64, color: Colors.grey),
                SizedBox(height: 16),
                Text('No logs available', style: TextStyle(fontSize: 18, color: Colors.grey)),
                Text('Enable logcat or system events to see logs', style: TextStyle(color: Colors.grey)),
              ],
            ),
          );
        }

        return ListView.builder(
          controller: _scrollController,
          itemCount: logs.length,
          itemBuilder: (context, index) {
            final log = logs[index];
            return _buildLogItem(log);
          },
        );
      },
    );
  }

  Widget _buildLogItem(SuperLogEntry log) {
    Color levelColor;
    IconData levelIcon;
    
    switch (log.level) {
      case 'VERBOSE':
        levelColor = Colors.grey;
        levelIcon = Icons.notes;
        break;
      case 'DEBUG':
        levelColor = Colors.blue;
        levelIcon = Icons.bug_report;
        break;
      case 'INFO':
        levelColor = Colors.green;
        levelIcon = Icons.info;
        break;
      case 'WARNING':
        levelColor = Colors.orange;
        levelIcon = Icons.warning;
        break;
      case 'ERROR':
        levelColor = Colors.red;
        levelIcon = Icons.error;
        break;
      case 'FATAL':
        levelColor = Colors.purple;
        levelIcon = Icons.dangerous;
        break;
      default:
        levelColor = Colors.grey;
        levelIcon = Icons.help;
    }

    Color sourceColor;
    switch (log.source) {
      case 'APP':
        sourceColor = Colors.blue;
        break;
      case 'LOGCAT':
        sourceColor = Colors.green;
        break;
      case 'SYSTEM':
        sourceColor = Colors.orange;
        break;
      default:
        sourceColor = Colors.grey;
    }

    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
      child: ListTile(
        leading: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(levelIcon, color: levelColor, size: 16),
            Container(
              width: 4,
              height: 4,
              decoration: BoxDecoration(
                color: sourceColor,
                shape: BoxShape.circle,
              ),
            ),
          ],
        ),
        title: Text(
          log.message,
          style: const TextStyle(fontFamily: 'monospace', fontSize: 12),
          maxLines: 2,
          overflow: TextOverflow.ellipsis,
        ),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              '${log.formattedTime} [${log.source}] [${log.level}] [${log.tag}]',
              style: TextStyle(
                fontFamily: 'monospace',
                fontSize: 10,
                color: Colors.grey[600],
              ),
            ),
            if (log.details != null)
              Text(
                'Details: ${log.details}',
                style: const TextStyle(fontSize: 10, color: Colors.grey),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            if (log.phoneNumber != null)
              Text(
                'Phone: ${log.phoneNumber}',
                style: const TextStyle(fontSize: 10, color: Colors.blue),
              ),
          ],
        ),
        onTap: () => _showLogDetails(log),
        dense: true,
      ),
    );
  }

  void _showLogDetails(SuperLogEntry log) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Log Details - ${log.level}'),
        content: SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              Text('Time: ${log.formattedTime}'),
              Text('Source: ${log.source}'),
              Text('Level: ${log.level}'),
              Text('Tag: ${log.tag}'),
              const SizedBox(height: 8),
              const Text('Message:', style: TextStyle(fontWeight: FontWeight.bold)),
              Text(log.message, style: const TextStyle(fontFamily: 'monospace')),
              if (log.details != null) ...[
                const SizedBox(height: 8),
                const Text('Details:', style: TextStyle(fontWeight: FontWeight.bold)),
                Text(log.details!, style: const TextStyle(fontFamily: 'monospace')),
              ],
              if (log.phoneNumber != null) ...[
                const SizedBox(height: 8),
                const Text('Phone:', style: TextStyle(fontWeight: FontWeight.bold)),
                Text(log.phoneNumber!, style: const TextStyle(fontFamily: 'monospace')),
              ],
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Close'),
          ),
        ],
      ),
    );
  }

  void _exportLogs() {
    final logService = Provider.of<EnhancedSuperLogService>(context, listen: false);
    final exportedLogs = logService.exportLogs();
    
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Export Logs'),
        content: SizedBox(
          width: double.maxFinite,
          height: 400,
          child: SingleChildScrollView(
            child: Text(
              exportedLogs,
              style: const TextStyle(fontFamily: 'monospace', fontSize: 10),
            ),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Close'),
          ),
        ],
      ),
    );
  }

  void _toggleLogcat() async {
    final logService = Provider.of<EnhancedSuperLogService>(context, listen: false);
    if (_isLogcatEnabled) {
      await logService.stopLogcat();
    } else {
      await logService.startLogcat();
    }
    setState(() {
      _isLogcatEnabled = !_isLogcatEnabled;
    });
  }

  void _toggleSystemEvents() async {
    final logService = Provider.of<EnhancedSuperLogService>(context, listen: false);
    if (_isSystemEventsEnabled) {
      await logService.stopSystemEvents();
    } else {
      await logService.startSystemEvents();
    }
    setState(() {
      _isSystemEventsEnabled = !_isSystemEventsEnabled;
    });
  }
}