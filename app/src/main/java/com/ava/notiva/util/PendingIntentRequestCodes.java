package com.ava.notiva.util;

/**
 * Centralized PendingIntent request code allocation.
 *
 * <p>Allocation Ranges:
 * <pre>
 * -----------------------------------------------------------------
 * Range              | Codes                 | Purpose
 * -----------------------------------------------------------------
 * RANGE_ALARM        | 0 - 999,999           | Alarm scheduling (ReminderTriggerWorker)
 * RANGE_DISMISS      | 0 - 999,999           | Notification dismiss action
 * RANGE_SNOOZE       | 1,000,000 - 1,999,999 | Notification snooze action
 * RANGE_SNOOZE_ALARM | 2,000,000 - 2,999,999 | Snooze re-fire alarm
 * RANGE_RESERVED     | 3,000,000 - 3,999,999 | Reserved for future use
 * -----------------------------------------------------------------
 * </pre>
 *
 * <p>Note: RANGE_ALARM and RANGE_DISMISS share the same numeric range but
 * target different components (ForegroundService vs BroadcastService) and
 * use different Intent targets, so Android treats them as distinct PendingIntents.
 */
public final class PendingIntentRequestCodes {

    private static final int OFFSET_SNOOZE = 1_000_000;
    private static final int OFFSET_SNOOZE_ALARM = 2_000_000;

    private PendingIntentRequestCodes() {
        // Utility class, no instantiation
    }

    /** Request code for scheduling an alarm for a reminder. */
    public static int forAlarm(int reminderId) {
        return reminderId;
    }

    /** Request code for the dismiss action PendingIntent on a notification. */
    public static int forDismiss(int notificationId) {
        return notificationId;
    }

    /** Request code for the snooze action PendingIntent on a notification. */
    public static int forSnooze(int notificationId) {
        return notificationId + OFFSET_SNOOZE;
    }

    /** Request code for the snooze re-fire alarm PendingIntent. */
    public static int forSnoozeAlarm(int reminderId) {
        return reminderId + OFFSET_SNOOZE_ALARM;
    }
}
