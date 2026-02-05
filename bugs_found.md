# Bugs Found in Notiva Reminder App

This document lists bugs identified through code analysis of the reminder application.

---

## Critical Bugs

### 1. PendingIntent Request Code Collision
**File:** `NotificationStarterService.java:127,140`

**Description:** Both Snooze and Dismiss actions use request code `0` for their PendingIntents:
```java
PendingIntent.getService(this, 0, dismissIntent, FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT);  // line 127
PendingIntent.getService(this, 0, snoozeIntent, FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT);   // line 140
```

**Impact:** Using the same request code with `FLAG_UPDATE_CURRENT` causes the second PendingIntent to overwrite the first. Both notification buttons will trigger the same action (whichever was created last).

**Fix:** Use unique request codes, e.g., `notificationId` for dismiss and `notificationId + 1000000` for snooze.

---

### 2. Snooze Doesn't Actually Delay for 10 Minutes
**File:** `NotificationStopperService.java:30-32`

**Description:** The snooze action claims to delay for 10 minutes but doesn't:
```java
if (ACTION_SNOOZE.equals(action)) {
  ReminderWorkerUtils.enqueueReminderWorker(getApplicationContext());
  android.widget.Toast.makeText(getApplicationContext(), "Reminder snoozed for 10 minutes", ...).show();
}
```

**Impact:** The snooze button immediately re-enqueues the worker which calculates next occurrences based on the original schedule, not adding any delay. The toast message is misleading.

**Fix:** Schedule a one-time AlarmManager alarm for 10 minutes in the future instead of immediately enqueueing the worker.

---

### 3. NumberFormatException in RecurrenceDelayChangedListener
**File:** `RecurrenceDelayChangedListener.java:29`

**Description:** No validation before parsing:
```java
@Override
public void afterTextChanged(Editable s) {
  reminder.setRecurrenceDelay(Integer.parseInt(s.toString()));
  summaryUpdater.run();
}
```

**Impact:** App crashes when user clears the recurrence delay field because `Integer.parseInt("")` throws `NumberFormatException`.

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

### 4. FOREVER Recurrence Causes ArithmeticException (Division by Zero)
**File:** `ReminderModel.java:143`

**Description:** When calculating the next occurrence for a `FOREVER` recurrence type with a past start date, the code throws `ArithmeticException`:
```java
long interval = recurrenceType.getMillis() * recurrenceDelay;  // interval = 0 for FOREVER
// ...
long intervalsPassed = (nowMillis - startMillis) / interval;   // Division by zero!
```

**Root Cause:** `RecurrenceType.FOREVER.getMillis()` returns `0`, causing `interval` to be `0`. When the start date is in the past, the division `(nowMillis - startMillis) / interval` throws `ArithmeticException`.

**Impact:** App crashes when viewing or processing a reminder with:
- `RecurrenceType.FOREVER`
- A start date in the past
- The `ReminderTriggerWorker` background task will crash, preventing all reminders from firing.

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

### 5. Wrong Next Occurrence Displayed in List
**File:** `ReminderItemAdapter.java:62-63`

**Description:** The adapter displays `startDateTime` instead of the actual next occurrence:
```java
Calendar nextOccurrence = reminder.getStartDateTime();
String nextOccurrenceStr = DateTimeDisplayUtil.getFriendlyDateTimeSingleLine(context, nextOccurrence);
```

**Impact:** For recurring reminders, the displayed time shows the original start time, not when the reminder will actually fire next. This is confusing for users.

**Fix:** Use `reminder.getNextOccurrenceAfter(Calendar.getInstance())` to get the actual next trigger time.

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

**Impact:** In edge cases (e.g., service restart by system), `intent` can be null, causing a crash.

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

**Impact:** If `startDateTime` is null, calling `equals()` throws NPE. Other fields use `Objects.equals()` for null safety.

**Fix:** Change to `Objects.equals(startDateTime, that.startDateTime)`.

---

### 8. Vibration Pattern Doesn't Repeat
**File:** `NotificationStarterService.java:187`

**Description:** Vibration only plays once:
```java
vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
```

**Impact:** The `-1` means no repeat. For an alarm notification, vibration should continue until dismissed to get user attention.

**Fix:** Use a positive repeat index: `VibrationEffect.createWaveform(pattern, 0)` to repeat from the beginning.

---

### 9. Typo in RecurrenceType Enum
**File:** `RecurrenceType.java:8`

**Description:**
```java
MONTH("Months(s)"),  // Should be "Month(s)"
```

**Impact:** The displayed text has an extra 's', showing "Months(s)" instead of "Month(s)". Minor UI issue.

**Fix:** Change to `MONTH("Month(s)")`.

---

### 10. Inaccurate Month/Year Duration Calculations
**File:** `RecurrenceType.java:47-48`

**Description:** Fixed millisecond values for variable-length periods:
```java
case MONTH -> 2_678_400_000L; // 31 days
case YEAR -> 31_622_400_000L; // 366 days
```

**Impact:**
- Month calculation uses 31 days, but months vary from 28-31 days, causing drift
- Year calculation uses 366 days (leap year), but most years have 365 days
- Over time, recurring reminders will shift from their intended schedule

**Fix:** Use `Calendar.add()` to properly handle date arithmetic instead of fixed milliseconds.

---

### 11. MaterialDatePicker Timezone Issue
**File:** `UpsertReminderActivity.java:257-259`

**Description:**
```java
datePicker.addOnPositiveButtonClickListener(selection -> {
  Calendar selected = Calendar.getInstance();
  selected.setTimeInMillis(selection);  // selection is in UTC
```

**Impact:** MaterialDatePicker returns dates as UTC midnight timestamps. Setting this directly can cause off-by-one-day errors depending on the device's timezone.

**Fix:** Use `MaterialDatePicker.todayInUtcMilliseconds()` approach or extract date components separately.

---

## Low Priority Issues

### 12. Unnecessary endDateTime Initialization
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

**Impact:** Non-recurring reminders will have an `endDateTime` value set even though it's not used, potentially causing confusion in the data layer.

**Fix:** Only initialize `endDateTime` when recurrence is explicitly enabled, or set to `null` by default.

---

### 13. Missing RECEIVE_BOOT_COMPLETED Permission
**File:** `AndroidManifest.xml`

**Description:** The BootReceiver is registered but the `RECEIVE_BOOT_COMPLETED` permission may not be declared.

**Impact:** The app may not receive boot completed broadcasts on all devices, causing reminders not to resume after reboot.

**Fix:** Ensure `<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />` is in the manifest.

---

### 14. WorkManager Minimum Interval Warning
**File:** `ReminderApplication.java:38`

**Description:**
```java
new PeriodicWorkRequest.Builder(ReminderTriggerWorker.class, 1, TimeUnit.MINUTES)
```

**Impact:** WorkManager enforces a minimum interval of 15 minutes for periodic work. Using 1 minute will be silently increased to 15 minutes, which may not align with expected behavior.

**Fix:** Document this limitation or use AlarmManager for more frequent scheduling if needed.

---

## Summary

| Severity | Count | Description |
|----------|-------|-------------|
| Critical | 5 | App crashes or core functionality broken |
| Medium | 6 | Incorrect behavior or potential crashes |
| Low | 3 | Minor issues or code quality concerns |

**Total bugs found: 14**
