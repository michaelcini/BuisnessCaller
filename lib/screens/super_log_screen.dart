import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import '../services/super_log_service.dart';

class SuperLogScreen extends StatefulWidget {
  const SuperLogScreen({Key? key}) : super(key: key);

  @override
  State<SuperLogScreen> createState() => _SuperLogScreenState();
}

class _SuperLogScreenState extends State<SuperLogScreen> {
  final TextEditingController _searchController = TextEditingController();
  final ScrollController _scrollController = ScrollController();
  bool _autoScroll = true;

  @override
  void initState() {
    super.initState();
    _searchController.addListener(() {
      context.read<SuperLogService>().setSearchQuery(_searchController.text);
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
        title: const Text('Super Log'),
        backgroundColor: Colors.purple,
        foregroundColor: Colors.white,
        actions: [
          IconButton(
            icon: const Icon(Icons.clear_all),
            onPressed: () => _showClearDialog(),
            tooltip: 'Clear Logs',
          ),
          IconButton(
            icon: const Icon(Icons.download),
            onPressed: () => _exportLogs(),
            tooltip: 'Export Logs',
          ),
          PopupMenuButton<String>(
            onSelected: (value) {
              if (value == 'clear_filters') {
                context.read<SuperLogService>().clearFilters();
                _searchController.clear();
              }
            },
            itemBuilder: (context) => [
              const PopupMenuItem(
                value: 'clear_filters',
                child: Text('Clear Filters'),
              ),
            ],
          ),
        ],
      ),
      body: Column(
        children: [
          _buildFilterBar(),
          Expanded(
            child: Consumer<SuperLogService>(
              builder: (context, logService, child) {
                final logs = logService.filteredLogs;
                
                if (logs.isEmpty) {
                  return const Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(Icons.bug_report, size: 64, color: Colors.grey),
                        SizedBox(height: 16),
                        Text(
                          'No logs available',
                          style: TextStyle(fontSize: 18, color: Colors.grey),
                        ),
                        SizedBox(height: 8),
                        Text(
                          'Start using the app to see logs here',
                          style: TextStyle(color: Colors.grey),
                        ),
                      ],
                    ),
                  );
                }

                return ListView.builder(
                  controller: _scrollController,
                  itemCount: logs.length,
                  itemBuilder: (context, index) {
                    final log = logs[index];
                    return _buildLogEntry(log);
                  },
                );
              },
            ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          if (_autoScroll) {
            _scrollController.animateTo(
              0,
              duration: const Duration(milliseconds: 300),
              curve: Curves.easeOut,
            );
          }
        },
        child: Icon(_autoScroll ? Icons.arrow_upward : Icons.arrow_downward),
        tooltip: 'Scroll to Top',
      ),
    );
  }

  Widget _buildFilterBar() {
    return Container(
      padding: const EdgeInsets.all(8),
      decoration: BoxDecoration(
        color: Colors.grey[100],
        border: Border(
          bottom: BorderSide(color: Colors.grey[300]!),
        ),
      ),
      child: Column(
        children: [
          // Search bar
          TextField(
            controller: _searchController,
            decoration: const InputDecoration(
              hintText: 'Search logs...',
              prefixIcon: Icon(Icons.search),
              border: OutlineInputBorder(),
              contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
            ),
          ),
          const SizedBox(height: 8),
          // Filter controls
          Consumer<SuperLogService>(
            builder: (context, logService, child) {
              return Row(
                children: [
                  // Level filter
                  Expanded(
                    child: DropdownButton<String>(
                      value: logService.levelFilter,
                      isExpanded: true,
                      items: logService.availableLevels.map((level) {
                        return DropdownMenuItem(
                          value: level,
                          child: Text(level),
                        );
                      }).toList(),
                      onChanged: (value) {
                        if (value != null) {
                          logService.setLevelFilter(value);
                        }
                      },
                    ),
                  ),
                  const SizedBox(width: 8),
                  // Tag filter
                  Expanded(
                    child: DropdownButton<String>(
                      value: logService.filteredTags.isEmpty ? 'All Tags' : '${logService.filteredTags.length} selected',
                      isExpanded: true,
                      items: [
                        const DropdownMenuItem(
                          value: 'All Tags',
                          child: Text('All Tags'),
                        ),
                        ...logService.availableTags.map((tag) {
                          return DropdownMenuItem(
                            value: tag,
                            child: Row(
                              children: [
                                Checkbox(
                                  value: logService.filteredTags.contains(tag),
                                  onChanged: (checked) {
                                    logService.toggleTagFilter(tag);
                                  },
                                ),
                                Expanded(child: Text(tag)),
                              ],
                            ),
                          );
                        }),
                      ],
                      onChanged: (value) {
                        // Handle tag selection
                      },
                    ),
                  ),
                ],
              );
            },
          ),
        ],
      ),
    );
  }

  Widget _buildLogEntry(SuperLogEntry log) {
    Color levelColor;
    IconData levelIcon;
    
    switch (log.level) {
      case 'ERROR':
        levelColor = Colors.red;
        levelIcon = Icons.error;
        break;
      case 'WARNING':
        levelColor = Colors.orange;
        levelIcon = Icons.warning;
        break;
      case 'INFO':
        levelColor = Colors.blue;
        levelIcon = Icons.info;
        break;
      case 'DEBUG':
        levelColor = Colors.grey;
        levelIcon = Icons.bug_report;
        break;
      default:
        levelColor = Colors.black;
        levelIcon = Icons.circle;
    }

    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: levelColor.withOpacity(0.1),
          child: Icon(levelIcon, color: levelColor, size: 20),
        ),
        title: Text(
          log.message,
          style: const TextStyle(fontSize: 14),
          maxLines: 2,
          overflow: TextOverflow.ellipsis,
        ),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              '${log.formattedTime} | ${log.tag}',
              style: TextStyle(
                fontSize: 12,
                color: Colors.grey[600],
              ),
            ),
            if (log.details != null)
              Text(
                'Details: ${log.details}',
                style: TextStyle(
                  fontSize: 11,
                  color: Colors.grey[500],
                ),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            if (log.phoneNumber != null)
              Text(
                'Phone: ${log.phoneNumber}',
                style: TextStyle(
                  fontSize: 11,
                  color: Colors.blue[600],
                ),
              ),
          ],
        ),
        onTap: () => _showLogDetails(log),
        isThreeLine: log.details != null || log.phoneNumber != null,
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
              Text('Tag: ${log.tag}'),
              Text('Level: ${log.level}'),
              const SizedBox(height: 8),
              const Text('Message:', style: TextStyle(fontWeight: FontWeight.bold)),
              Text(log.message),
              if (log.details != null) ...[
                const SizedBox(height: 8),
                const Text('Details:', style: TextStyle(fontWeight: FontWeight.bold)),
                Text(log.details!),
              ],
              if (log.phoneNumber != null) ...[
                const SizedBox(height: 8),
                const Text('Phone:', style: TextStyle(fontWeight: FontWeight.bold)),
                Text(log.phoneNumber!),
              ],
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Close'),
          ),
          TextButton(
            onPressed: () {
              Clipboard.setData(ClipboardData(text: log.fullMessage));
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Log copied to clipboard')),
              );
              Navigator.pop(context);
            },
            child: const Text('Copy'),
          ),
        ],
      ),
    );
  }

  void _showClearDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Clear Logs'),
        content: const Text('Are you sure you want to clear all logs? This action cannot be undone.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () {
              context.read<SuperLogService>().clearLogs();
              Navigator.pop(context);
            },
            child: const Text('Clear'),
          ),
        ],
      ),
    );
  }

  void _exportLogs() {
    final logService = context.read<SuperLogService>();
    final exportedLogs = logService.exportLogs();
    
    Clipboard.setData(ClipboardData(text: exportedLogs));
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Logs exported to clipboard'),
        duration: Duration(seconds: 3),
      ),
    );
  }
}