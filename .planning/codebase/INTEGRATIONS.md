# External Integrations

**Analysis Date:** 2026-02-04

## APIs & External Services

**None currently integrated.**
- No external API calls, third-party SDKs, or cloud service integrations detected
- All functionality is self-contained within the application

## Data Storage

**Databases:**
- SQLite (via Room ORM)
  - Database name: `Reminders-DB`
  - Connection: Managed by `androidx.room:room-runtime:2.8.1`
  - Client/ORM: Room 2.8.1
  - Configuration: `app/src/main/java/com/ava/notiva/module/DbModule.java`
  - Database class: `app/src/main/java/com/ava/notiva/data/RemindersDb.java`
  - Schema version: 1
  - Export schema: Disabled (no schema export file generated)

**File Storage:**
- Local filesystem only
- Alarm audio resource: `R.raw.alarm` (stored in `app/src/main/res/raw/`)

**Caching:**
- None - Application loads all data directly from SQLite on each operation

## Authentication & Identity

**Auth Provider:**
- Not applicable - No user authentication required
- Single-user local application with no account management

## Monitoring & Observability

**Error Tracking:**
- None - No external error tracking service integrated

**Logs:**
- Android Logcat logging via `android.util.Log`
- Log tags used throughout codebase for categorization:
  - `ReminderTriggerWorker` - Background job logging
  - `Notiva.NotificationStarterService` - Notification service logging
  - `Notiva.ReminderRepository` - Data operation logging
  - `BootReceiver` - Device boot event logging

## CI/CD & Deployment

**Hosting:**
- Not applicable - Native Android application
- Distribution: Manual APK build or Google Play Store

**CI Pipeline:**
- None detected - No CI/CD configuration files found

## Environment Configuration

**Required env vars:**
- None - All configuration is hardcoded or managed through Gradle properties

**Secrets location:**
- `local.properties` - Contains Android SDK path (local development only)
- No remote secrets management in use

## Webhooks & Callbacks

**Incoming:**
- None - Application does not expose HTTP endpoints or receive remote webhooks

**Outgoing:**
- None - Application does not call out to remote webhook services

## Internal Service Communications

**System Broadcasts:**
- `android.intent.action.BOOT_COMPLETED` - Device boot event received by `BootReceiver` (`app/src/main/java/com/ava/notiva/service/BootReceiver.java`)
  - Triggers periodic WorkManager job for reminder scheduling on device restart

**System Services Used:**
- `Context.ALARM_SERVICE` (AlarmManager) - Exact alarm scheduling in `ReminderTriggerWorker`
- `Context.VIBRATOR_MANAGER_SERVICE` (VibratorManager) - Vibration feedback in `NotificationStarterService`
- `Context.NOTIFICATION_SERVICE` (NotificationManager) - System notification display

**WorkManager Integration:**
- Periodic work: `ReminderTriggerWorker` executes every 1 minute
- Job ID: `ReminderSync`
- Configured in: `ReminderApplication.onCreate()` and `BootReceiver.onReceive()`
- Dependency: `androidx.work:work-runtime:2.10.5` with Hilt support (`androidx.hilt:hilt-work:1.3.0`)

## Data Flow Summary

1. **Reminder Creation**: User creates reminder via UI → saved to SQLite via `ReminderRepository` (`app/src/main/java/com/ava/notiva/data/ReminderRepository.java`)
2. **Periodic Scheduling**: WorkManager runs `ReminderTriggerWorker` every 1 minute → loads all active reminders from SQLite → schedules exact alarms via `AlarmManager`
3. **Reminder Trigger**: `AlarmManager` fires `PendingIntent` → launches `NotificationStarterService` → displays notification with sound and vibration
4. **User Interaction**: User dismisses or snoozes → `NotificationStopperService` called → notification removed

---

*Integration audit: 2026-02-04*
