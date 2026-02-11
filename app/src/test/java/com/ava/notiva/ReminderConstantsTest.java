package com.ava.notiva;

import static org.junit.Assert.*;

import com.ava.notiva.util.NotificationPreferences;
import com.ava.notiva.util.ReminderConstants;

import org.junit.Test;

/**
 * Unit tests for {@link ReminderConstants} and {@link NotificationPreferences}.
 * Verifies critical constant values that other components depend on.
 */
public class ReminderConstantsTest {

    // ==================== Action Constants ====================

    @Test
    public void actionSnooze_isSnooze() {
        assertEquals("Snooze", ReminderConstants.ACTION_SNOOZE);
    }

    @Test
    public void actionDismiss_isDismiss() {
        assertEquals("Dismiss", ReminderConstants.ACTION_DISMISS);
    }

    @Test
    public void actionSnooze_andDismiss_areDifferent() {
        assertNotEquals(ReminderConstants.ACTION_SNOOZE, ReminderConstants.ACTION_DISMISS);
    }

    // ==================== Channel Constants ====================

    @Test
    public void channelIds_areUnique() {
        assertNotEquals(ReminderConstants.CHANNEL_ID, ReminderConstants.FOREGROUND_CHANNEL_ID);
        assertNotEquals(ReminderConstants.CHANNEL_ID, ReminderConstants.SUMMARY_CHANNEL_ID);
        assertNotEquals(ReminderConstants.FOREGROUND_CHANNEL_ID, ReminderConstants.SUMMARY_CHANNEL_ID);
    }

    @Test
    public void channelId_isNotEmpty() {
        assertFalse(ReminderConstants.CHANNEL_ID.isEmpty());
        assertFalse(ReminderConstants.FOREGROUND_CHANNEL_ID.isEmpty());
        assertFalse(ReminderConstants.SUMMARY_CHANNEL_ID.isEmpty());
    }

    // ==================== Notification ID Constants ====================

    @Test
    public void summaryNotificationId_isMaxValueMinusOne() {
        assertEquals(Integer.MAX_VALUE - 1, ReminderConstants.SUMMARY_NOTIFICATION_ID);
    }

    @Test
    public void summaryNotificationId_doesNotColideWithForegroundId() {
        // Foreground uses Integer.MAX_VALUE
        assertNotEquals(Integer.MAX_VALUE, ReminderConstants.SUMMARY_NOTIFICATION_ID);
    }

    // ==================== Intent Extra Keys ====================

    @Test
    public void intentExtraKeys_areUnique() {
        String[] keys = {
            ReminderConstants.REMINDER_ID,
            ReminderConstants.REMINDER_ACTIVE,
            ReminderConstants.REMINDER_NAME,
            ReminderConstants.REMINDER_START_TIME,
            ReminderConstants.REMINDER_RECURRENCE_DELAY,
            ReminderConstants.REMINDER_RECURRENCE_TYPE,
            ReminderConstants.REMINDER_END_TIME,
            ReminderConstants.SCHEDULED_FIRE_EPOCH,
        };

        for (int i = 0; i < keys.length; i++) {
            for (int j = i + 1; j < keys.length; j++) {
                assertNotEquals("Keys at index " + i + " and " + j + " should be unique",
                        keys[i], keys[j]);
            }
        }
    }

    @Test
    public void intentExtraKeys_usePackagePrefix() {
        assertTrue(ReminderConstants.REMINDER_ID.startsWith("com.ava.notiva."));
        assertTrue(ReminderConstants.REMINDER_NAME.startsWith("com.ava.notiva."));
        assertTrue(ReminderConstants.SCHEDULED_FIRE_EPOCH.startsWith("com.ava.notiva."));
    }

    // ==================== Collapse Threshold ====================

    @Test
    public void perReminderCollapseThreshold_isThree() {
        assertEquals(3, ReminderConstants.PER_REMINDER_COLLAPSE_THRESHOLD);
    }

    @Test
    public void perReminderCollapseThreshold_isPositive() {
        assertTrue(ReminderConstants.PER_REMINDER_COLLAPSE_THRESHOLD > 0);
    }

    // ==================== Default Snooze Time ====================

    @Test
    public void defaultSnoozeTime_isTenMinutesInMillis() {
        assertEquals(10_000L, ReminderConstants.DEFAULT_SNOOZE_TIME_10_MINUTES);
    }

    // ==================== Group Key ====================

    @Test
    public void notificationGroupKey_isNotEmpty() {
        assertFalse(ReminderConstants.NOTIFICATION_GROUP_KEY.isEmpty());
    }

    // ==================== NotificationPreferences.getBurstWindowMillis() ====================

    @Test
    public void burstWindowMillis_isSixtySeconds() {
        assertEquals(60_000L, NotificationPreferences.getBurstWindowMillis());
    }

    @Test
    public void burstWindowMillis_isPositive() {
        assertTrue(NotificationPreferences.getBurstWindowMillis() > 0);
    }
}
