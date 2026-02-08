# Contributing to Notiva

Thank you for your interest in contributing to Notiva! This guide will help you get started with development and walk you through the contribution process.

## Table of Contents

1. [Getting Started](#getting-started)
2. [Development Workflow](#development-workflow)
3. [Code Style Guide](#code-style-guide)
4. [Commit Message Convention](#commit-message-convention)
5. [Pull Request Process](#pull-request-process)
6. [Documentation Contributions](#documentation-contributions)
7. [Questions and Help](#questions-and-help)

---

## Getting Started

### Prerequisites

Before contributing, set up your development environment:

1. **Environment Setup**: Follow the [Setup Guide](docs/SETUP.md) to install Android Studio, JDK 17, and configure your development environment.

2. **Understand the Codebase**: Review the [Architecture Overview](docs/ARCHITECTURE.md) to understand how the app is structured.

3. **Fork and Clone**:
   ```bash
   # Fork the repository on GitHub, then clone your fork
   git clone https://github.com/YOUR-USERNAME/Notiva.git
   cd Notiva

   # Add the upstream remote
   git remote add upstream https://github.com/ORIGINAL-OWNER/Notiva.git
   ```

4. **Build the Project**:
   ```bash
   ./gradlew assembleDebug
   ```

5. **Run Tests**:
   ```bash
   ./gradlew test                    # Unit tests
   ./gradlew connectedAndroidTest    # Instrumented tests (requires emulator/device)
   ```

---

## Development Workflow

### 1. Create a Feature Branch

Always create a new branch from `master` for your changes:

```bash
# Sync with upstream
git fetch upstream
git checkout master
git merge upstream/master

# Create feature branch
git checkout -b feature/your-feature-name
```

**Branch naming conventions:**
- `feature/description` - New features
- `fix/description` - Bug fixes
- `docs/description` - Documentation changes
- `refactor/description` - Code refactoring
- `test/description` - Test additions/fixes

### 2. Make Your Changes

- Follow the [Code Style Guide](#code-style-guide)
- Write tests for new functionality (see [Testing Guide](docs/TESTING.md))
- Keep changes focused and atomic

### 3. Test Locally

Before committing, ensure your changes work:

```bash
# Run unit tests
./gradlew test

# Run lint checks
./gradlew lint

# Build the app
./gradlew assembleDebug

# Test on emulator/device
./gradlew installDebug
```

### 4. Commit Your Changes

Follow the [Commit Message Convention](#commit-message-convention):

```bash
git add <specific-files>
git commit -m "feat(reminders): add snooze duration configuration"
```

### 5. Push and Create Pull Request

```bash
git push origin feature/your-feature-name
```

Then create a Pull Request on GitHub following the [Pull Request Process](#pull-request-process).

---

## Code Style Guide

### Java Conventions

**Indentation and Formatting:**
- Use 4 spaces for indentation (no tabs)
- Opening braces on the same line as the statement
- Maximum line length: 100 characters
- One statement per line

```java
// Good
public void myMethod() {
    if (condition) {
        doSomething();
    } else {
        doSomethingElse();
    }
}

// Bad
public void myMethod()
{
    if (condition) { doSomething(); }
    else { doSomethingElse(); }
}
```

**Naming Conventions:**
- Classes: `PascalCase` (e.g., `ReminderModel`, `NotificationStarterService`)
- Methods: `camelCase` (e.g., `getReminder()`, `scheduleAlarm()`)
- Constants: `SCREAMING_SNAKE_CASE` (e.g., `CHANNEL_ID`, `REMINDER_ID`)
- Member variables: `camelCase` (e.g., `reminderDao`, `notificationManager`)

**Documentation:**
- Add Javadoc for public classes and methods
- Use inline comments sparingly, only for complex logic
- Keep comments up-to-date with code changes

```java
/**
 * Schedules a reminder notification for the specified time.
 *
 * @param reminder The reminder to schedule
 * @param triggerTime Time when the notification should fire
 * @return true if successfully scheduled, false otherwise
 */
public boolean scheduleReminder(ReminderModel reminder, Calendar triggerTime) {
    // Implementation
}
```

### Android-Specific Patterns

**Logging:**
- Use `android.util.Log` instead of `System.out.println()`
- Define a TAG constant for each class
- Use appropriate log levels (Log.d, Log.i, Log.w, Log.e)

```java
public class MyService extends Service {
    private static final String TAG = "MyService";

    public void doSomething() {
        Log.d(TAG, "Starting operation");
        // ...
        Log.e(TAG, "Error occurred", exception);
    }
}
```

**Resource Usage:**
- String literals in `res/values/strings.xml`
- Colors in `res/values/colors.xml`
- Dimensions in `res/values/dimens.xml`

**Import Ordering:**
1. Android imports (`android.*`, `androidx.*`)
2. Third-party imports (`com.google.*`, `dagger.*`)
3. Java imports (`java.*`, `javax.*`)
4. Project imports (`com.ava.notiva.*`)

Blank line between each group. Alphabetical within groups.

---

## Commit Message Convention

We follow [Conventional Commits](https://www.conventionalcommits.org/) format:

```
type(scope): description

[optional body]

[optional footer]
```

### Types

| Type | Description |
|------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation changes |
| `refactor` | Code refactoring (no functional change) |
| `test` | Adding or updating tests |
| `chore` | Build, config, or tooling changes |
| `style` | Code formatting (no functional change) |
| `perf` | Performance improvements |

### Scope (Optional)

Area of the codebase affected:
- `reminders` - Reminder CRUD operations
- `notifications` - Notification system
- `ui` - User interface components
- `db` - Database operations
- `worker` - Background workers

### Examples

From the project history:

```bash
# Feature addition
feat(reminders): add snooze duration configuration

# Bug fix
fix(notifications): prevent crash when reminder name is null

# Documentation
docs(09-01): create comprehensive UI documentation

# Refactoring
refactor(repository): extract callback handling to separate method

# Tests
test(dao): add unit tests for reminder queries
```

### Guidelines

- Keep the subject line under 72 characters
- Use imperative mood ("add" not "added", "fix" not "fixed")
- Don't end the subject line with a period
- Separate subject from body with a blank line
- Use the body to explain *what* and *why*, not *how*

---

## Pull Request Process

### Before Submitting

1. **Ensure all tests pass locally**
2. **Run lint and fix any issues**
3. **Rebase on latest master if needed**:
   ```bash
   git fetch upstream
   git rebase upstream/master
   ```

### Creating the PR

1. **Push your branch** to your fork
2. **Open a Pull Request** on GitHub
3. **Fill out the PR template** with:
   - Summary of changes
   - Related issue numbers (if any)
   - Testing performed
   - Screenshots (for UI changes)

### PR Title

Follow the same format as commit messages:
```
feat(reminders): add custom recurrence intervals
```

### Review Process

1. **Automated checks** will run (if configured)
2. **Maintainers will review** your code
3. **Address feedback** by pushing additional commits
4. **Once approved**, a maintainer will merge your PR

### After Merge

- Delete your feature branch
- Pull the latest master to your local repository
- Celebrate your contribution!

---

## Documentation Contributions

Documentation is just as important as code! Here's how to contribute:

### Location

- All documentation lives in the `docs/` folder
- Feature-specific docs go in `docs/features/`
- Main docs (SETUP, ARCHITECTURE, etc.) go in `docs/`

### Format

- Use Markdown (`.md` files)
- Use [Mermaid](https://mermaid.js.org/) for diagrams (GitHub renders natively)
- Follow existing documentation structure and style

### Style Guidelines

- Write for developers (technical, practical)
- Include code examples where helpful
- Link to source files when referencing code
- Keep sections focused and scannable
- Use tables for structured data

### Adding New Documentation

1. Create your `.md` file in the appropriate location
2. Update `docs/README.md` navigation table
3. Add links from related documents
4. Follow the commit convention: `docs(scope): description`

### Existing Documentation

For reference, see existing docs:
- [Architecture Overview](docs/ARCHITECTURE.md) - System design patterns
- [Database Reference](docs/DATABASE.md) - Schema documentation
- [Code Examples](docs/CODE_EXAMPLES.md) - Implementation patterns

---

## Questions and Help

### Getting Help

- **Check existing documentation** in `docs/` folder
- **Search existing issues** for similar questions
- **Open a new issue** if you can't find an answer

### Reporting Bugs

When reporting a bug, please include:

1. **Description**: What happened vs. what you expected
2. **Steps to reproduce**: Numbered list of actions
3. **Environment**: Android version, device/emulator, app version
4. **Logs**: Relevant logcat output (use `adb logcat | grep Notiva`)
5. **Screenshots**: If applicable

### Feature Requests

For feature requests:

1. **Check existing issues** for similar requests
2. **Describe the use case**: Why is this feature needed?
3. **Propose a solution**: How might it work?
4. **Be open to discussion**: Features may evolve through conversation

---

## Thank You!

Every contribution matters, whether it's:
- Fixing a typo in documentation
- Reporting a bug
- Suggesting a feature
- Writing code

We appreciate your time and effort in making Notiva better!

---

*Last updated: 2026-02-05*
