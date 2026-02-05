# Bugs Found in Notiva Reminder App

This document lists bugs identified through code analysis of the reminder application.

**Severity Criteria:**
- **Critical**: App crashes or core functionality broken
- **Medium**: Incorrect behavior or potential crashes
- **Low**: Minor issues or code quality concerns

---

## Critical Bugs (ALL FIXED)

### 1. FOREVER Recurrence Causes ArithmeticException (Division by Zero)
**Status:** ✅ FIXED
**Commit:** `6206581` fix(bug-1): handle FOREVER recurrence to prevent ArithmeticException
**File:** `ReminderModel.java:132-134`

**Description:** When calculating the next occurrence for a `FOREVER` recurrence type with a past start date, the code threw `ArithmeticException`:
```java
long interval = recurrenceType.getMillis() * recurrenceDelay;  // interval = 0 for FOREVER
// ...
long intervalsPassed = (nowMillis - startMillis) / interval;   // Division by zero!
```

**Root Cause:** `RecurrenceType.FOREVER.getMillis()` returns `0`, causing `interval` to be `0`. When the start date is in the past, the division `(nowMillis - startMillis) / interval` threw `ArithmeticException`.

**Impact:** App crashed when viewing or processing a reminder with FOREVER recurrence and a past start date. The `ReminderTriggerWorker` background task would crash, **preventing ALL reminders from firing**.

**Decision Making:**
- **Option A (Chosen):** Treat FOREVER like NEVER - return start time if future, null if past. This is semantically correct since FOREVER means "no end date" not "repeat forever with zero interval".
- **Option B (Rejected):** Make FOREVER return a non-zero interval (e.g., 1 day). Rejected because it changes the semantics of the enum.

**Fix Applied:**
```java
// NEVER/FOREVER have getMillis()=0, treat as one-time reminders
if (recurrenceType == RecurrenceType.NEVER || recurrenceType == RecurrenceType.FOREVER || recurrenceDelay <= 0) {
  return startDateTime.after(now) ? (Calendar) startDateTime.clone() : null;
}
```

**Tests:** `ReminderModelTest.getNextOccurrence_foreverRecurrence_withPastStart_returnsNull()`

---

### 2. NumberFormatException in RecurrenceDelayChangedListener
**Status:** ✅ FIXED
**Commit:** `c2c2913` fix(bug-2): handle empty/invalid input in RecurrenceDelayChangedListener
**File:** `RecurrenceDelayChangedListener.java:27-35`

**Description:** No validation before parsing user input:
```java
@Override
public void afterTextChanged(Editable s) {
  reminder.setRecurrenceDelay(Integer.parseInt(s.toString()));  // Crashes on empty!
  summaryUpdater.run();
}
```

**Impact:** App crashed immediately when user cleared the recurrence delay field because `Integer.parseInt("")` throws `NumberFormatException`.

**Decision Making:**
- Keep previous value when input is invalid (don't reset to 0 or default)
- Still call summaryUpdater to keep UI in sync
- Use try-catch for robustness against any invalid input (not just empty)

**Fix Applied:**
```java
@Override
public void afterTextChanged(Editable s) {
  String text = s.toString().trim();
  if (!text.isEmpty()) {
    try {
      reminder.setRecurrenceDelay(Integer.parseInt(text));
    } catch (NumberFormatException ignored) {
      // Invalid input, keep previous value
    }
  }
  summaryUpdater.run();
}
```

**Tests:** `RecurrenceDelayChangedListenerTest.afterTextChanged_emptyString_keepsPreviousValue()`

---

### 3. PendingIntent Request Code Collision
**Status:** ✅ FIXED
**Commit:** `fab8ba6` fix(bug-3): use unique request codes for PendingIntents
**File:** `NotificationStarterService.java:127,140`

**Description:** Both Snooze and Dismiss actions used request code `0` for their PendingIntents:
```java
PendingIntent.getService(this, 0, dismissIntent, FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT);
PendingIntent.getService(this, 0, snoozeIntent, FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT);
```

**Impact:** Using the same request code with `FLAG_UPDATE_CURRENT` caused the second PendingIntent to overwrite the first. Both notification buttons triggered the same action (snooze, since it was created last). **Core notification functionality was broken.**

**Decision Making:**
- Use `notificationId` as base for request codes to ensure uniqueness per notification
- Dismiss: `notificationId` (offset 0)
- Snooze button: `notificationId + 1000000` (offset 1M)
- Snooze alarm: `notificationId + 2000000` (offset 2M, added in Bug #4 fix)
- Large offsets prevent collision even with many reminders

**Fix Applied:**
```java
// Dismiss action
PendingIntent.getService(this, notificationId, dismissIntent, FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT);

// Snooze action
PendingIntent.getService(this, notificationId + 1000000, snoozeIntent, FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT);
```

**Tests:** `BugVerificationInstrumentedTest.bug3_pendingIntent_uniqueRequestCodes()`

---

### 4. Snooze Doesn't Actually Delay for 10 Minutes
**Status:** ✅ FIXED
**Commits:**
- `a128c1b` fix(bug-4): implement actual 10-minute snooze delay using AlarmManager
- `9b781e2` fix(bug-4): add snooze tracking to prevent duplicate alarms
**File:** `NotificationStopperService.java`

**Description:** The snooze action claimed to delay for 10 minutes but didn't:
```java
if (ACTION_SNOOZE.equals(action)) {
  ReminderWorkerUtils.enqueueReminderWorker(getApplicationContext());
  Toast.makeText(..., "Reminder snoozed for 10 minutes", ...).show();
}
```

**Impact:** The snooze button immediately re-enqueued the worker which calculated next occurrences based on the original schedule, not adding any delay. The toast message was misleading. **Core snooze functionality was broken.**

**Decision Making - Phase 1 (Basic Fix):**
- Use `AlarmManager.setExactAndAllowWhileIdle()` to schedule precise alarm
- Calculate snooze time as `System.currentTimeMillis() + 10 minutes`
- Use unique request code (`notificationId + 2000000`) to avoid conflicts

**Decision Making - Phase 2 (Duplicate Prevention):**
- **Problem identified:** If a reminder repeats every 5 minutes and user snoozes for 10 minutes, both the regular alarm AND snooze alarm would fire.
- **Option A (Chosen):** Track snooze state in database with `snoozedUntil` field. Worker skips snoozed reminders.
- **Option B (Rejected):** Temporarily disable the reminder. Too hacky, could leave reminder in disabled state if something goes wrong.
- **Option C (Rejected):** Accept current behavior. Not acceptable for short-interval reminders.

**Database Changes:**
- Added `snoozed_until` column to `reminders` table (migration v1→v2)
- Added `isSnoozed()` helper method to `ReminderModel`
- Added `updateSnoozedUntil()` method to DAO and Repository

**Fix Applied:**
```java
private void scheduleSnoozeAlarm(Intent intent) {
  int reminderId = intent.getIntExtra(REMINDER_ID, -1);
  long snoozeTime = System.currentTimeMillis() + (10 * 60 * 1000L);

  // Mark reminder as snoozed in database
  reminderDao.updateSnoozedUntil(reminderId, snoozeTime);

  // Schedule exact alarm
  alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent);
}

// In ReminderTriggerWorker:
if (reminder.isSnoozed()) {
  continue;  // Skip snoozed reminders
}

// In NotificationStarterService (when alarm fires):
reminderDao.updateSnoozedUntil(notificationId, null);  // Clear snooze state
```

**Tests:** `BugVerificationInstrumentedTest.bug4_snoozeAction_schedulesCorrectDelay()`

---

### 5. Missing RECEIVE_BOOT_COMPLETED Permission
**Status:** ✅ FIXED
**Commit:** `f51915a` fix(bug-5): add RECEIVE_BOOT_COMPLETED permission to manifest
**File:** `AndroidManifest.xml`

**Description:** The BootReceiver was registered with `BOOT_COMPLETED` intent filter but the `RECEIVE_BOOT_COMPLETED` permission was not declared in the manifest.

**Verified:** Confirmed missing - manifest only declared VIBRATE, FOREGROUND_SERVICE, SCHEDULE_EXACT_ALARM, USE_EXACT_ALARM, and POST_NOTIFICATIONS.

**Impact:** The app would not receive boot completed broadcasts on most devices, causing **reminders to not resume after device reboot**. Users would need to manually open the app to restore reminder scheduling.

**Decision Making:**
- Simple one-line fix, no alternatives considered
- Permission is required by Android for receiving BOOT_COMPLETED broadcasts

**Fix Applied:**
```xml
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

**Tests:** `BugVerificationInstrumentedTest.bug5_bootPermission_isDeclaredInManifest()`

---

## Medium Bugs

### 6. Potential NullPointerException in NotificationStarterService
**Status:** ✅ FIXED
**Commit:** `25c9a4d` fix: resolve bugs #6, #7, #12, #13, #14
**File:** `NotificationStarterService.java:79-85`

**Description:** The intent is accessed without null check:
```java
@Override
public int onStartCommand(Intent intent, int flags, int startId) {
  notificationId = intent.getIntExtra(REMINDER_ID, -1);  // NPE if intent is null
```

**Impact:** In edge cases (e.g., service restart by system after being killed), `intent` can be null, causing a crash.

**Fix Applied:**
```java
if (intent == null) {
  Log.w(TAG, "onStartCommand received null intent, stopping service");
  stopSelf();
  return START_NOT_STICKY;
}
```

---

### 7. NullPointerException in ReminderModel.equals()
**Status:** ✅ FIXED
**Commit:** `25c9a4d` fix: resolve bugs #6, #7, #12, #13, #14
**File:** `ReminderModel.java:194`

**Description:** Inconsistent null handling in equals():
```java
&& Objects.equals(name, that.name)
&& startDateTime.equals(that.startDateTime)  // Can NPE if startDateTime is null
&& recurrenceType == that.recurrenceType
&& Objects.equals(endDateTime, that.endDateTime);
```

**Impact:** If `startDateTime` is null, calling `equals()` throws NPE. Other fields use `Objects.equals()` for null safety but this one doesn't.

**Fix Applied:** Changed to `Objects.equals(startDateTime, that.startDateTime)`.

---

### 8. Inaccurate Month/Year Duration Calculations
**Status:** ✅ FIXED
**Commit:** `c27651d` fix: resolve remaining bugs #8, #9, #10, #11
**File:** `ReminderModel.java:143-185`

**Description:** Fixed millisecond values for variable-length periods:
```java
case MONTH -> 2_678_400_000L; // 31 days
case YEAR -> 31_622_400_000L; // 366 days
```

**Impact:**
- Month calculation uses 31 days, but months vary from 28-31 days, causing drift
- Year calculation uses 366 days (leap year), but most years have 365 days
- Over time, recurring reminders will shift from their intended schedule (e.g., "every month on the 15th" will drift)

**Fix Applied:** Refactored `getNextOccurrenceAfter()` to use `Calendar.add()` for MONTH/YEAR recurrence types. MINUTE/HOUR/DAY continue using millisecond math (fixed-length periods). Monthly reminder on the 15th now correctly stays on the 15th.

---

### 9. MaterialDatePicker Timezone Issue
**Status:** ✅ FIXED
**Commit:** `c27651d` fix: resolve remaining bugs #8, #9, #10, #11
**File:** `UpsertReminderActivity.java:258-282`

**Description:**
```java
datePicker.addOnPositiveButtonClickListener(selection -> {
  Calendar selected = Calendar.getInstance();
  selected.setTimeInMillis(selection);  // selection is in UTC
```

**Impact:** MaterialDatePicker returns dates as UTC midnight timestamps. Setting this directly can cause off-by-one-day errors depending on the device's timezone (e.g., user in UTC-5 selects Jan 15, but gets Jan 14).

**Fix Applied:** Extract date components using UTC timezone before applying to local Calendar:
```java
Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
utcCalendar.setTimeInMillis(selection);
dateTime.set(YEAR, utcCalendar.get(YEAR));
dateTime.set(MONTH, utcCalendar.get(MONTH));
dateTime.set(DATE, utcCalendar.get(DATE));
```

---

### 10. WorkManager Minimum Interval Ignored
**Status:** ✅ FIXED
**Commit:** `c27651d` fix: resolve remaining bugs #8, #9, #10, #11
**File:** `ReminderApplication.java:38-42`

**Description:**
```java
new PeriodicWorkRequest.Builder(ReminderTriggerWorker.class, 1, TimeUnit.MINUTES)
```

**Impact:** WorkManager enforces a minimum interval of 15 minutes for periodic work. The 1-minute interval is silently increased to 15 minutes. This means reminders may fire up to 15 minutes late, which could be significant for time-sensitive reminders.

**Fix Applied:** Changed interval to 15 minutes to be explicit about actual behavior. Added comment documenting WorkManager limitation:
```java
// WorkManager enforces a minimum interval of 15 minutes for periodic work.
new PeriodicWorkRequest.Builder(ReminderTriggerWorker.class, 15, TimeUnit.MINUTES)
```

---

### 11. Wrong Next Occurrence Displayed in List
**Status:** ✅ FIXED
**Commit:** `c27651d` fix: resolve remaining bugs #8, #9, #10, #11
**File:** `ReminderItemAdapter.java:62-68`

**Description:** The adapter displays `startDateTime` instead of the actual next occurrence:
```java
Calendar nextOccurrence = reminder.getStartDateTime();
String nextOccurrenceStr = DateTimeDisplayUtil.getFriendlyDateTimeSingleLine(context, nextOccurrence);
```

**Impact:** For recurring reminders, the displayed time shows the original start time, not when the reminder will actually fire next. This is confusing for users but the reminder still fires at the correct time.

**Fix Applied:** Use `getNextOccurrenceAfter()` to show actual next trigger time:
```java
Calendar nextOccurrence = reminder.getNextOccurrenceAfter(Calendar.getInstance());
if (nextOccurrence == null) {
  nextOccurrence = reminder.getStartDateTime();  // Fallback for expired/non-recurring
}
```

---

### 12. Vibration Pattern Doesn't Repeat
**Status:** ✅ FIXED
**Commit:** `25c9a4d` fix: resolve bugs #6, #7, #12, #13, #14
**File:** `NotificationStarterService.java:216`

**Description:** Vibration only plays once:
```java
vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
```

**Impact:** The `-1` means no repeat. For an alarm notification, vibration should continue until dismissed to properly get user attention, especially for important reminders.

**Fix Applied:** Changed to `VibrationEffect.createWaveform(pattern, 0)` to repeat from the beginning.

---

## Low Priority Issues

### 13. Typo in RecurrenceType Enum
**Status:** ✅ FIXED
**Commit:** `25c9a4d` fix: resolve bugs #6, #7, #12, #13, #14
**File:** `RecurrenceType.java:8`

**Description:**
```java
MONTH("Months(s)"),  // Should be "Month(s)"
```

**Impact:** The displayed text has an extra 's', showing "Months(s)" instead of "Month(s)". Cosmetic UI issue only.

**Fix Applied:** Changed to `MONTH("Month(s)")`.

---

### 14. Unnecessary endDateTime Initialization
**Status:** ✅ FIXED
**Commit:** `25c9a4d` fix: resolve bugs #6, #7, #12, #13, #14
**File:** `ReminderModel.java:45`

**Description:** Default constructor initializes `endDateTime` to current time:
```java
public ReminderModel() {
  this.active = true;
  this.recurrenceType = RecurrenceType.DAY;
  this.endDateTime = Calendar.getInstance();  // Unnecessary
  this.startDateTime = Calendar.getInstance();
}
```

**Impact:** Non-recurring reminders will have an `endDateTime` value set even though it's not used, potentially causing confusion in the data layer. Code quality concern.

**Fix Applied:** Changed to `this.endDateTime = null;  // Only set when recurrence end is explicitly configured`.

---

## Summary

| Severity | Total | Fixed | Remaining |
|----------|-------|-------|-----------|
| Critical | 5 | 5 | 0 |
| Medium | 7 | 7 | 0 |
| Low | 2 | 2 | 0 |

**Total bugs found: 14**
**Total bugs fixed: 14**
**Remaining: 0** ✅

---

## Fix History

| Date | Bug | Commit | Description |
|------|-----|--------|-------------|
| 2026-02-05 | #1 | `6206581` | Handle FOREVER recurrence to prevent ArithmeticException |
| 2026-02-05 | #2 | `c2c2913` | Handle empty/invalid input in RecurrenceDelayChangedListener |
| 2026-02-05 | #3 | `fab8ba6` | Use unique request codes for PendingIntents |
| 2026-02-05 | #4 | `a128c1b` | Implement actual 10-minute snooze delay using AlarmManager |
| 2026-02-05 | #4 | `9b781e2` | Add snooze tracking to prevent duplicate alarms |
| 2026-02-05 | #5 | `f51915a` | Add RECEIVE_BOOT_COMPLETED permission to manifest |
| 2026-02-05 | #6 | `25c9a4d` | Add null intent check in NotificationStarterService |
| 2026-02-05 | #7 | `25c9a4d` | Fix NPE in ReminderModel.equals() with null startDateTime |
| 2026-02-05 | #12 | `25c9a4d` | Fix vibration pattern to repeat continuously |
| 2026-02-05 | #13 | `25c9a4d` | Fix typo "Months(s)" to "Month(s)" |
| 2026-02-05 | #14 | `25c9a4d` | Initialize endDateTime to null by default |
| 2026-02-05 | #8 | `c27651d` | Use Calendar.add() for accurate month/year recurrence |
| 2026-02-05 | #9 | `c27651d` | Fix MaterialDatePicker UTC timezone issue |
| 2026-02-05 | #10 | `c27651d` | Use explicit 15-minute WorkManager interval |
| 2026-02-05 | #11 | `c27651d` | Show actual next occurrence in reminder list |

---

## GitHub Issue Template

For each remaining bug, create an issue using this template:

```
Title: [Bug #X] Brief description

**Severity:** Medium/Low
**File:** `filename.java:line`

**Description:**
[Copy from bug description above]

**Impact:**
[Copy from impact section above]

**Proposed Fix:**
[Copy from proposed fix section above]

**Labels:** bug, [severity]
```
