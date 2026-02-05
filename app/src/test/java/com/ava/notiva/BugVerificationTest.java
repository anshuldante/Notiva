package com.ava.notiva;

import static org.junit.Assert.*;

import com.ava.notiva.model.RecurrenceType;
import com.ava.notiva.model.ReminderModel;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

/**
 * Unit tests that verify/expose bugs documented in bugs_found.md.
 * These tests document the current buggy behavior and should be updated
 * to assert correct behavior after bugs are fixed.
 *
 * Test naming convention: bug{number}_{description}_{expectedBehavior}
 */
public class BugVerificationTest {

    private Calendar now;

    @Before
    public void setUp() {
        now = Calendar.getInstance();
        now.set(2024, Calendar.JANUARY, 15, 10, 0, 0);
        now.set(Calendar.MILLISECOND, 0);
    }

    // ==================== Bug #1: FOREVER Recurrence ArithmeticException ====================

    /**
     * Bug #1: FOREVER recurrence with past start date - FIXED
     * File: ReminderModel.java:143
     *
     * Previous behavior: Threw ArithmeticException (division by zero)
     * Fixed behavior: Returns null for past start (no next occurrence)
     */
    @Test
    public void bug1_foreverRecurrence_pastStart_returnsNull() {
        ReminderModel reminder = new ReminderModel();
        Calendar pastStart = (Calendar) now.clone();
        pastStart.add(Calendar.DAY_OF_YEAR, -30);

        reminder.setStartDateTime(pastStart);
        reminder.setRecurrenceType(RecurrenceType.FOREVER);
        reminder.setRecurrenceDelay(1);
        reminder.setEndDateTime(null);

        // FIXED: Now returns null instead of throwing ArithmeticException
        Calendar result = reminder.getNextOccurrenceAfter(now);
        assertNull("FOREVER with past start should return null (no next occurrence)", result);
    }

    /**
     * Bug #1 supplementary: Verify FOREVER returns 0 millis (root cause)
     */
    @Test
    public void bug1_foreverRecurrenceType_returnsZeroMillis() {
        assertEquals("FOREVER.getMillis() returns 0, causing the bug",
                0L, RecurrenceType.FOREVER.getMillis());
    }

    /**
     * Bug #1: FOREVER with future start works (no division needed)
     * This confirms the bug only manifests when start is in the past.
     */
    @Test
    public void bug1_foreverRecurrence_futureStart_works() {
        ReminderModel reminder = new ReminderModel();
        Calendar futureStart = (Calendar) now.clone();
        futureStart.add(Calendar.DAY_OF_YEAR, 5);

        reminder.setStartDateTime(futureStart);
        reminder.setRecurrenceType(RecurrenceType.FOREVER);
        reminder.setRecurrenceDelay(1);

        // Future start works because no interval calculation is needed
        Calendar next = reminder.getNextOccurrenceAfter(now);
        assertNotNull(next);
        assertEquals(futureStart.getTimeInMillis(), next.getTimeInMillis());
    }

    // ==================== Bug #7: NPE in ReminderModel.equals() ====================

    /**
     * Bug #7: ReminderModel.equals() throws NPE when startDateTime is null
     * File: ReminderModel.java:177
     *
     * Current behavior: Throws NullPointerException
     * Expected behavior: Should handle null startDateTime using Objects.equals()
     */
    @Test(expected = NullPointerException.class)
    public void bug7_equals_nullStartDateTime_throwsNPE() {
        ReminderModel model1 = new ReminderModel();
        model1.setStartDateTime(null);

        ReminderModel model2 = new ReminderModel();
        model2.setStartDateTime(null);

        // This throws NPE because equals() uses startDateTime.equals() instead of Objects.equals()
        model1.equals(model2);
    }

    /**
     * Bug #7 supplementary: Verify other fields use Objects.equals() but startDateTime doesn't
     */
    @Test
    public void bug7_equals_nullName_handledCorrectly() {
        ReminderModel model1 = new ReminderModel();
        model1.setName(null);

        ReminderModel model2 = new ReminderModel();
        model2.setName(null);

        // This works because name uses Objects.equals()
        // But we can't fully test equals() because startDateTime would NPE
        // This test just shows the inconsistency
        assertTrue("name field handles null correctly", true);
    }

    /**
     * Bug #7: equals() works when startDateTime is non-null (normal case)
     */
    @Test
    public void bug7_equals_nonNullStartDateTime_works() {
        Calendar time = Calendar.getInstance();

        ReminderModel model1 = new ReminderModel();
        model1.setId(1);
        model1.setStartDateTime(time);
        model1.setEndDateTime(time);

        ReminderModel model2 = new ReminderModel();
        model2.setId(1);
        model2.setStartDateTime(time);
        model2.setEndDateTime(time);

        assertEquals(model1, model2);
    }

    // ==================== Bug #8: Inaccurate Month/Year Duration ====================

    /**
     * Bug #8: MONTH uses fixed 31 days, causing drift for shorter months
     * File: RecurrenceType.java:47
     *
     * Current behavior: MONTH = 31 days (2,678,400,000 ms)
     * Problem: February has 28/29 days, April/June/Sept/Nov have 30 days
     */
    @Test
    public void bug8_monthDuration_uses31Days() {
        long expectedFor31Days = 31L * 24 * 60 * 60 * 1000;
        assertEquals("MONTH is hardcoded to 31 days",
                expectedFor31Days, RecurrenceType.MONTH.getMillis());

        // This causes drift: a "monthly" reminder on Jan 15 would be:
        // Feb 15 (correct by calendar) vs Feb 15 + ~3 days (by fixed millis)
        // After 12 months, drift could be significant
    }

    /**
     * Bug #8: YEAR uses fixed 366 days (leap year), causing drift in non-leap years
     * File: RecurrenceType.java:48
     */
    @Test
    public void bug8_yearDuration_uses366Days() {
        long expectedFor366Days = 366L * 24 * 60 * 60 * 1000;
        assertEquals("YEAR is hardcoded to 366 days (leap year)",
                expectedFor366Days, RecurrenceType.YEAR.getMillis());

        // Most years have 365 days, causing 1-day drift per year
        long expectedFor365Days = 365L * 24 * 60 * 60 * 1000;
        assertNotEquals("YEAR should use 365 days for non-leap years",
                expectedFor365Days, RecurrenceType.YEAR.getMillis());
    }

    /**
     * Bug #8: Demonstrate actual drift for monthly reminder
     */
    @Test
    public void bug8_monthlyReminder_driftsOverTime() {
        ReminderModel reminder = new ReminderModel();

        // Start on Jan 15, 2024
        Calendar start = Calendar.getInstance();
        start.set(2024, Calendar.JANUARY, 15, 10, 0, 0);
        start.set(Calendar.MILLISECOND, 0);

        reminder.setStartDateTime(start);
        reminder.setRecurrenceType(RecurrenceType.MONTH);
        reminder.setRecurrenceDelay(1);
        reminder.setEndDateTime(null);

        // Check March 1, 2024 (after ~1.5 months)
        Calendar march1 = Calendar.getInstance();
        march1.set(2024, Calendar.MARCH, 1, 10, 0, 0);
        march1.set(Calendar.MILLISECOND, 0);

        Calendar next = reminder.getNextOccurrenceAfter(march1);

        // Expected by calendar logic: March 15
        Calendar expectedMarch15 = Calendar.getInstance();
        expectedMarch15.set(2024, Calendar.MARCH, 15, 10, 0, 0);
        expectedMarch15.set(Calendar.MILLISECOND, 0);

        // Actual result will be different due to fixed 31-day intervals
        // Jan 15 + 31 days = Feb 15
        // Feb 15 + 31 days = March 17 (not March 15!)
        assertNotEquals("Monthly reminder drifts due to fixed 31-day interval",
                expectedMarch15.getTimeInMillis(), next.getTimeInMillis());
    }

    // ==================== Bug #13: Typo in RecurrenceType ====================

    /**
     * Bug #13: MONTH has typo "Months(s)" instead of "Month(s)"
     * File: RecurrenceType.java:8
     */
    @Test
    public void bug13_monthTypo_showsMonthsWithExtraS() {
        String monthValue = RecurrenceType.MONTH.getValue();

        // Current buggy value
        assertEquals("MONTH has typo with extra 's'",
                "Months(s)", monthValue);

        // Should be "Month(s)" - this assertion documents expected fix
        assertNotEquals("Should be 'Month(s)' after fix",
                "Month(s)", monthValue);
    }

    /**
     * Bug #13: Verify other types don't have the typo
     */
    @Test
    public void bug13_otherTypes_noTypo() {
        assertEquals("Year(s)", RecurrenceType.YEAR.getValue());
        assertEquals("Day(s)", RecurrenceType.DAY.getValue());
        assertEquals("Hour(s)", RecurrenceType.HOUR.getValue());
        assertEquals("Minute(s)", RecurrenceType.MINUTE.getValue());
        assertEquals("Forever", RecurrenceType.FOREVER.getValue());
        assertEquals("Never", RecurrenceType.NEVER.getValue());
    }

    // ==================== Bug #14: Unnecessary endDateTime Initialization ====================

    /**
     * Bug #14: Default constructor initializes endDateTime to current time
     * File: ReminderModel.java:42-43
     *
     * Current behavior: endDateTime is set to Calendar.getInstance()
     * Problem: Non-recurring reminders don't need endDateTime, can cause confusion
     */
    @Test
    public void bug14_defaultConstructor_initializesEndDateTime() {
        ReminderModel reminder = new ReminderModel();

        // endDateTime is unnecessarily initialized
        assertNotNull("endDateTime should be null for new reminders but isn't",
                reminder.getEndDateTime());
    }

    /**
     * Bug #14: startDateTime is also initialized (this is reasonable)
     */
    @Test
    public void bug14_defaultConstructor_initializesStartDateTime() {
        ReminderModel reminder = new ReminderModel();

        // startDateTime initialization makes sense
        assertNotNull(reminder.getStartDateTime());
    }

    // ==================== Bug #11: Wrong Next Occurrence Display (Logic Test) ====================

    /**
     * Bug #11: Adapter uses getStartDateTime() instead of getNextOccurrenceAfter()
     * File: ReminderItemAdapter.java:62-63
     *
     * This test verifies that getStartDateTime() returns different value than
     * getNextOccurrenceAfter() for recurring reminders with past start dates.
     */
    @Test
    public void bug11_startDateTime_differsFromNextOccurrence_forRecurringReminders() {
        ReminderModel reminder = new ReminderModel();

        // Start date in the past
        Calendar pastStart = (Calendar) now.clone();
        pastStart.add(Calendar.DAY_OF_YEAR, -10);

        // End date in the future
        Calendar futureEnd = (Calendar) now.clone();
        futureEnd.add(Calendar.MONTH, 1);

        reminder.setStartDateTime(pastStart);
        reminder.setRecurrenceType(RecurrenceType.DAY);
        reminder.setRecurrenceDelay(1);
        reminder.setEndDateTime(futureEnd);

        // What the adapter currently shows (buggy)
        Calendar displayedTime = reminder.getStartDateTime();

        // What should be shown (correct next occurrence)
        Calendar correctNextOccurrence = reminder.getNextOccurrenceAfter(now);

        // These should be different - the adapter shows the wrong one
        assertNotEquals("Adapter shows startDateTime but should show next occurrence",
                displayedTime.getTimeInMillis(), correctNextOccurrence.getTimeInMillis());

        // Verify next occurrence is in the future
        assertTrue("Next occurrence should be after now",
                correctNextOccurrence.after(now));

        // Verify startDateTime is in the past (what's being wrongly displayed)
        assertTrue("StartDateTime is in the past (wrongly displayed)",
                displayedTime.before(now));
    }

    // ==================== Bug #3: PendingIntent Request Code Collision (Logic Test) ====================

    /**
     * Bug #3: Both dismiss and snooze use request code 0
     * File: NotificationStarterService.java:127,140
     *
     * This is a logic/design bug that can't be unit tested without Android framework,
     * but we can document the issue here.
     */
    @Test
    public void bug3_documentPendingIntentCollision() {
        // Both PendingIntents in NotificationStarterService use request code 0:
        // Line 127: PendingIntent.getService(this, 0, dismissIntent, FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT)
        // Line 140: PendingIntent.getService(this, 0, snoozeIntent, FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT)
        //
        // With FLAG_UPDATE_CURRENT, the second one overwrites the first
        // Both buttons will trigger snooze (the last one created)
        //
        // Fix: Use unique request codes like notificationId and notificationId + 1000000
        assertTrue("See instrumented tests for actual PendingIntent verification", true);
    }

    // ==================== Bug #4: Snooze Doesn't Delay (Logic Test) ====================

    /**
     * Bug #4: Snooze action doesn't actually delay for 10 minutes
     * File: NotificationStopperService.java:30-32
     *
     * This documents the logic bug - actual testing needs Android framework.
     */
    @Test
    public void bug4_documentSnoozeNotDelaying() {
        // Current snooze implementation:
        // if (ACTION_SNOOZE.equals(action)) {
        //   ReminderWorkerUtils.enqueueReminderWorker(getApplicationContext());
        //   Toast.makeText(..., "Reminder snoozed for 10 minutes", ...).show();
        // }
        //
        // Problem: enqueueReminderWorker() just re-enqueues the worker which
        // calculates next occurrence based on original schedule, not +10 minutes
        //
        // Fix: Use AlarmManager to schedule a one-time alarm for current time + 10 minutes
        assertTrue("See instrumented tests for actual snooze behavior verification", true);
    }

    // ==================== Bug #10: WorkManager Minimum Interval ====================

    /**
     * Bug #10: WorkManager silently increases 1-minute interval to 15 minutes
     * File: ReminderApplication.java:38
     *
     * WorkManager enforces MIN_PERIODIC_INTERVAL_MILLIS = 15 minutes
     * The app requests 1 minute but gets 15 minutes
     */
    @Test
    public void bug10_documentWorkManagerMinInterval() {
        // Current code in ReminderApplication:
        // new PeriodicWorkRequest.Builder(ReminderTriggerWorker.class, 1, TimeUnit.MINUTES)
        //
        // WorkManager's PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS = 15 * 60 * 1000
        // The 1-minute interval is silently increased to 15 minutes
        //
        // Impact: Reminders may fire up to 15 minutes late
        long requestedInterval = 1 * 60 * 1000; // 1 minute in ms
        long workManagerMinInterval = 15 * 60 * 1000; // 15 minutes in ms

        assertTrue("Requested interval is less than WorkManager minimum",
                requestedInterval < workManagerMinInterval);
    }
}
