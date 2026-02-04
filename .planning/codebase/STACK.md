# Technology Stack

**Analysis Date:** 2026-02-04

## Languages

**Primary:**
- Java 17 - All application code, services, and business logic
- XML - Manifest configuration and resource files

## Runtime

**Environment:**
- Android Runtime (ART)
- Target SDK: 36 (Android 15)
- Minimum SDK: 33 (Android 13)
- Build Tools: 36.0.0

**Package Manager:**
- Gradle 8.12.3 - Build system and dependency management
- Lockfile: `gradle.properties` and `.gradle/` directory manage version constraints

## Frameworks

**Core:**
- AndroidX AppCompat 1.7.1 - UI compatibility layer
- AndroidX Constraint Layout 2.2.1 - Layout management for activities
- Material Design 1.13.0 - UI component library

**Dependency Injection:**
- Dagger/Hilt 2.57.2 - Dependency injection framework
- Hilt Android integration 1.3.0 - Android-specific DI features

**Data Persistence:**
- Room 2.8.1 - Database ORM and persistence layer
  - Room Core runtime
  - Room Compiler for annotation processing
  - Room RxJava2 support for reactive database queries

**Background Jobs:**
- AndroidX Work Manager 2.10.5 - Scheduling periodic and one-time background tasks
- Hilt Work 1.3.0 - Integration of Hilt with WorkManager

**Testing:**
- JUnit 4.13.2 - Unit testing framework
- AndroidX Test JUnit 1.3.0 - Android test support
- Espresso 3.7.0 - UI/Instrumentation testing framework

## Key Dependencies

**Critical:**
- `androidx.room:room-runtime:2.8.1` - Provides SQLite database abstraction and ORM capabilities
- `com.google.dagger:hilt-android:2.57.2` - Core dependency injection enabling modular architecture
- `androidx.work:work-runtime:2.10.5` - Background task scheduling for periodic reminder checks

**Infrastructure:**
- `androidx.activity:activity:1.11.0` - Activity lifecycle management with modern API
- `androidx.appcompat:appcompat:1.7.1` - Backward compatibility and AppBar support
- `androidx.hilt:hilt-compiler:1.3.0` - Annotation processor for Hilt in Android context

## Configuration

**Environment:**
- Configuration managed via `gradle.properties`:
  - JVM memory allocation: `-Xmx2048m`
  - File encoding: `UTF-8`
  - AndroidX package structure enabled
- Lint warnings disabled: `RemoveWorkManagerInitializer` (see `app/build.gradle`)

**Build:**
- Top-level build configuration: `build.gradle`
- App module configuration: `app/build.gradle`
- Namespace: `com.ava.notiva`
- Application ID: `com.ava.notiva`
- Version Code: 1, Version Name: 1.0

**Compiler Settings:**
- Java source/target compatibility: Java 17
- Compilation warnings enabled: `-Xlint:unchecked`, `-Xlint:deprecation`
- Minification: Disabled for release builds (ProGuard rules in `app/proguard-rules.pro`)

## Platform Requirements

**Development:**
- Android Studio with Gradle support
- Java 17 SDK installed
- API 33+ for testing on physical devices or emulators
- `local.properties` required with Android SDK path

**Production:**
- Target deployment: Android 15 (API 36)
- Minimum runtime: Android 13 (API 33)
- Required permissions declared in `app/src/main/AndroidManifest.xml`:
  - `VIBRATE` - Notification vibration
  - `FOREGROUND_SERVICE` - Background reminder service
  - `FOREGROUND_SERVICE_MEDIA_PLAYBACK` - Audio playback in foreground service
  - `SCHEDULE_EXACT_ALARM` - Precise alarm scheduling
  - `USE_EXACT_ALARM` - Exact alarm capability
  - `POST_NOTIFICATIONS` - Push notification display

---

*Stack analysis: 2026-02-04*
