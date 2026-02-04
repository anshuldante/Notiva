# Codebase Structure

**Analysis Date:** 2026-02-04

## Directory Layout

```
my-reminder-app/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/ava/notiva/          # Application source code
│   │   │   │   ├── MainActivity.java          # Main list activity
│   │   │   │   ├── UpsertReminderActivity.java # Create/edit activity
│   │   │   │   ├── ReminderApplication.java   # App class with Hilt & WorkManager
│   │   │   │   ├── data/                      # Data layer (ViewModels, Repository, DAO, Database)
│   │   │   │   ├── model/                     # Entity classes (ReminderModel, RecurrenceType)
│   │   │   │   ├── service/                   # Services and Workers (alarm/notification logic)
│   │   │   │   ├── adapter/                   # RecyclerView adapters
│   │   │   │   ├── listener/                  # UI listeners (spinner, switch, text change)
│   │   │   │   ├── converter/                 # Room type converters
│   │   │   │   ├── module/                    # Dagger dependency injection modules
│   │   │   │   └── util/                      # Utility classes (constants, formatters)
│   │   │   ├── res/
│   │   │   │   ├── layout/                    # XML layouts (3 layouts: main, upsert, item)
│   │   │   │   ├── drawable/                  # Vector drawables and icons
│   │   │   │   ├── values/                    # String/dimension resources
│   │   │   │   ├── values-night/              # Dark theme resources
│   │   │   │   ├── raw/                       # Raw audio file (alarm.mp3)
│   │   │   │   └── mipmap-*/                  # App icons (multiple densities)
│   │   │   └── AndroidManifest.xml            # App manifest with permissions, activities, services
│   │   └── androidTest/java/...               # Instrumentation tests (empty placeholder)
│   ├── build.gradle                           # App-level build config
│   └── proguard-rules.pro
├── build.gradle                               # Project-level build config
├── settings.gradle                            # Gradle module configuration
└── README.md                                  # User documentation

```

## Directory Purposes

**app/src/main/java/com/ava/notiva/:**
- Purpose: Main application package containing all Kotlin/Java source code
- Contains: Activities, ViewModels, Services, models, utilities
- Key files: `MainActivity.java`, `ReminderApplication.java`

**app/src/main/java/com/ava/notiva/data/:**
- Purpose: Data layer implementing MVVM and Repository pattern
- Contains: ViewModels, Repository, DAO interface, Room Database class
- Key files: `ReminderRepository.java`, `ReminderDao.java`, `RemindersDb.java`, `GetAllRemindersViewModel.java`, `ReminderDmlViewModel.java`

**app/src/main/java/com/ava/notiva/model/:**
- Purpose: Data models and enumerations
- Contains: Room entity definitions, business logic for scheduling
- Key files: `ReminderModel.java`, `RecurrenceType.java`

**app/src/main/java/com/ava/notiva/service/:**
- Purpose: Background execution for reminders and notifications
- Contains: Services (foreground notification), Workers (alarm scheduling), Receivers (boot)
- Key files: `ReminderTriggerWorker.java`, `NotificationStarterService.java`, `NotificationStopperService.java`, `BootReceiver.java`

**app/src/main/java/com/ava/notiva/adapter/:**
- Purpose: RecyclerView data binding
- Contains: List adapters and diff callbacks
- Key files: `ReminderItemAdapter.java`, `ReminderDiffCallback.java`

**app/src/main/java/com/ava/notiva/listener/:**
- Purpose: UI event listeners and callbacks
- Contains: Spinner listeners, switch listeners, text change listeners
- Key files: `RecurrenceTypeListener.java`, `RecurrenceSwitchListener.java`, `ReminderNameChangedListener.java`, `RecurrenceDelayChangedListener.java`

**app/src/main/java/com/ava/notiva/converter/:**
- Purpose: Room type conversion for non-standard types
- Contains: Custom converters for Calendar, RecurrenceType, etc.
- Key files: `DbTypeConverters.java`

**app/src/main/java/com/ava/notiva/module/:**
- Purpose: Dagger Hilt dependency injection configuration
- Contains: Module providing singletons (Database, Repository, ViewModels, ExecutorService)
- Key files: `DbModule.java`

**app/src/main/java/com/ava/notiva/util/:**
- Purpose: Utility classes and constants
- Contains: Display formatters, constants, input validators
- Key files: `ReminderConstants.java`, `DateTimeDisplayUtil.java`, `RecurrenceDisplayUtil.java`, `InputFilterMinMax.java`

**app/src/main/res/layout/:**
- Purpose: XML layout definitions for UI screens
- Contains: 3 layouts total
- Key files: `activity_main.xml` (list screen), `activity_upsert_reminder.xml` (create/edit screen), `rv_item_reminder.xml` (list item)

**app/src/main/res/drawable/:**
- Purpose: Vector drawables and custom graphics
- Contains: Icon PNGs and vector XMLs for buttons, actions
- Key files: `ic_launcher.png`, `ic_alarm.xml`, `ic_baseline_cancel_24.xml`, `ic_baseline_snooze_24.xml`

**app/src/main/res/values/:**
- Purpose: Non-image resources (strings, dimensions, colors)
- Contains: String resources (labels, messages), dimension constants
- Key files: `strings.xml` (all user-visible text)

**app/src/main/res/raw/:**
- Purpose: Raw asset files
- Contains: Audio file for alarm tone
- Key files: `alarm.mp3`

## Key File Locations

**Entry Points:**

- `app/src/main/java/com/ava/notiva/MainActivity.java`: Main launcher activity, displays reminder list
- `app/src/main/java/com/ava/notiva/ReminderApplication.java`: Application class, initializes Hilt and WorkManager
- `app/src/main/java/com/ava/notiva/service/ReminderTriggerWorker.java`: WorkManager periodic task for alarm scheduling
- `app/src/main/AndroidManifest.xml`: Declares activities, services, receivers, permissions

**Configuration:**

- `app/build.gradle`: Dependencies, SDK versions, build options
- `build.gradle`: Project-level Gradle configuration
- `settings.gradle`: Module settings
- `local.properties`: Local development properties (SDK path)

**Core Logic:**

- `app/src/main/java/com/ava/notiva/model/ReminderModel.java`: Entity with recurrence calculation logic
- `app/src/main/java/com/ava/notiva/data/ReminderRepository.java`: Data access abstraction
- `app/src/main/java/com/ava/notiva/data/ReminderDao.java`: Room database interface
- `app/src/main/java/com/ava/notiva/service/NotificationStarterService.java`: Alarm notification and sound playback

**UI/View:**

- `app/src/main/java/com/ava/notiva/MainActivity.java`: Reminder list, swipe-to-delete
- `app/src/main/java/com/ava/notiva/UpsertReminderActivity.java`: Create/edit reminder form
- `app/src/main/java/com/ava/notiva/adapter/ReminderItemAdapter.java`: List item binding
- `app/src/main/res/layout/activity_main.xml`: Main screen layout
- `app/src/main/res/layout/activity_upsert_reminder.xml`: Create/edit screen layout

**Testing:**

- `app/src/androidTest/java/com/ava/notiva/`: Instrumentation test placeholder (currently empty)

## Naming Conventions

**Files:**

- Activities: `[Feature]Activity.java` (e.g., `MainActivity.java`, `UpsertReminderActivity.java`)
- Services: `[Name]Service.java` (e.g., `NotificationStarterService.java`)
- Workers: `[Name]Worker.java` (e.g., `ReminderTriggerWorker.java`)
- Receivers: `[Name]Receiver.java` (e.g., `BootReceiver.java`)
- ViewModels: `[Feature]ViewModel.java` (e.g., `ReminderDmlViewModel.java`, `GetAllRemindersViewModel.java`)
- Repository: `[Entity]Repository.java` (e.g., `ReminderRepository.java`)
- DAO: `[Entity]Dao.java` (e.g., `ReminderDao.java`)
- Models/Entities: `[Entity]Model.java` or `[Entity].java` (e.g., `ReminderModel.java`, `RecurrenceType.java`)
- Adapters: `[Item][Type]Adapter.java` (e.g., `ReminderItemAdapter.java`)
- Utilities: `[Feature]Util.java` or `[Feature]Utils.java` (e.g., `DateTimeDisplayUtil.java`)
- Constants: `[Feature]Constants.java` (e.g., `ReminderConstants.java`)
- Listeners: `[Event]Listener.java` (e.g., `RecurrenceTypeListener.java`)

**Directories:**

- Package pattern: `com.ava.notiva` + optional subpackage (data, model, service, adapter, etc.)
- Resource directories: Standard Android naming (res/layout, res/drawable, res/values)
- Resource file naming:
  - Activities: `activity_[feature].xml` (e.g., `activity_main.xml`)
  - Items: `rv_item_[type].xml` (e.g., `rv_item_reminder.xml`)
  - Drawables: `ic_[name].xml` or `ic_[name].png`
  - Strings file: `strings.xml` (single file for all strings)

## Where to Add New Code

**New Feature:**
- Primary code: Create new Activity in `app/src/main/java/com/ava/notiva/[FeatureName]Activity.java`
- ViewModel: `app/src/main/java/com/ava/notiva/data/[Feature]ViewModel.java`
- Layout: `app/src/main/res/layout/activity_[feature].xml`
- Tests: `app/src/androidTest/java/com/ava/notiva/[FeatureName]ActivityTest.java`

**New Service/Worker:**
- Implementation: `app/src/main/java/com/ava/notiva/service/[Name]Service.java` or `[Name]Worker.java`
- Register in: `app/src/main/AndroidManifest.xml` (add <service> or <receiver> tag)
- Tests: `app/src/androidTest/java/com/ava/notiva/service/[Name]ServiceTest.java`

**New Database Entity:**
- Model: `app/src/main/java/com/ava/notiva/model/[Entity]Model.java` with `@Entity` annotation
- DAO: Add methods to existing `ReminderDao.java` or create `app/src/main/java/com/ava/notiva/data/[Entity]Dao.java`
- Type Converters: Add methods to `app/src/main/java/com/ava/notiva/converter/DbTypeConverters.java` if needed
- Migration: Update Room @Database version in `RemindersDb.java`, create migration if changing existing schema

**New Utility/Helper:**
- Shared helpers: `app/src/main/java/com/ava/notiva/util/[Feature]Util.java`
- Constants: Add to `app/src/main/java/com/ava/notiva/util/ReminderConstants.java` or create new `[Feature]Constants.java`
- Listeners: `app/src/main/java/com/ava/notiva/listener/[Event]Listener.java`

**Dependency Injection:**
- Update: `app/src/main/java/com/ava/notiva/module/DbModule.java` to add new @Provides methods for singletons

**UI Resources:**
- Strings: Add to `app/src/main/res/values/strings.xml`
- Dimensions: Add to `app/src/main/res/values/dimens.xml` (create if missing)
- Colors: Add to `app/src/main/res/values/colors.xml` (create if missing)
- Drawables: Create SVG/PNG in `app/src/main/res/drawable/`

## Special Directories

**app/.gradle/:**
- Purpose: Gradle build cache
- Generated: Yes (automatically created by Gradle)
- Committed: No (in .gitignore)

**app/build/:**
- Purpose: Compiled outputs, APK, intermediate files
- Generated: Yes (build outputs)
- Committed: No (in .gitignore)

**.idea/:**
- Purpose: Android Studio IDE configuration
- Generated: Yes (IDE metadata)
- Committed: No (in .gitignore)

**.planning/:**
- Purpose: GSD planning documents
- Generated: Yes (by orchestrator)
- Committed: Yes (version control for analysis)
- Contains: `codebase/ARCHITECTURE.md`, `codebase/STRUCTURE.md`, etc.

## Package Structure Visualization

```
com.ava.notiva
├── (root package)
│   ├── MainActivity.java              # Main list activity
│   ├── UpsertReminderActivity.java    # Create/edit activity
│   └── ReminderApplication.java       # Application class
├── data                               # MVVM ViewModels + Repository layer
│   ├── GetAllRemindersViewModel.java
│   ├── ReminderDmlViewModel.java
│   ├── ReminderRepository.java
│   ├── ReminderDao.java
│   └── RemindersDb.java
├── model                              # Entity definitions
│   ├── ReminderModel.java
│   └── RecurrenceType.java
├── service                            # Background services + workers
│   ├── ReminderTriggerWorker.java
│   ├── NotificationStarterService.java
│   ├── NotificationStopperService.java
│   └── BootReceiver.java
├── adapter                            # RecyclerView
│   ├── ReminderItemAdapter.java
│   └── ReminderDiffCallback.java
├── listener                           # UI listeners
│   ├── RecurrenceTypeListener.java
│   ├── RecurrenceSwitchListener.java
│   ├── ReminderNameChangedListener.java
│   └── RecurrenceDelayChangedListener.java
├── converter                          # Room type converters
│   └── DbTypeConverters.java
├── module                             # Dagger DI
│   └── DbModule.java
└── util                               # Utilities
    ├── ReminderConstants.java
    ├── DateTimeDisplayUtil.java
    ├── RecurrenceDisplayUtil.java
    ├── InputFilterMinMax.java
    ├── ReminderWorkerUtils.java
    └── FriendlyDateType.java
```

---

*Structure analysis: 2026-02-04*
