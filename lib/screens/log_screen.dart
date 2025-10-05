import 'package:flutter/material.dart';
import '../models/log_entry.dart';
import '../services/log_service.dart';

class LogScreen extends StatefulWidget {
  const LogScreen({Key? key}) : super(key: key);

  @override
  State<LogScreen> createState() => _LogScreenState();
}

class _LogScreenState extends State<LogScreen> {
  final LogService _logService = LogService();
  LogLevel? _selectedLevel;
  String? _selectedPhoneNumber;
  List<LogEntry> _filteredLogs = [];

  @override
  void initState() {
    super.initState();
    _loadLogs();
  }

  void _loadLogs() {
    setState(() {
      _filteredLogs = _logService.logs;
    });
  }

  void _applyFilters() {
    setState(() {
      _filteredLogs = _logService.logs.where((log) {
        bool levelMatch = _selectedLevel == null || log.level == _selectedLevel;
        bool phoneMatch = _selectedPhoneNumber == null || log.phoneNumber == _selectedPhoneNumber;
        return levelMatch && phoneMatch;
      }).toList();
    });
  }

  void _clearLogs() async {
    await _logService.clearLogs();
    _loadLogs();
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Logs cleared')),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Call Blocker Logs'),
        backgroundColor: Colors.blue,
        foregroundColor: Colors.white,
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _loadLogs,
            tooltip: 'Refresh',
          ),
          IconButton(
            icon: const Icon(Icons.clear_all),
            onPressed: _clearLogs,
            tooltip: 'Clear Logs',
          ),
        ],
      ),
      body: Column(
        children: [
          _buildFilterBar(),
          Expanded(
            child: _filteredLogs.isEmpty
                ? const Center(
                    child: Text(
                      'No logs available',
                      style: TextStyle(fontSize: 16, color: Colors.grey),
                    ),
                  )
                : ListView.builder(
                    itemCount: _filteredLogs.length,
                    itemBuilder: (context, index) {
                      final log = _filteredLogs[index];
                      return _buildLogCard(log);
                    },
                  ),
          ),
        ],
      ),
    );
  }

  Widget _buildFilterBar() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.grey[100],
        border: Border(
          bottom: BorderSide(color: Colors.grey[300]!),
        ),
      ),
      child: Column(
        children: [
          Row(
            children: [
              Expanded(
                child: DropdownButtonFormField<LogLevel?>(
                  value: _selectedLevel,
                  decoration: const InputDecoration(
                    labelText: 'Filter by Level',
                    border: OutlineInputBorder(),
                    contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                  ),
                  items: [
                    const DropdownMenuItem<LogLevel?>(
                      value: null,
                      child: Text('All Levels'),
                    ),
                    ...LogLevel.values.map((level) => DropdownMenuItem<LogLevel?>(
                      value: level,
                      child: Text(level.displayName),
                    )),
                  ],
                  onChanged: (value) {
                    setState(() {
                      _selectedLevel = value;
                    });
                    _applyFilters();
                  },
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: DropdownButtonFormField<String?>(
                  value: _selectedPhoneNumber,
                  decoration: const InputDecoration(
                    labelText: 'Filter by Phone',
                    border: OutlineInputBorder(),
                    contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                  ),
                  items: [
                    const DropdownMenuItem<String?>(
                      value: null,
                      child: Text('All Numbers'),
                    ),
                    ..._logService.logs
                        .where((log) => log.phoneNumber != null)
                        .map((log) => log.phoneNumber!)
                        .toSet()
                        .map((phone) => DropdownMenuItem<String?>(
                              value: phone,
                              child: Text(phone),
                            )),
                  ],
                  onChanged: (value) {
                    setState(() {
                      _selectedPhoneNumber = value;
                    });
                    _applyFilters();
                  },
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            'Total Logs: ${_filteredLogs.length}',
            style: TextStyle(
              fontSize: 12,
              color: Colors.grey[600],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildLogCard(LogEntry log) {
    Color levelColor;
    IconData levelIcon;
    
    switch (log.level) {
      case LogLevel.info:
        levelColor = Colors.blue;
        levelIcon = Icons.info;
        break;
      case LogLevel.warning:
        levelColor = Colors.orange;
        levelIcon = Icons.warning;
        break;
      case LogLevel.error:
        levelColor = Colors.red;
        levelIcon = Icons.error;
        break;
      case LogLevel.success:
        levelColor = Colors.green;
        levelIcon = Icons.check_circle;
        break;
    }

    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(levelIcon, color: levelColor, size: 20),
                const SizedBox(width: 8),
                Text(
                  log.level.displayName,
                  style: TextStyle(
                    fontWeight: FontWeight.bold,
                    color: levelColor,
                    fontSize: 12,
                  ),
                ),
                const Spacer(),
                Text(
                  '${log.formattedDate} ${log.formattedTime}',
                  style: TextStyle(
                    fontSize: 12,
                    color: Colors.grey[600],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Text(
              log.message,
              style: const TextStyle(fontSize: 14),
            ),
            if (log.phoneNumber != null) ...[
              const SizedBox(height: 4),
              Text(
                'Phone: ${log.phoneNumber}',
                style: TextStyle(
                  fontSize: 12,
                  color: Colors.grey[600],
                  fontStyle: FontStyle.italic,
                ),
              ),
            ],
            if (log.details != null) ...[
              const SizedBox(height: 4),
              Text(
                log.details!,
                style: TextStyle(
                  fontSize: 12,
                  color: Colors.grey[700],
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
