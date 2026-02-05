# Bugs Found in Notiva Reminder App

This document lists bugs identified through code analysis of the reminder application.

**Severity Criteria:**
- **Critical**: App crashes or core functionality broken
- **Medium**: Incorrect behavior or potential crashes
- **Low**: Minor issues or code quality concerns

---

## Critical Bugs

### 1. FOREVER Recurrence Causes ArithmeticException (Division by Zero)
**File:** `ReminderModel.java:143`

**Description:** When calculating the next occurrence for a `FOREVER` recurrence type with a past start date, the code throws `ArithmeticException`:
```java
long interval = recurrenceType.getMillis() * recurrenceDelay;  // interval = 0 for FOREVER
// ...
long intervalsPassed = (nowMillis - startMillis) / interval;   // Division by zero!
```

**Root Cause:** `RecurrenceType.FOREVER.getMillis()` returns `0`, causing `interval` to be `0`. When the start date is in the past, the division `(nowMillis - startMillis) / interval` throws `ArithmeticException`.

**Impact:** App crashes when viewing or processing a reminder with FOREVER recurrence and a past start date. The `ReminderTriggerWorker` background task will crash, **preventing ALL reminders from firing**.

**Reproduction:**
1. Create a reminder with FOREVER recurrence
2. Set start date to the past
3. App crashes when calculating next occurrence

**Fix:** Handle FOREVER specially before the division:
```java
if (recurrenceType == RecurrenceType.FOREVER) {
  // FOREVER means repeat indefinitely with some default interval (e.g., daily)
  interval = RecurrenceType.DAY.getMillis() * recurrenceDelay;
}
```

Or check for zero interval:
```java
if (interval <= 0) {
  return startDateTime.after(now) ? (Calendar) startDateTime.clone() : null;
}
```

**Discovered by:** Unit test `ReminderModelTest.getNextOccurrence_foreverRecurrence_withPastStart_throwsArithmeticException`

---

### 2. NumberFormatException in RecurrenceDelayChangedListener
**File:** `RecurrenceDelayChangedListener.java:29`

**Description:** No validation before parsing:
```java
@Override
public void afterTextChanged(Editable s) {
  reminder.setRecurrenceDelay(Integer.parseInt(s.toString()));
  summaryUpdater.run();
}
```

**Impact:** App crashes immediately when user clears the recurrence delay field because `Integer.parseInt("")` throws `NumberFormatException`.

**Fix:** Add try-catch or check for empty/null before parsing:
```java
String text = s.toString().trim();
if (!text.isEmpty()) {
  try {
    reminder.setRecurrenceDelay(Integer.parseInt(text));
  } catch (NumberFormatException ignored) {}
}
```

---

### 3. PendingIntent Request Code Collision
**File:** `NotificationStarterService.java:127,140`

**Description:** Both Snooze and Dismiss actions use request code `0` for their PendingIntents:
```java
PendingIntent.getService(this, 0, dismissIntent, FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT);  // line 127
PendingIntent.getService(this, 0, snoozeIntent, FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT);   // line 140
```

**Impact:** Using the same request code with `FLAG_UPDATE_CURRENT` causes the second PendingIntent to overwrite the first. Both notification buttons will trigger the same action (whichever was created last). **Core notification functionality is broken.**

**Fix:** Use unique request codes, e.g., `notificationId` for dismiss and `notificationId + 1000000` for snooze.

---

### 4. Snooze Doesn't Actually Delay for 10 Minutes
**File:** `NotificationStopperService.java:30-32`

**Description:** The snooze action claims to delay for 10 minutes but doesn't:
```java
if (ACTION_SNOOZE.equals(action)) {
  ReminderWorkerUtils.enqueueReminderWorker(getApplicationContext());
  android.widget.Toast.makeText(getApplicationContext(), "Reminder snoozed for 10 minutes", ...).show();
}
```

**Impact:** The snooze button immediately re-enqueues the worker which calculates next occurrences based on the original schedule, not adding any delay. The toast message is misleading. **Core snooze functionality is broken.**

**Fix:** Schedule a one-time AlarmManager alarm for 10 minutes in the future instead of immediately enqueueing the worker.

---

### 5. Missing RECEIVE_BOOT_COMPLETED Permission
**File:** `AndroidManifest.xml`

**Description:** The BootReceiver is registered with `BOOT_COMPLETED` intent filter but the `RECEIVE_BOOT_COMPLETED` permission is not declared in the manifest.

**Verified:** Confirmed missing - manifest only declares VIBRATE, FOREGROUND_SERVICE, SCHEDULE_EXACT_ALARM, USE_EXACT_ALARM, and POST_NOTIFICATIONS.

**Impact:** The app will not receive boot completed broadcasts on most devices, causing **reminders to not resume after device reboot**. Users would need to manually open the app to restore reminder scheduling.

**Fix:** Add to manifest:
```xml
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

---

## Medium Bugs

### 6. Potential NullPointerException in NotificationStarterService
**File:** `NotificationStarterService.java:71`

**Description:** The intent is accessed without null check:
```java
@Override
public int onStartCommand(Intent intent, int flags, int startId) {
  notificationId = intent.getIntExtra(REMINDER_ID, -1);  // NPE if intent is null
```

**Impact:** In edge cases (e.g., service restart by system after being killed), `intent` can be null, causing a crash.

**Fix:** Add null check:
```java
if (intent == null) {
  stopSelf();
  return START_NOT_STICKY;
}
```

---

### 7. NullPointerException in ReminderModel.equals()
**File:** `ReminderModel.java:177`

**Description:** Inconsistent null handling in equals():
```java
&& Objects.equals(name, that.name)
&& startDateTime.equals(that.startDateTime)  // Can NPE if startDateTime is null
&& recurrenceType == that.recurrenceType
&& Objects.equals(endDateTime, that.endDateTime);
```

**Impact:** If `startDateTime` is null, calling `equals()` throws NPE. Other fields use `Objects.equals()` for null safety but this one doesn't.

**Fix:** Change to `Objects.equals(startDateTime, that.startDateTime)`.

---

### 8. Inaccurate Month/Year Duration Calculations
**File:** `RecurrenceType.java:47-48`

**Description:** Fixed millisecond values for variable-length periods:
```java
case MONTH -> 2_678_400_000L; // 31 days
case YEAR -> 31_622_400_000L; // 366 days
```

**Impact:**
- Month calculation uses 31 days, but months vary from 28-31 days, causing drift
- Year calculation uses 366 days (leap year), but most years have 365 days
- Over time, recurring reminders will shift from their intended schedule (e.g., "every month on the 15th" will drift)

**Fix:** Use `Calendar.add()` to properly handle date arithmetic instead of fixed milliseconds.

---

### 9. MaterialDatePicker Timezone Issue
**File:** `UpsertReminderActivity.java:257-259`

**Description:**
```java
datePicker.addOnPositiveButtonClickListener(selection -> {
  Calendar selected = Calendar.getInstance();
  selected.setTimeInMillis(selection);  // selection is in UTC
```

**Impact:** MaterialDatePicker returns dates as UTC midnight timestamps. Setting this directly can cause off-by-one-day errors depending on the device's timezone (e.g., user in UTC-5 selects Jan 15, but gets Jan 14).

**Fix:** Use `MaterialDatePicker.todayInUtcMilliseconds()` approach or extract date components separately.

---

### 10. WorkManager Minimum Interval Ignored
**File:** `ReminderApplication.java:38`

**Description:**
```java
new PeriodicWorkRequest.Builder(ReminderTriggerWorker.class, 1, TimeUnit.MINUTES)
```

**Impact:** WorkManager enforces a minimum interval of 15 minutes for periodic work. The 1-minute interval is silently increased to 15 minutes. This means reminders may fire up to 15 minutes late, which could be significant for time-sensitive reminders.

**Fix:** Document this limitation or use AlarmManager for more frequent/precise scheduling.

---

### 11. Wrong Next Occurrence Displayed in List
**File:** `ReminderItemAdapter.java:62-63`

**Description:** The adapter displays `startDateTime` instead of the actual next occurrence:
```java
Calendar nextOccurrence = reminder.getStartDateTime();
String nextOccurrenceStr = DateTimeDisplayUtil.getFriendlyDateTimeSingleLine(context, nextOccurrence);
```

**Impact:** For recurring reminders, the displayed time shows the original start time, not when the reminder will actually fire next. This is confusing for users but the reminder still fires at the correct time.

**Fix:** Use `reminder.getNextOccurrenceAfter(Calendar.getInstance())` to get the actual next trigger time.

---

### 12. Vibration Pattern Doesn't Repeat
**File:** `NotificationStarterService.java:187`

**Description:** Vibration only plays once:
```java
vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
```

**Impact:** The `-1` means no repeat. For an alarm notification, vibration should continue until dismissed to properly get user attention, especially for important reminders.

**Fix:** Use a positive repeat index: `VibrationEffect.createWaveform(pattern, 0)` to repeat from the beginning.

---

## Low Priority Issues

### 13. Typo in RecurrenceType Enum
**File:** `RecurrenceType.java:8`

**Description:**
```java
MONTH("Months(s)"),  // Should be "Month(s)"
```

**Impact:** The displayed text has an extra 's', showing "Months(s)" instead of "Month(s)". Cosmetic UI issue only.

**Fix:** Change to `MONTH("Month(s)")`.

---

### 14. Unnecessary endDateTime Initialization
**File:** `ReminderModel.java:42-43`

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

**Fix:** Only initialize `endDateTime` when recurrence is explicitly enabled, or set to `null` by default.

---

## Summary

| Severity | Count | Bugs |
|----------|-------|------|
| Critical | 5 | #1 (ArithmeticException), #2 (NumberFormatException), #3 (PendingIntent), #4 (Snooze), #5 (Boot Permission) |
| Medium | 7 | #6-12 (NPEs, date drift, timezone, WorkManager, display, vibration) |
| Low | 2 | #13 (Typo), #14 (Initialization) |

**Total bugs found: 14**

### Recommended Fix Order

**Immediate (blocks core functionality):**
1. Bug #1 - ArithmeticException crashes the worker, blocking ALL reminders
2. Bug #5 - One-line fix, restores reminders after reboot
3. Bug #3 - PendingIntent collision breaks notification buttons
4. Bug #4 - Snooze feature completely non-functional

**Soon (crashes or significant issues):**
5. Bug #2 - NumberFormatException on user input
6. Bug #6 - NPE on service restart
7. Bug #7 - NPE in equals()

**Later (correctness improvements):**
8. Bug #8-12 - Date accuracy, display, and UX issues

**When convenient:**
9. Bug #13-14 - Cosmetic and code quality
