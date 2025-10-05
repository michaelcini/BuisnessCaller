import 'package:flutter/material.dart';

class CustomMessageField extends StatefulWidget {
  final String initialValue;
  final Function(String) onChanged;

  const CustomMessageField({
    super.key,
    required this.initialValue,
    required this.onChanged,
  });

  @override
  State<CustomMessageField> createState() => _CustomMessageFieldState();
}

class _CustomMessageFieldState extends State<CustomMessageField> {
  late TextEditingController _controller;

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController(text: widget.initialValue);
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Auto-Reply Message',
          style: Theme.of(context).textTheme.bodyMedium?.copyWith(
            color: Theme.of(context).colorScheme.onSurfaceVariant,
          ),
        ),
        const SizedBox(height: 8),
        TextField(
          controller: _controller,
          maxLines: 3,
          decoration: InputDecoration(
            hintText: 'Enter your custom auto-reply message...',
            border: const OutlineInputBorder(),
            helperText: 'This message will be sent automatically when outside business hours',
          ),
          onChanged: widget.onChanged,
        ),
        const SizedBox(height: 8),
        Row(
          children: [
            Expanded(
              child: OutlinedButton.icon(
                onPressed: () {
                  _controller.text = 'I am currently unavailable. Please call back during business hours.';
                  widget.onChanged(_controller.text);
                },
                icon: const Icon(Icons.restore),
                label: const Text('Reset to Default'),
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: OutlinedButton.icon(
                onPressed: () {
                  _controller.text = 'Thank you for your message. I will get back to you during business hours.';
                  widget.onChanged(_controller.text);
                },
                icon: const Icon(Icons.edit),
                label: const Text('Professional'),
              ),
            ),
          ],
        ),
      ],
    );
  }
}

