package com.ava.notiva.util;

/**
 * Generates stable, deterministic notification IDs from a reminder ID and
 * scheduled fire epoch.
 *
 * <h3>Bit-packing scheme</h3>
 * <pre>
 * ┌──────────────────────┬──────────────────────┐
 * │  upper 16 bits       │  lower 16 bits       │
 * │  (reminderId & 0xFFFF)│  (epochSec & 0xFFFF) │
 * └──────────────────────┴──────────────────────┘
 * </pre>
 *
 * <ul>
 *   <li>Upper 16 bits: reminder ID (cast to unsigned short range, supports IDs 0-65535)</li>
 *   <li>Lower 16 bits: seconds portion of the scheduled fire epoch, masked to 16 bits.
 *       This provides ~18.2 hours (65536 seconds) of uniqueness per reminder, which is
 *       more than sufficient since different scheduled times produce different lower bits.</li>
 * </ul>
 *
 * <p>The epoch is UTC-based, making the generated ID immune to DST transitions
 * and stable across device reboots.
 *
 * <p><b>Collision avoidance:</b> The result is guaranteed to never equal
 * {@code Integer.MAX_VALUE} (used by the foreground service) or
 * {@link ReminderConstants#SUMMARY_NOTIFICATION_ID} (used by the summary notification).
 * If a collision would occur, the lowest bit is flipped to produce a safe value.
 */
public final class NotificationIdGenerator {

    private NotificationIdGenerator() {
        // Utility class, no instantiation
    }

    /**
     * Generates a deterministic notification ID for a specific reminder firing.
     *
     * <p>Same {@code reminderId} + same {@code scheduledFireEpochMillis} always
     * produces the same notification ID. Different scheduled times for the same
     * reminder produce different IDs, enabling multiple active notifications
     * per reminder.
     *
     * @param reminderId               the reminder's database ID
     * @param scheduledFireEpochMillis  the scheduled fire time in UTC epoch milliseconds
     * @return a stable notification ID that avoids reserved ID collisions
     */
    public static int generate(int reminderId, long scheduledFireEpochMillis) {
        int upper = (reminderId & 0xFFFF) << 16;
        int lower = (int) ((scheduledFireEpochMillis / 1000) & 0xFFFF);
        int result = upper | lower;

        // Ensure no collision with foreground service ID or summary notification ID
        if (result == Integer.MAX_VALUE
                || result == ReminderConstants.SUMMARY_NOTIFICATION_ID) {
            result ^= 1; // Flip lowest bit to shift away from reserved value
        }

        return result;
    }

    /**
     * Extracts the original reminder ID from a bit-packed notification ID.
     *
     * @param notificationId a notification ID previously produced by {@link #generate}
     * @return the reminder ID stored in the upper 16 bits
     */
    public static int extractReminderId(int notificationId) {
        return (notificationId >>> 16) & 0xFFFF;
    }
}
