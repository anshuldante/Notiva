package com.ava.notiva;

import static org.junit.Assert.*;

import com.ava.notiva.model.RecurrenceType;
import com.ava.notiva.model.ReminderModel;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

/**
 * Unit tests for {@link ReminderModel}, focusing on the complex
 * recurrence calculation logic in {@link ReminderModel#getNextOccurrenceAfter(Calendar)}.
 */
public class ReminderModelTest {

    private ReminderModel reminder;
    private Calendar now;

    @Before
    public void setUp() {
        reminder = new ReminderModel();
        now = Calendar.getInstance();
        // Set to a fixed time for predictable tests: 2024-01-15 10:00:00
        now.set(2024, Calendar.JANUARY, 15, 10, 0, 0);
        now.set(Calendar.MILLISECOND, 0);
    }

    // ==================== Constructor Tests ====================

    @Test
    public void defaultConstructor_setsDefaultValues() {
        ReminderModel model = new ReminderModel();

        assertTrue("Default active should be true", model.isActive());
        assertEquals("Default recurrence type should be DAY", RecurrenceType.DAY, model.getRecurrenceType());
        assertNotNull("Start date should not be null", model.getStartDateTime());
        assertNull("End date should be null by default", model.getEndDateTime());
    }

    @Test
    public void nameConstructor_setsNameAndDefaults() {
        ReminderModel model = new ReminderModel("Test Reminder");

        assertEquals("Test Reminder", model.getName());
        assertTrue(model.isActive());
        assertEquals(RecurrenceType.DAY, model.getRecurrenceType());
    }

    @Test
    public void fullConstructor_setsAllFields() {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + 86400000L; // +1 day

        ReminderModel model = new ReminderModel(
                1, "Full Test", true, startTime, 2, "DAY", endTime);

        assertEquals(1, model.getId());
        assertEquals("Full Test", model.getName());
        assertTrue(model.isActive());
        assertEquals(2, model.getRecurrenceDelay());
        assertEquals(RecurrenceType.DAY, model.getRecurrenceType());
        assertEquals(startTime, model.getStartDateTime().getTimeInMillis());
        assertEquals(endTime, model.getEndDateTime().getTimeInMillis());
    }

    // ==================== getNextOccurrenceAfter - NEVER recurrence ====================

    @Test
    public void getNextOccurrence_neverRecurrence_startInFuture_returnsStartTime() {
        Calendar futureStart = (Calendar) now.clone();
        futureStart.add(Calendar.HOUR, 2); // 2 hours in future

        reminder.setStartDateTime(futureStart);
        reminder.setRecurrenceType(RecurrenceType.NEVER);
        reminder.setRecurrenceDelay(1);

        Calendar next = reminder.getNextOccurrenceAfter(now);

        assertNotNull(next);
        assertEquals(futureStart.getTimeInMillis(), next.getTimeInMillis());
    }

    @Test
    public void getNextOccurrence_neverRecurrence_startInPast_returnsNull() {
        Calendar pastStart = (Calendar) now.clone();
        pastStart.add(Calendar.HOUR, -2); // 2 hours in past

        reminder.setStartDateTime(pastStart);
        reminder.setRecurrenceType(RecurrenceType.NEVER);
        reminder.setRecurrenceDelay(1);

        Calendar next = reminder.getNextOccurrenceAfter(now);

        assertNull("Should return null for past one-time reminder", next);
    }

    @Test
    public void getNextOccurrence_zeroDelay_startInPast_returnsNull() {
        Calendar pastStart = (Calendar) now.clone();
        pastStart.add(Calendar.HOUR, -2);

        reminder.setStartDateTime(pastStart);
        reminder.setRecurrenceType(RecurrenceType.DAY);
        reminder.setRecurrenceDelay(0); // Zero delay treated as non-recurring

        Calendar next = reminder.getNextOccurrenceAfter(now);

        assertNull("Zero delay should behave like NEVER for past start", next);
    }

    // ==================== getNextOccurrenceAfter - Start in future ====================

    @Test
    public void getNextOccurrence_startInFuture_returnsStartTime() {
        Calendar futureStart = (Calendar) now.clone();
        futureStart.add(Calendar.DAY_OF_YEAR, 5); // 5 days in future

        reminder.setStartDateTime(futureStart);
        reminder.setRecurrenceType(RecurrenceType.DAY);
        reminder.setRecurrenceDelay(1);
        reminder.setEndDateTime(null); // No end

        Calendar next = reminder.getNextOccurrenceAfter(now);

        assertNotNull(next);
        assertEquals(futureStart.getTimeInMillis(), next.getTimeInMillis());
    }

    // ==================== getNextOccurrenceAfter - Minute recurrence ====================

    @Test
    public void getNextOccurrence_minuteRecurrence_calculatesCorrectly() {
        Calendar pastStart = (Calendar) now.clone();
        pastStart.add(Calendar.MINUTE, -10); // Started 10 minutes ago

        Calendar futureEnd = (Calendar) now.clone();
        futureEnd.add(Calendar.HOUR, 1); // Ends in 1 hour

        reminder.setStartDateTime(pastStart);
        reminder.setRecurrenceType(RecurrenceType.MINUTE);
        reminder.setRecurrenceDelay(3); // Every 3 minutes
        reminder.setEndDateTime(futureEnd);

        Calendar next = reminder.getNextOccurrenceAfter(now);

        assertNotNull(next);
        assertTrue("Next occurrence should be after now", next.after(now));

        // Verify it's a multiple of 3 minutes from start
        long diffMinutes = (next.getTimeInMillis() - pastStart.getTimeInMillis()) / 60_000L;
        assertEquals("Should be multiple of 3 minutes", 0, diffMinutes % 3);
    }

    // ==================== getNextOccurrenceAfter - Hour recurrence ====================

    @Test
    public void getNextOccurrence_hourRecurrence_calculatesCorrectly() {
        Calendar pastStart = (Calendar) now.clone();
        pastStart.add(Calendar.HOUR, -5); // Started 5 hours ago

        Calendar futureEnd = (Calendar) now.clone();
        futureEnd.add(Calendar.DAY_OF_YEAR, 1); // Ends tomorrow

        reminder.setStartDateTime(pastStart);
        reminder.setRecurrenceType(RecurrenceType.HOUR);
        reminder.setRecurrenceDelay(2); // Every 2 hours
        reminder.setEndDateTime(futureEnd);

        Calendar next = reminder.getNextOccurrenceAfter(now);

        assertNotNull(next);
        assertTrue("Next occurrence should be after now", next.after(now));

        // Verify it's a multiple of 2 hours from start
        long diffHours = (next.getTimeInMillis() - pastStart.getTimeInMillis()) / 3_600_000L;
        assertEquals("Should be multiple of 2 hours", 0, diffHours % 2);
    }

    // ==================== getNextOccurrenceAfter - Day recurrence ====================

    @Test
    public void getNextOccurrence_dayRecurrence_calculatesCorrectly() {
        Calendar pastStart = (Calendar) now.clone();
        pastStart.add(Calendar.DAY_OF_YEAR, -7); // Started 7 days ago

        Calendar futureEnd = (Calendar) now.clone();
        futureEnd.add(Calendar.MONTH, 1); // Ends in 1 month

        reminder.setStartDateTime(pastStart);
        reminder.setRecurrenceType(RecurrenceType.DAY);
        reminder.setRecurrenceDelay(3); // Every 3 days
        reminder.setEndDateTime(futureEnd);

        Calendar next = reminder.getNextOccurrenceAfter(now);

        assertNotNull(next);
        assertTrue("Next occurrence should be after now", next.after(now));

        // Verify it's a multiple of 3 days from start
        long diffDays = (next.getTimeInMillis() - pastStart.getTimeInMillis()) / 86_400_000L;
        assertEquals("Should be multiple of 3 days", 0, diffDays % 3);
    }

    // ==================== getNextOccurrenceAfter - Month recurrence ====================

    @Test
    public void getNextOccurrence_monthRecurrence_calculatesCorrectly() {
        Calendar pastStart = (Calendar) now.clone();
        pastStart.add(Calendar.MONTH, -2); // Started 2 months ago

        Calendar futureEnd = (Calendar) now.clone();
        futureEnd.add(Calendar.YEAR, 1); // Ends in 1 year

        reminder.setStartDateTime(pastStart);
        reminder.setRecurrenceType(RecurrenceType.MONTH);
        reminder.setRecurrenceDelay(1); // Every month
        reminder.setEndDateTime(futureEnd);

        Calendar next = reminder.getNextOccurrenceAfter(now);

        assertNotNull(next);
        assertTrue("Next occurrence should be after now", next.after(now));
    }

    // ==================== getNextOccurrenceAfter - Year recurrence ====================

    @Test
    public void getNextOccurrence_yearRecurrence_calculatesCorrectly() {
        Calendar pastStart = (Calendar) now.clone();
        pastStart.add(Calendar.YEAR, -2); // Started 2 years ago

        Calendar futureEnd = (Calendar) now.clone();
        futureEnd.add(Calendar.YEAR, 5); // Ends in 5 years

        reminder.setStartDateTime(pastStart);
        reminder.setRecurrenceType(RecurrenceType.YEAR);
        reminder.setRecurrenceDelay(1); // Every year
        reminder.setEndDateTime(futureEnd);

        Calendar next = reminder.getNextOccurrenceAfter(now);

        assertNotNull(next);
        assertTrue("Next occurrence should be after now", next.after(now));
    }

    // ==================== getNextOccurrenceAfter - FOREVER recurrence ====================

    @Test
    public void getNextOccurrence_foreverRecurrence_withPastStart_returnsNull() {
        // FIXED: FOREVER with past start now returns null instead of throwing ArithmeticException.
        // FOREVER means "no end date" and is treated as a one-time reminder.
        // If start is in the past, there's no next occurrence.

        Calendar pastStart = (Calendar) now.clone();
        pastStart.add(Calendar.DAY_OF_YEAR, -30); // Started 30 days ago

        Calendar pastEnd = (Calendar) now.clone();
        pastEnd.add(Calendar.DAY_OF_YEAR, -10); // End date already passed

        reminder.setStartDateTime(pastStart);
        reminder.setRecurrenceType(RecurrenceType.FOREVER);
        reminder.setRecurrenceDelay(1);
        reminder.setEndDateTime(pastEnd);

        // FIXED: Now returns null instead of throwing ArithmeticException
        Calendar result = reminder.getNextOccurrenceAfter(now);
        assertNull("FOREVER with past start should return null", result);
    }

    @Test
    public void getNextOccurrence_foreverRecurrence_withFutureStart_returnsStartTime() {
        // When start is in the future, FOREVER should return the start time
        // (no division occurs in this case)
        Calendar futureStart = (Calendar) now.clone();
        futureStart.add(Calendar.DAY_OF_YEAR, 5); // 5 days in future

        reminder.setStartDateTime(futureStart);
        reminder.setRecurrenceType(RecurrenceType.FOREVER);
        reminder.setRecurrenceDelay(1);
        reminder.setEndDateTime(null);

        Calendar next = reminder.getNextOccurrenceAfter(now);

        assertNotNull(next);
        assertEquals(futureStart.getTimeInMillis(), next.getTimeInMillis());
    }

    // ==================== getNextOccurrenceAfter - End date boundary ====================

    @Test
    public void getNextOccurrence_nextOccurrenceAfterEndDate_returnsNull() {
        Calendar pastStart = (Calendar) now.clone();
        pastStart.add(Calendar.DAY_OF_YEAR, -30); // Started 30 days ago

        Calendar pastEnd = (Calendar) now.clone();
        pastEnd.add(Calendar.DAY_OF_YEAR, -1); // Ended yesterday

        reminder.setStartDateTime(pastStart);
        reminder.setRecurrenceType(RecurrenceType.DAY);
        reminder.setRecurrenceDelay(1);
        reminder.setEndDateTime(pastEnd);

        Calendar next = reminder.getNextOccurrenceAfter(now);

        assertNull("Should return null when next occurrence is after end date", next);
    }

    @Test
    public void getNextOccurrence_endDateExactlyOnNextOccurrence_returnsOccurrence() {
        Calendar pastStart = (Calendar) now.clone();
        pastStart.add(Calendar.DAY_OF_YEAR, -2); // Started 2 days ago

        // Set end date to exactly 1 day after now (3 days from start)
        Calendar endDate = (Calendar) pastStart.clone();
        endDate.add(Calendar.DAY_OF_YEAR, 3);

        reminder.setStartDateTime(pastStart);
        reminder.setRecurrenceType(RecurrenceType.DAY);
        reminder.setRecurrenceDelay(1);
        reminder.setEndDateTime(endDate);

        Calendar next = reminder.getNextOccurrenceAfter(now);

        assertNotNull("Should return occurrence when it falls on or before end date", next);
    }

    // ==================== getNextOccurrenceAfter - Null start date ====================

    @Test
    public void getNextOccurrence_nullStartDate_returnsNull() {
        reminder.setStartDateTime(null);
        reminder.setRecurrenceType(RecurrenceType.DAY);
        reminder.setRecurrenceDelay(1);

        Calendar next = reminder.getNextOccurrenceAfter(now);

        assertNull("Should return null when start date is null", next);
    }

    // ==================== Equals and HashCode ====================

    @Test
    public void equals_sameValues_returnsTrue() {
        Calendar startTime = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();
        endTime.add(Calendar.DAY_OF_YEAR, 1);

        ReminderModel model1 = new ReminderModel();
        model1.setId(1);
        model1.setName("Test");
        model1.setActive(true);
        model1.setStartDateTime(startTime);
        model1.setEndDateTime(endTime);
        model1.setRecurrenceType(RecurrenceType.DAY);
        model1.setRecurrenceDelay(1);

        ReminderModel model2 = new ReminderModel();
        model2.setId(1);
        model2.setName("Test");
        model2.setActive(true);
        model2.setStartDateTime(startTime);
        model2.setEndDateTime(endTime);
        model2.setRecurrenceType(RecurrenceType.DAY);
        model2.setRecurrenceDelay(1);

        assertEquals(model1, model2);
        assertEquals(model1.hashCode(), model2.hashCode());
    }

    @Test
    public void equals_differentId_returnsFalse() {
        ReminderModel model1 = new ReminderModel("Test");
        model1.setId(1);

        ReminderModel model2 = new ReminderModel("Test");
        model2.setId(2);

        assertNotEquals(model1, model2);
    }

    @Test
    public void equals_differentName_returnsFalse() {
        ReminderModel model1 = new ReminderModel("Test1");
        ReminderModel model2 = new ReminderModel("Test2");

        assertNotEquals(model1, model2);
    }

    @Test
    public void equals_differentActive_returnsFalse() {
        ReminderModel model1 = new ReminderModel("Test");
        model1.setActive(true);

        ReminderModel model2 = new ReminderModel("Test");
        model2.setActive(false);

        assertNotEquals(model1, model2);
    }

    // ==================== toString ====================

    @Test
    public void toString_containsAllFields() {
        reminder.setId(1);
        reminder.setName("Test Reminder");
        reminder.setActive(true);
        reminder.setRecurrenceDelay(5);
        reminder.setRecurrenceType(RecurrenceType.DAY);

        String result = reminder.toString();

        assertTrue("Should contain id", result.contains("id='1'"));
        assertTrue("Should contain name", result.contains("name='Test Reminder'"));
        assertTrue("Should contain active", result.contains("active=true"));
        assertTrue("Should contain recurrenceDelay", result.contains("recurrenceDelay=5"));
        assertTrue("Should contain recurrenceType", result.contains("recurrenceType=DAY"));
    }

    // ==================== New Tracking Fields (Phase 11) ====================

    @Test
    public void equals_differentLastFiredAt_returnsFalse() {
        ReminderModel model1 = new ReminderModel("Test");
        model1.setLastFiredAt(1000L);

        ReminderModel model2 = new ReminderModel("Test");
        model2.setLastFiredAt(2000L);

        assertNotEquals(model1, model2);
    }

    @Test
    public void equals_differentLastAcknowledgedAt_returnsFalse() {
        ReminderModel model1 = new ReminderModel("Test");
        model1.setLastAcknowledgedAt(1000L);

        ReminderModel model2 = new ReminderModel("Test");
        model2.setLastAcknowledgedAt(2000L);

        assertNotEquals(model1, model2);
    }

    @Test
    public void equals_differentRingtoneUri_returnsFalse() {
        ReminderModel model1 = new ReminderModel("Test");
        model1.setRingtoneUri("content://settings/system/alarm_alert");

        ReminderModel model2 = new ReminderModel("Test");
        model2.setRingtoneUri("content://settings/system/notification_sound");

        assertNotEquals(model1, model2);
    }

    @Test
    public void equals_sameNewFields_returnsTrue() {
        Calendar startTime = Calendar.getInstance();

        ReminderModel model1 = new ReminderModel();
        model1.setId(1);
        model1.setName("Test");
        model1.setActive(true);
        model1.setStartDateTime(startTime);
        model1.setRecurrenceType(RecurrenceType.DAY);
        model1.setRecurrenceDelay(1);
        model1.setLastFiredAt(5000L);
        model1.setLastAcknowledgedAt(6000L);
        model1.setRingtoneUri("content://media/ringtone");

        ReminderModel model2 = new ReminderModel();
        model2.setId(1);
        model2.setName("Test");
        model2.setActive(true);
        model2.setStartDateTime(startTime);
        model2.setRecurrenceType(RecurrenceType.DAY);
        model2.setRecurrenceDelay(1);
        model2.setLastFiredAt(5000L);
        model2.setLastAcknowledgedAt(6000L);
        model2.setRingtoneUri("content://media/ringtone");

        assertEquals(model1, model2);
        assertEquals(model1.hashCode(), model2.hashCode());
    }

    @Test
    public void toString_containsNewFields() {
        reminder.setLastFiredAt(123456L);
        reminder.setLastAcknowledgedAt(789012L);
        reminder.setRingtoneUri("content://media/ringtone");

        String result = reminder.toString();

        assertTrue("Should contain lastFiredAt", result.contains("lastFiredAt=123456"));
        assertTrue("Should contain lastAcknowledgedAt", result.contains("lastAcknowledgedAt=789012"));
        assertTrue("Should contain ringtoneUri", result.contains("ringtoneUri='content://media/ringtone'"));
    }

    @Test
    public void defaultConstructor_newFieldsAreNull() {
        ReminderModel model = new ReminderModel();

        assertNull("lastFiredAt should default to null", model.getLastFiredAt());
        assertNull("lastAcknowledgedAt should default to null", model.getLastAcknowledgedAt());
        assertNull("ringtoneUri should default to null", model.getRingtoneUri());
    }

    // ==================== isSnoozed() ====================

    @Test
    public void isSnoozed_nullSnoozedUntil_returnsFalse() {
        reminder.setSnoozedUntil(null);
        assertFalse("Null snoozedUntil should not be snoozed", reminder.isSnoozed());
    }

    @Test
    public void isSnoozed_pastTimestamp_returnsFalse() {
        reminder.setSnoozedUntil(System.currentTimeMillis() - 60_000L); // 1 minute ago
        assertFalse("Past snoozedUntil should not be snoozed", reminder.isSnoozed());
    }

    @Test
    public void isSnoozed_futureTimestamp_returnsTrue() {
        reminder.setSnoozedUntil(System.currentTimeMillis() + 600_000L); // 10 minutes from now
        assertTrue("Future snoozedUntil should be snoozed", reminder.isSnoozed());
    }

    @Test
    public void isSnoozed_zeroTimestamp_returnsFalse() {
        reminder.setSnoozedUntil(0L);
        assertFalse("Zero snoozedUntil (epoch) should not be snoozed", reminder.isSnoozed());
    }

    @Test
    public void isSnoozed_farFutureTimestamp_returnsTrue() {
        reminder.setSnoozedUntil(Long.MAX_VALUE);
        assertTrue("Far future snoozedUntil should be snoozed", reminder.isSnoozed());
    }

    // ==================== snoozedUntil in equals ====================

    @Test
    public void equals_sameSnoozedUntil_returnsTrue() {
        ReminderModel model1 = new ReminderModel("Test");
        model1.setSnoozedUntil(5000L);

        ReminderModel model2 = new ReminderModel("Test");
        model2.setSnoozedUntil(5000L);

        assertEquals(model1, model2);
    }

    @Test
    public void equals_differentSnoozedUntil_returnsFalse() {
        ReminderModel model1 = new ReminderModel("Test");
        model1.setSnoozedUntil(5000L);

        ReminderModel model2 = new ReminderModel("Test");
        model2.setSnoozedUntil(6000L);

        assertNotEquals(model1, model2);
    }

    @Test
    public void equals_nullVsNonNullSnoozedUntil_returnsFalse() {
        ReminderModel model1 = new ReminderModel("Test");
        model1.setSnoozedUntil(null);

        ReminderModel model2 = new ReminderModel("Test");
        model2.setSnoozedUntil(5000L);

        assertNotEquals(model1, model2);
    }

    // ==================== snoozedUntil in toString ====================

    @Test
    public void toString_containsSnoozedUntil() {
        reminder.setSnoozedUntil(99999L);
        String result = reminder.toString();
        assertTrue("Should contain snoozedUntil", result.contains("snoozedUntil=99999"));
    }

    @Test
    public void toString_nullSnoozedUntil_containsNull() {
        reminder.setSnoozedUntil(null);
        String result = reminder.toString();
        assertTrue("Should contain snoozedUntil=null", result.contains("snoozedUntil=null"));
    }

    // ==================== Edge Cases ====================

    @Test
    public void getNextOccurrence_negativeDelay_treatedAsNonRecurring() {
        Calendar pastStart = (Calendar) now.clone();
        pastStart.add(Calendar.HOUR, -2);

        reminder.setStartDateTime(pastStart);
        reminder.setRecurrenceType(RecurrenceType.DAY);
        reminder.setRecurrenceDelay(-1); // Negative delay

        Calendar next = reminder.getNextOccurrenceAfter(now);

        assertNull("Negative delay should be treated as non-recurring", next);
    }

    @Test
    public void getNextOccurrence_veryLargeDelay_calculatesCorrectly() {
        Calendar pastStart = (Calendar) now.clone();
        pastStart.add(Calendar.DAY_OF_YEAR, -1);

        Calendar futureEnd = (Calendar) now.clone();
        futureEnd.add(Calendar.YEAR, 100);

        reminder.setStartDateTime(pastStart);
        reminder.setRecurrenceType(RecurrenceType.MINUTE);
        reminder.setRecurrenceDelay(1000); // Every 1000 minutes
        reminder.setEndDateTime(futureEnd);

        Calendar next = reminder.getNextOccurrenceAfter(now);

        assertNotNull(next);
        assertTrue("Next occurrence should be after now", next.after(now));
    }

    @Test
    public void getNextOccurrence_startEqualsNow_returnsNextInterval() {
        reminder.setStartDateTime((Calendar) now.clone());
        reminder.setRecurrenceType(RecurrenceType.HOUR);
        reminder.setRecurrenceDelay(1);

        Calendar futureEnd = (Calendar) now.clone();
        futureEnd.add(Calendar.DAY_OF_YEAR, 1);
        reminder.setEndDateTime(futureEnd);

        Calendar next = reminder.getNextOccurrenceAfter(now);

        assertNotNull(next);
        // When start equals now, next should be start + 1 interval
        long expectedMillis = now.getTimeInMillis() + RecurrenceType.HOUR.getMillis();
        assertEquals(expectedMillis, next.getTimeInMillis());
    }
}
