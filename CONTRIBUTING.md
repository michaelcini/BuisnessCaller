# Contributing to Call Blocker

Thank you for your interest in contributing to Call Blocker! This document provides guidelines for contributing to the project.

## 🚀 Getting Started

### Prerequisites
- Flutter SDK (3.16.0 or higher)
- Android Studio or VS Code
- Git
- Android device or emulator for testing

### Setup
1. **Fork the repository** on GitHub
2. **Clone your fork**:
   ```bash
   git clone https://github.com/YOUR_USERNAME/call-blocker-android.git
   cd call-blocker-android
   ```
3. **Add upstream remote**:
   ```bash
   git remote add upstream https://github.com/ORIGINAL_OWNER/call-blocker-android.git
   ```
4. **Install dependencies**:
   ```bash
   flutter pub get
   ```

## 🔄 Development Workflow

### Branch Strategy
- `main` - Production-ready code
- `develop` - Integration branch for features
- `feature/*` - New features
- `bugfix/*` - Bug fixes
- `hotfix/*` - Critical fixes

### Making Changes
1. **Create a feature branch**:
   ```bash
   git checkout -b feature/amazing-feature
   ```
2. **Make your changes**
3. **Run tests**:
   ```bash
   flutter test
   flutter analyze
   ```
4. **Commit your changes**:
   ```bash
   git add .
   git commit -m "feat: add amazing feature"
   ```
5. **Push to your fork**:
   ```bash
   git push origin feature/amazing-feature
   ```
6. **Create a Pull Request**

## 📝 Code Style Guidelines

### Dart/Flutter Style
- Follow [Dart Style Guide](https://dart.dev/guides/language/effective-dart/style)
- Use meaningful variable and function names
- Add comments for complex logic
- Keep functions small and focused

### Commit Message Convention
```
type(scope): description

Types:
- feat: New feature
- fix: Bug fix
- docs: Documentation changes
- style: Code style changes
- refactor: Code refactoring
- test: Adding tests
- chore: Maintenance tasks

Examples:
feat(call-blocking): add call blocking functionality
fix(sms): resolve SMS sending issue
docs(readme): update installation instructions
```

## 🧪 Testing

### Running Tests
```bash
# Run all tests
flutter test

# Run specific test files
flutter test test/models/
flutter test test/widgets/

# Run with coverage
flutter test --coverage
```

### Test Requirements
- All new features must include tests
- Maintain at least 80% code coverage
- Test both happy path and edge cases
- Test on real Android device when possible

### Manual Testing Checklist
- [ ] App launches without crashes
- [ ] Permission requests work correctly
- [ ] Settings can be configured
- [ ] Call blocking works (outside business hours)
- [ ] SMS auto-reply works (outside business hours)
- [ ] Background service runs properly
- [ ] Battery optimization exemption works

## 🐛 Reporting Issues

### Bug Reports
Use the bug report template and include:
- Clear description of the issue
- Steps to reproduce
- Expected vs actual behavior
- Device information (device, OS, app version)
- Screenshots if applicable
- Relevant logs

### Feature Requests
Use the feature request template and include:
- Clear description of the feature
- Use case and benefits
- Any alternatives considered
- Additional context

## 🔍 Code Review Process

### Pull Request Guidelines
- Use the PR template
- Ensure all tests pass
- Update documentation if needed
- Keep PRs focused and small
- Respond to review feedback promptly

### Review Checklist
- [ ] Code follows style guidelines
- [ ] Tests are included and pass
- [ ] Documentation is updated
- [ ] No breaking changes
- [ ] Performance considerations
- [ ] Security implications

## 🚀 Release Process

### Version Management
- Use semantic versioning (MAJOR.MINOR.PATCH)
- Update version in `pubspec.yaml`
- Create release notes
- Tag releases properly

### Release Checklist
- [ ] All tests pass
- [ ] Documentation updated
- [ ] Version bumped
- [ ] Release notes prepared
- [ ] APK built and tested
- [ ] GitHub release created

## 🔐 Security

### Security Guidelines
- Never commit sensitive information
- Use environment variables for secrets
- Report security vulnerabilities privately
- Follow secure coding practices

### Reporting Security Issues
- Email: security@callblocker.app
- Use GitHub Security Advisories
- Do not create public issues for security problems

## 📚 Resources

### Documentation
- [Flutter Documentation](https://flutter.dev/docs)
- [Dart Language Guide](https://dart.dev/guides/language)
- [Android Development](https://developer.android.com/)

### Tools
- [Flutter Inspector](https://flutter.dev/docs/development/tools/flutter-inspector)
- [Dart DevTools](https://dart.dev/tools/dart-devtools)
- [Android Studio](https://developer.android.com/studio)

## 🤝 Community

### Getting Help
- Check existing issues and discussions
- Ask questions in GitHub Discussions
- Join our community chat (if available)

### Code of Conduct
- Be respectful and inclusive
- Focus on constructive feedback
- Help others learn and grow
- Follow GitHub's Community Guidelines

## 📄 License

By contributing to Call Blocker, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing to Call Blocker! 🎉
