package com.ava.notiva;

import static org.junit.Assert.*;

import com.ava.notiva.util.PendingIntentRequestCodes;

import org.junit.Test;

/**
 * Unit tests for {@link PendingIntentRequestCodes}.
 */
public class PendingIntentRequestCodesTest {

    @Test
    public void forAlarm_returnsReminderId() {
        assertEquals(42, PendingIntentRequestCodes.forAlarm(42));
    }

    @Test
    public void forDismiss_returnsNotificationId() {
        assertEquals(42, PendingIntentRequestCodes.forDismiss(42));
    }

    @Test
    public void forSnooze_addsSnoozeOffset() {
        assertEquals(1_000_042, PendingIntentRequestCodes.forSnooze(42));
    }

    @Test
    public void forSnoozeAlarm_addsSnoozeAlarmOffset() {
        assertEquals(2_000_042, PendingIntentRequestCodes.forSnoozeAlarm(42));
    }

    @Test
    public void forSnooze_andForSnoozeAlarm_produceDifferentCodes() {
        int id = 500;
        int snoozeCode = PendingIntentRequestCodes.forSnooze(id);
        int snoozeAlarmCode = PendingIntentRequestCodes.forSnoozeAlarm(id);

        assertNotEquals("Snooze and snooze-alarm codes must differ for the same input",
                snoozeCode, snoozeAlarmCode);
        assertTrue("Snooze code should be in 1M range", snoozeCode >= 1_000_000 && snoozeCode < 2_000_000);
        assertTrue("Snooze-alarm code should be in 2M range", snoozeAlarmCode >= 2_000_000 && snoozeAlarmCode < 3_000_000);
    }

    @Test
    public void forAlarm_withZero_returnsZero() {
        assertEquals(0, PendingIntentRequestCodes.forAlarm(0));
    }

    @Test
    public void forSnooze_withZero_returnsOffset() {
        assertEquals(1_000_000, PendingIntentRequestCodes.forSnooze(0));
    }

    @Test
    public void forSnoozeAlarm_withZero_returnsOffset() {
        assertEquals(2_000_000, PendingIntentRequestCodes.forSnoozeAlarm(0));
    }

    @Test
    public void forSnooze_withMaxValidId_doesNotOverflowIntoSnoozeAlarmRange() {
        int maxValidId = 999_999;
        int snoozeCode = PendingIntentRequestCodes.forSnooze(maxValidId);

        assertTrue("Snooze code for max valid ID should stay within snooze range",
                snoozeCode < 2_000_000);
        assertEquals(1_999_999, snoozeCode);
    }
}
