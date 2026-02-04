# Notiva Developer Documentation

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![License](https://img.shields.io/badge/license-proprietary-lightgrey)
![Platform](https://img.shields.io/badge/platform-Android-green)
![Min SDK](https://img.shields.io/badge/minSdk-33-blue)

## Project Overview

Notiva is an Android reminder application designed to help users manage their tasks and receive timely notifications. The app provides a clean, intuitive interface for creating, editing, and managing reminders with support for various recurrence patterns and notification options.

The application is built using modern Android development practices with Java as the primary language. It leverages Room for local database persistence, Hilt for dependency injection, and WorkManager for reliable background task scheduling. The UI follows Material Design guidelines to provide a consistent and accessible user experience.

This documentation covers everything developers need to understand, build, and contribute to Notiva: from initial environment setup to deep dives into the database schema, testing strategies, and feature implementations.

## Quick Start

For complete environment setup, see the [Setup Guide](SETUP.md).

**Quick overview:**

```bash
# Clone the repository
git clone <repository-url>
cd my-reminder-app

# Open in Android Studio and sync Gradle
# Build and run on emulator or device
./gradlew assembleDebug
```

## Documentation Navigation

| I want to... | Go to |
|--------------|-------|
| Set up my development environment | [Setup Guide](SETUP.md) |
| Understand the app architecture | [Architecture Overview](ARCHITECTURE.md) |
| Learn about the database schema | [Database Reference](DATABASE.md) |
| Run or write tests | [Testing Guide](TESTING.md) |
| Understand how reminders work | [Reminders Feature](features/REMINDERS.md) |
| Understand notifications | [Notifications Feature](features/NOTIFICATIONS.md) |
| Learn about recurrence logic | [Recurrence & Constraints](features/RECURRENCE.md) |
| See the UI structure | [UI Documentation](UI.md) |
| Contribute to the project | [Contributing Guide](CONTRIBUTING.md) |

## Documentation Status

| Document | Status | Description |
|----------|--------|-------------|
| [README.md](README.md) | Available | This file - documentation hub |
| [SETUP.md](SETUP.md) | Available | Development environment setup |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Available | App architecture and patterns |
| [DATABASE.md](DATABASE.md) | Planned | Room schema and migrations |
| [TESTING.md](TESTING.md) | Planned | Testing strategy and guides |
| [UI.md](UI.md) | Planned | UI components and navigation |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Planned | Contribution guidelines |
| [features/REMINDERS.md](features/REMINDERS.md) | Planned | Reminder CRUD operations |
| [features/NOTIFICATIONS.md](features/NOTIFICATIONS.md) | Planned | Notification system |
| [features/RECURRENCE.md](features/RECURRENCE.md) | Planned | Recurrence patterns |

## Tech Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Language | Java | 17 |
| UI Framework | Material Design | 1.13.0 |
| Database | Room | 2.8.1 |
| Dependency Injection | Hilt | 2.57.2 |
| Background Tasks | WorkManager | 2.10.5 |
| Async Operations | RxJava2 | (via Room) |
| Min SDK | Android 13 | API 33 |
| Target SDK | Android 15 | API 36 |

## App Package Info

| Property | Value |
|----------|-------|
| Package | `com.ava.notiva` |
| Application ID | `com.ava.notiva` |
| Main Entry | `MainActivity` |
| Reminder Editor | `UpsertReminderActivity` |

## Directory Structure

```
my-reminder-app/
├── app/
│   └── src/main/java/com/ava/notiva/
│       ├── activities/          # Activity classes
│       ├── adapters/            # RecyclerView adapters
│       ├── database/            # Room entities and DAOs
│       ├── models/              # Data models
│       ├── receivers/           # Broadcast receivers
│       ├── services/            # Background services
│       └── workers/             # WorkManager workers
├── docs/                        # This documentation
└── resources/                   # App resources
```

## Getting Help

- **Setup issues**: Start with [SETUP.md](SETUP.md)
- **Architecture questions**: See [ARCHITECTURE.md](ARCHITECTURE.md)
- **Database questions**: Check [DATABASE.md](DATABASE.md)
- **Want to contribute**: Read [CONTRIBUTING.md](CONTRIBUTING.md)

---

*Documentation last updated: 2026-02-05*
