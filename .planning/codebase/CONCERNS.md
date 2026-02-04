# Codebase Concerns

**Analysis Date:** 2026-02-04

## Tech Debt

**Duplicate Recurrence Switch Logic:**
- Issue: `RecurrenceSwitchListener` at `app/src/main/java/com/ava/notiva/listener/RecurrenceSwitchListener.java` exists but is not used; logic is duplicated inline in `UpsertReminderActivity.initRecurrenceSwitchListener()` (lines 282-306).
- Files: `app/src/main/java/com/ava/notiva/listener/RecurrenceSwitchListener.java` (TODO comment at line 10), `app/src/main/java/com/ava/notiva/UpsertReminderActivity.java` (lines 282-306)
- Impact: Maintenance burden; changes to recurrence switch behavior must be made in two places; code reusability is compromised.
- Fix approach: Remove inline logic from `UpsertReminderActivity` and integrate `RecurrenceSwitchListener` into the initialization, passing the necessary callbacks.

**Large Activity Class:**
- Issue: `UpsertReminderActivity.java` is 549 lines with mixed concerns: state management, UI initialization, date/time handling, and validation all in one class.
- Files: `app/src/main/java/com/ava/notiva/UpsertReminderActivity.java` (entire file)
- Impact: Difficult to test, high cognitive complexity, harder to maintain, changes in one concern ripple across the entire activity.
- Fix approach: Extract date/time picker setup into a separate manager class, create a dedicated validator class for recurrence validation, move listener setup to dedicated listener classes.

**Hardcoded Snooze Duration:**
- Issue: Snooze duration is hardcoded as "10 minutes" in notification dismiss action (line 32 of `NotificationStopperService.java`) and as 1 minute in periodic worker (line 21 of `BootReceiver.java` and line 38 of `ReminderApplication.java`).
- Files: `app/src/main/java/com/ava/notiva/service/NotificationStopperService.java` (line 32), `app/src/main/java/com/ava/notiva/service/BootReceiver.java` (line 21), `app/src/main/java/com/ava/notiva/ReminderApplication.java` (line 38)
- Impact: Snoozed reminders use one duration, periodic sync uses another; values cannot be configured; message shows "10 minutes" but no actual snooze scheduling is implemented.
- Fix approach: Extract durations to `ReminderConstants.java` with configurable values; implement actual snooze scheduling logic.

**Missing Null Safety in Callbacks:**
- Issue: `ReminderRepository.addWithCallback()` (line 36) and `ReminderDmlViewModel.addReminderWithCallback()` execute callbacks without null checks after async operations. If callback is null, operation succeeds silently.
- Files: `app/src/main/java/com/ava/notiva/data/ReminderRepository.java` (lines 27-40), `app/src/main/java/com/ava/notiva/data/ReminderDmlViewModel.java` (line 33)
- Impact: Callbacks with side effects (like `ReminderWorkerUtils.enqueueReminderWorker()`) may not execute even if data is saved; callers can't distinguish between success and callback failure.
- Fix approach: Make callback required (non-null), or return a result/LiveData from the add operation instead of using callbacks.

## Known Issues

**Snooze Feature Not Fully Implemented:**
- Symptoms: When user taps "Snooze" in notification, UI shows "Reminder snoozed for 10 minutes" but no actual 10-minute delay is scheduled. The next alarm still uses the original recurrence schedule.
- Files: `app/src/main/java/com/ava/notiva/service/NotificationStopperService.java` (line 31), `app/src/main/java/com/ava/notiva/util/ReminderWorkerUtils.java`
- Trigger: User receives reminder notification, taps "Snooze" button
- Current behavior: Alarm is dismissed, periodic worker re-enqueued immediately without delay, no actual snooze is applied
- Workaround: None. User must wait for original recurrence schedule or manually reschedule.

**End Date/Time Not Set for New Recurring Reminders:**
- Symptoms: When creating a new reminder with recurrence, if recurrence is enabled but user doesn't explicitly set an end date, the end date is set to current time (line 42 of `ReminderModel.java`), making the reminder expire immediately.
- Files: `app/src/main/java/com/ava/notiva/model/ReminderModel.java` (lines 39-44), `app/src/main/java/com/ava/notiva/UpsertReminderActivity.java` (lines 290-297)
- Trigger: Create new reminder, enable recurrence, don't set end date, save
- Current behavior: Reminder saves with `endDateTime` set to today; `getNextOccurrenceAfter()` returns null if end date has passed (line 147-148 of `ReminderModel.java`)
- Workaround: User must manually set end date before saving.

**Missing Intent Data Validation:**
- Symptoms: `UpsertReminderActivity.buildReminderAndSetTitle()` (lines 150-169) assumes intent extras exist without validation. If the intent extra is missing, `getStringExtra()` or `getIntExtra()` return null/-1, and model construction may fail silently.
- Files: `app/src/main/java/com/ava/notiva/UpsertReminderActivity.java` (lines 150-169)
- Trigger: Activity is launched with incomplete intent extras (e.g., from crash restoration, malformed deep link)
- Current behavior: Fields may be null; `ReminderModel` constructor (lines 53-70) assumes non-null startTime/endTime values
- Workaround: None; activity crashes if critical data is missing.

**Calendar.clone() Type Safety:**
- Symptoms: Code uses `(Calendar) startDateTime.clone()` (line 133, 138 of `ReminderModel.java`, line 294 of `UpsertReminderActivity.java`) which requires casting. If `clone()` returns Object type, cast errors are possible at runtime.
- Files: `app/src/main/java/com/ava/notiva/model/ReminderModel.java` (lines 133, 138), `app/src/main/java/com/ava/notiva/UpsertReminderActivity.java` (line 294)
- Trigger: Runtime (if ever clone() behavior changes)
- Current behavior: Works but not type-safe; developers may forget cast, causing runtime errors
- Workaround: Code currently works; no workaround needed yet.

## Security Considerations

**Broadcast Receiver Without Permission Validation:**
- Risk: `BootReceiver` (line 55-62 of `AndroidManifest.xml`) is exported and listens to `BOOT_COMPLETED` action without additional permission validation. If another app on device broadcasts the action, reminders could be scheduled unexpectedly.
- Files: `app/src/main/java/com/ava/notiva/service/BootReceiver.java`, `app/src/main/AndroidManifest.xml` (line 58)
- Current mitigation: System-level `BOOT_COMPLETED` is protected by OS; external broadcasts are less likely to succeed.
- Recommendations: Change `android:exported="false"` in manifest; add explicit intent filter for system-level broadcasts only.

**Sensitive Data Logged:**
- Risk: Reminder IDs and names are logged throughout the codebase using `Log.i()` and `Log.e()`. In production builds, logs may be accessible via adb logcat or log files, exposing user reminder data.
- Files: `app/src/main/java/com/ava/notiva/UpsertReminderActivity.java` (lines 91, 106, 432-433, 441-447, 506-513, 525), `app/src/main/java/com/ava/notiva/ReminderTriggerWorker.java` (lines 60, 63), `app/src/main/java/com/ava/notiva/MainActivity.java` (lines 100, 120), `app/src/main/java/com/ava/notiva/data/ReminderRepository.java` (lines 32, 47, 59, 71)
- Current mitigation: Logs are debug-level; no additional masking is present.
- Recommendations: Use BuildConfig.DEBUG to disable sensitive logging in release builds; mask reminder names/IDs in log messages.

**No Input Validation on Reminder Names:**
- Risk: Reminder names are user-provided strings with no validation for length, special characters, or injection attacks. While unlikely in Android context, names are stored directly in database and displayed in UI without sanitization.
- Files: `app/src/main/java/com/ava/notiva/listener/ReminderNameChangedListener.java`, `app/src/main/java/com/ava/notiva/model/ReminderModel.java` (line 92)
- Current mitigation: Room database provides type safety; names are not used in queries or dynamic code generation.
- Recommendations: Add max length validation (e.g., 100 characters); trim whitespace; optionally validate against forbidden characters.

**Database Not Encrypted:**
- Risk: Room database (created at `app/src/main/java/com/ava/notiva/module/DbModule.java` line 39) is not encrypted. User's reminder data is stored in plaintext SQLite file at `/data/data/com.ava.notiva/databases/Reminders-DB`.
- Files: `app/src/main/java/com/ava/notiva/module/DbModule.java` (line 39)
- Current mitigation: File is in app-private data directory; requires root or device connection to access.
- Recommendations: Consider integrating SQLCipher for database encryption, especially if reminders contain sensitive information (e.g., medication schedules).

## Performance Bottlenecks

**Synchronous Database Queries on Main Thread:**
- Problem: `ReminderTriggerWorker.doWork()` (line 43 of `ReminderTriggerWorker.java`) calls `reminderRepository.getAllSync()`, which is a blocking Room query. This runs on a WorkManager thread pool, not main thread, so less critical than main thread access, but still blocks thread.
- Files: `app/src/main/java/com/ava/notiva/service/ReminderTriggerWorker.java` (line 43), `app/src/main/java/com/ava/notiva/data/ReminderRepository.java` (line 100)
- Cause: Database queries executed synchronously; no pagination or filtering applied.
- Improvement path: Return paginated results; consider loading only active reminders; transition to async LiveData queries.

**Fixed Thread Pool Size Based on Available Processors:**
- Problem: `DbModule.getReminderDaoExecutorService()` (line 33) creates a fixed thread pool with size = `Runtime.getRuntime().availableProcessors()`. On high-core devices, this creates many threads; on low-core devices, it's undersized.
- Files: `app/src/main/java/com/ava/notiva/module/DbModule.java` (line 33)
- Cause: Naive scaling without considering memory and I/O constraints.
- Improvement path: Use bounded queue thread pool (e.g., 2-4 threads) tuned for database workload; use `Executors.newFixedThreadPool(2)` or `newCachedThreadPool()` with queue limits.

**No Query Optimization in Periodic Worker:**
- Problem: `BootReceiver` and `ReminderApplication` both enqueue a 1-minute periodic `ReminderTriggerWorker` (lines 21 and 38, respectively). Worker fetches all reminders, iterates all, and schedules alarms. No indexing or query filtering on active/next-occurrence.
- Files: `app/src/main/java/com/ava/notiva/service/BootReceiver.java` (line 21), `app/src/main/java/com/ava/notiva/ReminderApplication.java` (line 38), `app/src/main/java/com/ava/notiva/service/ReminderTriggerWorker.java` (line 43)
- Cause: Naive full-table scan; no database indices for active reminders or next occurrence time.
- Improvement path: Add database index on `active` column; add query filter to only fetch active reminders; cache last sync time to avoid redundant alarms.

**Alarm Manager Called Once per Worker Iteration:**
- Problem: `ReminderTriggerWorker.doWork()` iterates all reminders (line 46) and calls `AlarmManager.setExactAndAllowWhileIdle()` for each (lines 55-59). On 100 reminders, this is 100 AlarmManager calls, each a system service IPC.
- Files: `app/src/main/java/com/ava/notiva/service/ReminderTriggerWorker.java` (lines 46-59)
- Cause: No batching or deduplication; no cancellation of old alarms before setting new ones.
- Improvement path: Only set alarms that have changed; batch or debounce alarm scheduling; cancel previously scheduled alarms for the same reminder.

## Fragile Areas

**Date/Time Handling with Calendar API:**
- Files: `app/src/main/java/com/ava/notiva/util/DateTimeDisplayUtil.java`, `app/src/main/java/com/ava/notiva/UpsertReminderActivity.java` (entire file), `app/src/main/java/com/ava/notiva/model/ReminderModel.java` (lines 128-151)
- Why fragile: Calendar API is mutable and error-prone. Code frequently clones Calendar objects, sets individual fields, and relies on timezone-aware comparisons. Logic in `getNextOccurrenceAfter()` assumes integer arithmetic for interval calculations, which fails for month/year recurrence due to variable-length months.
- Safe modification: Add unit tests for edge cases (e.g., daylight saving time transitions, month boundaries, leap years); consider switching to Java 8 `java.time` API (ZonedDateTime, Instant) which is immutable and handles DST correctly.
- Test coverage: No test coverage for date/time logic. Tests are minimal (example unit tests only).

**Recurrence Calculation Logic:**
- Files: `app/src/main/java/com/ava/notiva/model/ReminderModel.java` (lines 128-151)
- Why fragile: `getNextOccurrenceAfter()` uses integer interval multiplied by recurrence type milliseconds (line 143-144). This assumes constant-length intervals, but months and years vary. For example, a 1-month recurrence from Jan 31 is ambiguous (Feb 28/29? Mar 31?).
- Safe modification: Add explicit test cases for month/year boundaries; document expected behavior for ambiguous dates; consider switching to `LocalDateTime` and `Period` from `java.time` package.
- Test coverage: No unit tests for recurrence calculation.

**MediaPlayer and Vibrator Resource Management:**
- Files: `app/src/main/java/com/ava/notiva/service/NotificationStarterService.java` (lines 44-66, 77-86, 203-227, 229-237)
- Why fragile: MediaPlayer is reinitialized on `onStartCommand()` if null (line 78-86), but old instance may still be playing. Flag `mediaPlayerReleased` (line 49) is checked but may not prevent race conditions if `onDestroy()` and `onStartCommand()` overlap. `safelyStopAndReleaseMediaPlayer()` posts to main looper (line 207), introducing potential deadlocks.
- Safe modification: Use a WeakReference or lifecycle-aware holder; prevent re-initialization if already playing; use CountDownLatch to ensure synchronous release before setting null.
- Test coverage: No unit tests for service lifecycle.

**Activity Lifecycle and State Loss:**
- Files: `app/src/main/java/com/ava/notiva/UpsertReminderActivity.java` (entire file), `app/src/main/java/com/ava/notiva/MainActivity.java` (lines 113-134)
- Why fragile: Both activities use instance variables (`reminderModel`, `currentTime`, adapters) that are not saved/restored on configuration changes. If activity is destroyed and recreated (e.g., rotation), state is lost. `UpsertReminderActivity.onCreate()` rebuilds entire state from intent extras (line 86), which fails if extras are missing.
- Safe modification: Implement `onSaveInstanceState()`/`onRestoreInstanceState()` or use ViewModel with SavedStateHandle to persist state across configuration changes.
- Test coverage: No instrumented tests for activity lifecycle.

## Scaling Limits

**Periodic Worker Runs Every 1 Minute:**
- Current capacity: Works for typical reminder loads (e.g., <100 reminders)
- Limit: As reminder count grows, each 1-minute worker iteration becomes expensive (full table scan, AlarmManager calls for each reminder). At 1000+ reminders, this consumes significant CPU and battery.
- Scaling path: Implement smarter scheduling: (1) compute next occurrence per reminder and schedule only the nearest one, (2) use AlarmManager callbacks to reschedule after each alarm fires instead of polling, (3) use room query with index on `next_occurrence_time` column.

**Database Without Indices:**
- Current capacity: Works for <500 reminders
- Limit: Room queries `getAll()` and `getAllSync()` scan entire table. At 5000+ reminders, query latency increases linearly.
- Scaling path: Add database indices on frequently queried columns (`active`, `end_date`); add computed column `next_occurrence_time` with index; use Room's `@Query` with WHERE clauses to filter.

**ExecutorService Thread Pool Unbounded:**
- Current capacity: Works for typical usage
- Limit: Each insert/update/delete spawns a task on executor. If many operations enqueued rapidly, thread pool grows; each thread consumes memory.
- Scaling path: Cap thread pool size to 2-4 threads; use bounded queue; add metrics to monitor queue depth.

**Alarm Manager Register Limit:**
- Current capacity: Android allows ~500 alarms per app
- Limit: App is not documented to enforce a limit; if user creates 500+ reminders, new alarms silently fail.
- Scaling path: Document user-facing limit; check alarm capacity before inserting reminder; provide clear error message.

## Dependencies at Risk

**Room Database Schema Not Versioned:**
- Risk: `RemindersDb.java` (line 10) has `version = 1` and `exportSchema = false`. If future changes add columns or change types, Room cannot migrate; app will crash on upgrade.
- Impact: Any schema change (e.g., add `label` column, change `startDateTime` to `startTime + startDate`) breaks the app for existing users.
- Migration plan: (1) Enable `exportSchema = true` to track schema versions, (2) implement migration scripts using `Migration` class, (3) test migrations thoroughly before release.

**Dagger Hilt 2.57.2:**
- Risk: Dagger 2.57.2 is a stable version, but versions >2.50 have moved to Java 11+ requirements. If project is built with older toolchain, it may fail.
- Impact: Build failures if Java version is downgraded.
- Migration plan: Lock version in build.gradle; update project to Java 11+.

**Work Manager OneTime/Periodic Work Not Tested:**
- Risk: `ReminderWorkerUtils.enqueueReminderWorker()` (line 12) enqueues `OneTimeWorkRequest` without constraints or backoff. If device is offline or battery low, work is delayed; if app is force-stopped, work is lost.
- Impact: Reminders may not be scheduled reliably after device restart or app restart.
- Migration plan: Add retry policy (`setBackoffCriteria()`); add constraint to execute only when charging or on WiFi (if acceptable for user); test via WorkManager testing utilities.

## Missing Critical Features

**Snooze Feature Not Fully Implemented:**
- Problem: Notification has "Snooze" button, but pressing it doesn't actually snooze the reminder for 10 minutes; it just re-enqueues the periodic worker, which reschedules immediately.
- Blocks: Users cannot delay a reminder without dismissing it forever.
- Recommendation: Implement snooze by scheduling a one-time alarm 10 minutes in the future; clear the original alarm for that reminder until snooze expires.

**No Alarm Edit-While-Playing:**
- Problem: Once a reminder starts playing, user cannot open the app to edit or dismiss it directly from the UI; only notification actions are available.
- Blocks: User experience is fragmented; UI is unreachable during alarm.
- Recommendation: Ensure NotificationStarterService is accessible from UI; allow MainActivity to query active alarms and dismiss them.

**No Backup/Export of Reminders:**
- Problem: Reminders are stored only in local database; if app is uninstalled or device is lost, all data is gone.
- Blocks: Users cannot migrate reminders to a new device.
- Recommendation: Implement backup to cloud (Google Drive, Firebase) or export to CSV; add import functionality.

**No Quiet Hours or Do Not Disturb Integration:**
- Problem: Alarms sound and vibrate at all times, ignoring device Do Not Disturb settings or user-defined quiet hours.
- Blocks: Users cannot sleep without disabling reminders entirely.
- Recommendation: Integrate with `NotificationManager.getNotificationPolicy()` to respect device DND state; add in-app quiet hours settings.

## Test Coverage Gaps

**No Tests for Date/Time Logic:**
- What's not tested: `ReminderModel.getNextOccurrenceAfter()`, `DateTimeDisplayUtil` methods, timezone handling.
- Files: `app/src/main/java/com/ava/notiva/model/ReminderModel.java` (lines 128-151), `app/src/main/java/com/ava/notiva/util/DateTimeDisplayUtil.java`
- Risk: Bugs in recurrence calculation go unnoticed; timezone edge cases (DST transitions) are not validated.
- Priority: High

**No Tests for Alarm Scheduling:**
- What's not tested: `ReminderTriggerWorker.doWork()`, `BootReceiver`, AlarmManager integration.
- Files: `app/src/main/java/com/ava/notiva/service/ReminderTriggerWorker.java`, `app/src/main/java/com/ava/notiva/service/BootReceiver.java`
- Risk: Worker failures, permission issues, and system integration bugs are not caught.
- Priority: High

**No Tests for Activity Lifecycle:**
- What's not tested: `UpsertReminderActivity` onCreate/onSaveInstanceState, state persistence, configuration changes.
- Files: `app/src/main/java/com/ava/notiva/UpsertReminderActivity.java`
- Risk: State loss on rotation, null pointer exceptions from missing intent extras.
- Priority: Medium

**No Tests for Database Operations:**
- What's not tested: Room DAO queries, migrations, concurrent access.
- Files: `app/src/main/java/com/ava/notiva/data/ReminderDao.java`, `app/src/main/java/com/ava/notiva/data/RemindersDb.java`
- Risk: Database corruption, data loss, concurrent modification crashes.
- Priority: High

**No Tests for Service Lifecycle (MediaPlayer, Vibrator):**
- What's not tested: `NotificationStarterService` start/stop, MediaPlayer lifecycle, resource cleanup.
- Files: `app/src/main/java/com/ava/notiva/service/NotificationStarterService.java`
- Risk: Resource leaks, crashes from null/released resources.
- Priority: Medium

---

*Concerns audit: 2026-02-04*
