# Architecture

**Analysis Date:** 2026-02-04

## Pattern Overview

**Overall:** MVVM (Model-View-ViewModel) with Layered Architecture + Dependency Injection

**Key Characteristics:**
- Separation of concerns via ViewModels and Repository pattern
- Dagger Hilt dependency injection for loose coupling
- LiveData for reactive data binding
- Room ORM for persistence layer
- WorkManager for background scheduling
- Services and Workers for reminder triggering

## Layers

**Presentation Layer (UI):**
- Purpose: Handle user interactions and display reminders
- Location: `app/src/main/java/com/ava/notiva/MainActivity.java`, `UpsertReminderActivity.java`
- Contains: Activities, adapters, UI logic
- Depends on: ViewModels, Models
- Used by: Android framework (launched by system)

**ViewModel/Controller Layer:**
- Purpose: Manage UI state and orchestrate data operations
- Location: `app/src/main/java/com/ava/notiva/data/GetAllRemindersViewModel.java`, `ReminderDmlViewModel.java`
- Contains: ViewModel classes extending AndroidX ViewModel
- Depends on: Repository
- Used by: Activities through DI

**Repository Layer:**
- Purpose: Abstract data access and provide unified interface to data sources
- Location: `app/src/main/java/com/ava/notiva/data/ReminderRepository.java`
- Contains: ReminderRepository with async operations via ExecutorService
- Depends on: DAO, ExecutorService
- Used by: ViewModels

**Data Access Layer (DAO):**
- Purpose: Direct database access using Room ORM
- Location: `app/src/main/java/com/ava/notiva/data/ReminderDao.java`
- Contains: Room DAO interface with LiveData-returning queries
- Depends on: Room Database
- Used by: Repository

**Database Layer:**
- Purpose: SQLite persistence
- Location: `app/src/main/java/com/ava/notiva/data/RemindersDb.java`
- Contains: Room Database definition, migration handling
- Depends on: Room runtime
- Used by: DAO

**Model Layer:**
- Purpose: Data representation
- Location: `app/src/main/java/com/ava/notiva/model/ReminderModel.java`, `RecurrenceType.java`
- Contains: Room entities with type converters
- Depends on: None (pure data)
- Used by: All layers

**Background/Service Layer:**
- Purpose: Handle reminder triggering and notifications
- Location: `app/src/main/java/com/ava/notiva/service/ReminderTriggerWorker.java`, `NotificationStarterService.java`, `NotificationStopperService.java`
- Contains: WorkManager Worker and foreground Services
- Depends on: Repository, AlarmManager, MediaPlayer
- Used by: WorkManager scheduler, Android system

**Utility/Helper Layer:**
- Purpose: Cross-cutting concerns and formatting
- Location: `app/src/main/java/com/ava/notiva/util/ReminderConstants.java`, `DateTimeDisplayUtil.java`, `RecurrenceDisplayUtil.java`
- Contains: Constants, display formatting, conversion utilities
- Depends on: Android Context, Calendar
- Used by: All layers

**Dependency Injection Layer:**
- Purpose: Configure and provide singleton instances
- Location: `app/src/main/java/com/ava/notiva/module/DbModule.java`, `ReminderApplication.java`
- Contains: Dagger Module providing Database, DAO, Repository, ViewModels
- Depends on: All data layer components
- Used by: Hilt container at app startup

## Data Flow

**Viewing Reminders (Read):**

1. `MainActivity.onCreate()` calls `GetAllRemindersViewModel.getAllReminders()`
2. ViewModel delegates to `ReminderRepository.getAll()` which returns `LiveData<List<ReminderModel>>`
3. Repository wraps `ReminderDao.getAll()` which executes Room query
4. Query result flows through LiveData to MainActivity observer
5. `ReminderItemAdapter.submitList()` updates RecyclerView with results

**Creating/Updating Reminders (Write):**

1. `UpsertReminderActivity` collects user input and builds `ReminderModel`
2. Activity calls `ReminderDmlViewModel.addReminderWithCallback()`
3. ViewModel delegates to `ReminderRepository.addWithCallback()`
4. Repository submits task to `reminderDaoExecutor` (fixed thread pool)
5. Executor thread calls `ReminderDao.add()` in transaction
6. Callback fires on executor thread with generated ID
7. UI updated via callback consumer

**Triggering Reminders (Scheduled Background):**

1. `ReminderApplication.onCreate()` enqueues `ReminderTriggerWorker` as periodic task (1-minute interval)
2. WorkManager executes `ReminderTriggerWorker.doWork()` periodically
3. Worker calls `ReminderRepository.getAllSync()` to fetch all active reminders
4. Worker iterates reminders and calculates next occurrence using `ReminderModel.getNextOccurrenceAfter()`
5. For each upcoming reminder, worker schedules exact alarm via `AlarmManager.setExactAndAllowWhileIdle()`
6. Pending intent targets `NotificationStarterService`
7. When alarm fires, `NotificationStarterService.onStartCommand()` executes
8. Service plays alarm sound, vibrates, and posts foreground notification
9. User interacts with dismiss/snooze actions (Intents to `NotificationStopperService`)

**State Management:**

- UI state: Managed by ViewModel's internal LiveData reference
- Persistent state: Room database (reminders with all properties)
- Runtime state: Service instances hold MediaPlayer and vibrator state
- Scheduled state: AlarmManager holds pending intents for upcoming alarms

## Key Abstractions

**ReminderModel:**
- Purpose: Represents a reminder with scheduling logic
- Examples: `app/src/main/java/com/ava/notiva/model/ReminderModel.java`
- Pattern: Room Entity with `@Entity`, `@PrimaryKey`, `@TypeConverters` annotations. Includes business logic `getNextOccurrenceAfter()` for recurrence calculation

**RecurrenceType:**
- Purpose: Enumeration of repeat patterns (NEVER, DAY, WEEK, MONTH, FOREVER)
- Examples: `app/src/main/java/com/ava/notiva/model/RecurrenceType.java`
- Pattern: Enum with custom `getMillis()` method returning interval in milliseconds

**ReminderRepository:**
- Purpose: Data access abstraction with async operations
- Examples: `app/src/main/java/com/ava/notiva/data/ReminderRepository.java`
- Pattern: Repository wraps DAO and ExecutorService, returns callbacks for async operations

**ViewModels (GetAllRemindersViewModel, ReminderDmlViewModel):**
- Purpose: Survive configuration changes, expose LiveData for reactive binding
- Examples: `app/src/main/java/com/ava/notiva/data/GetAllRemindersViewModel.java`, `ReminderDmlViewModel.java`
- Pattern: Thin wrappers around Repository, one ViewModel per use case (reads vs writes)

## Entry Points

**Main Activity (User-Facing):**
- Location: `app/src/main/java/com/ava/notiva/MainActivity.java`
- Triggers: Launcher intent (android.intent.action.MAIN)
- Responsibilities: Display list of reminders, handle swipe-to-delete, permission checks, observer setup

**Upsert Activity:**
- Location: `app/src/main/java/com/ava/notiva/UpsertReminderActivity.java`
- Triggers: Explicit intent from MainActivity (create/edit flows)
- Responsibilities: Collect reminder details, validate input, save via ViewModel

**Application Class:**
- Location: `app/src/main/java/com/ava/notiva/ReminderApplication.java`
- Triggers: Process startup (before any activities)
- Responsibilities: Initialize Dagger Hilt, configure WorkManager, enqueue periodic ReminderTriggerWorker

**Boot Receiver:**
- Location: `app/src/main/java/com/ava/notiva/service/BootReceiver.java`
- Triggers: System boot completion (android.intent.action.BOOT_COMPLETED)
- Responsibilities: Re-enqueue WorkManager periodic task after device restart

**Reminder Trigger Worker:**
- Location: `app/src/main/java/com/ava/notiva/service/ReminderTriggerWorker.java`
- Triggers: WorkManager periodic execution (every 1 minute)
- Responsibilities: Fetch all active reminders, calculate next occurrences, schedule exact alarms via AlarmManager

**Notification Starter Service:**
- Location: `app/src/main/java/com/ava/notiva/service/NotificationStarterService.java`
- Triggers: Exact alarm (PendingIntent from ReminderTriggerWorker)
- Responsibilities: Play alarm sound, vibrate device, post foreground notification with actions

**Notification Stopper Service:**
- Location: `app/src/main/java/com/ava/notiva/service/NotificationStopperService.java`
- Triggers: User taps dismiss/snooze on notification
- Responsibilities: Stop sound/vibration, cancel notification, reschedule if snooze

## Error Handling

**Strategy:** Defensive logging with try-catch blocks at operation boundaries

**Patterns:**

- **Repository Layer:** Catches exceptions during DAO operations, logs with TAG, returns gracefully (optional callback accepts -1 on failure)
- **ViewModel Layer:** No explicit error handling; exceptions bubble to Activity observers
- **Activity Layer:** Try-catch wraps initialization and event handlers, shows Toast messages on error
- **Service Layer:** Try-catch in onStartCommand and doWork, logs errors, returns Result.failure() or continues
- **Worker Layer:** Returns Result.failure() on exception, allowing WorkManager retry policy

Example (ReminderRepository.java):
```java
public void addWithCallback(ReminderModel model, Consumer<Long> callback) {
  reminderDaoExecutor.submit(() -> {
    long id = -1;
    try {
      id = reminderDao.add(model);
      Log.i(TAG, "Added reminder (async): " + model + ", id: " + id);
    } catch (Exception e) {
      Log.e(TAG, "Error while adding the reminder (async): " + model, e);
    }
    if (callback != null) {
      callback.accept(id);
    }
  });
}
```

## Cross-Cutting Concerns

**Logging:**

- Framework: Android `android.util.Log`
- Approach: Tag-based logging with package/class prefix (e.g., "Notiva.MainActivity", "ReminderTriggerWorker")
- Locations: `MainActivity.java:40`, `ReminderTriggerWorker.java:28`, `ReminderRepository.java:16`

**Validation:**

- Input validation in Activities before ViewModel calls (e.g., reminder name empty check in UpsertReminderActivity)
- Date/time constraint enforcement via MaterialDatePicker and custom validation listeners
- Recurrence delay range validation via `InputFilterMinMax` on EditText

**Authentication:**

- Not applicable (single-user local app)

**Permissions:**

- Runtime permissions checked in MainActivity: SCHEDULE_EXACT_ALARM, POST_NOTIFICATIONS
- Manifest declares required permissions: VIBRATE, FOREGROUND_SERVICE, FOREGROUND_SERVICE_MEDIA_PLAYBACK
- Graceful degradation if permissions denied (Toast warnings)

**Threading:**

- Presentation: Android Main Thread (UI operations in Activities/Services)
- Data: Fixed thread pool ExecutorService (ReminderDaoExecutor with pool size = CPU count)
- Background: WorkManager thread pool (ReminderTriggerWorker)
- Notification playback: Handler/Looper on main thread (MediaPlayer safety)

---

*Architecture analysis: 2026-02-04*
