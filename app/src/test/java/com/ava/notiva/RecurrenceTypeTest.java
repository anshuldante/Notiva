package com.ava.notiva;

import static org.junit.Assert.*;

import com.ava.notiva.model.RecurrenceType;

import org.junit.Test;

/**
 * Unit tests for {@link RecurrenceType} enum.
 * Tests interval calculations and value lookups.
 */
public class RecurrenceTypeTest {

    // ==================== getMillis Tests ====================

    @Test
    public void getMillis_minute_returns60000() {
        assertEquals(60_000L, RecurrenceType.MINUTE.getMillis());
    }

    @Test
    public void getMillis_hour_returns3600000() {
        assertEquals(3_600_000L, RecurrenceType.HOUR.getMillis());
    }

    @Test
    public void getMillis_day_returns86400000() {
        assertEquals(86_400_000L, RecurrenceType.DAY.getMillis());
    }

    @Test
    public void getMillis_month_returns31Days() {
        // Month is defined as 31 days
        assertEquals(2_678_400_000L, RecurrenceType.MONTH.getMillis());
        assertEquals(31 * 24 * 60 * 60 * 1000L, RecurrenceType.MONTH.getMillis());
    }

    @Test
    public void getMillis_year_returns366Days() {
        // Year is defined as 366 days (leap year)
        assertEquals(31_622_400_000L, RecurrenceType.YEAR.getMillis());
        assertEquals(366 * 24 * 60 * 60 * 1000L, RecurrenceType.YEAR.getMillis());
    }

    @Test
    public void getMillis_forever_returnsZero() {
        assertEquals(0L, RecurrenceType.FOREVER.getMillis());
    }

    @Test
    public void getMillis_never_returnsZero() {
        assertEquals(0L, RecurrenceType.NEVER.getMillis());
    }

    // ==================== getValue Tests ====================

    @Test
    public void getValue_allTypes_returnCorrectDisplayStrings() {
        assertEquals("Year(s)", RecurrenceType.YEAR.getValue());
        assertEquals("Months(s)", RecurrenceType.MONTH.getValue());
        assertEquals("Day(s)", RecurrenceType.DAY.getValue());
        assertEquals("Hour(s)", RecurrenceType.HOUR.getValue());
        assertEquals("Minute(s)", RecurrenceType.MINUTE.getValue());
        assertEquals("Forever", RecurrenceType.FOREVER.getValue());
        assertEquals("Never", RecurrenceType.NEVER.getValue());
    }

    // ==================== getRecurrenceTypeByValue Tests ====================

    @Test
    public void getRecurrenceTypeByValue_validValues_returnsCorrectEnum() {
        assertEquals(RecurrenceType.YEAR, RecurrenceType.getRecurrenceTypeByValue("Year(s)"));
        assertEquals(RecurrenceType.MONTH, RecurrenceType.getRecurrenceTypeByValue("Months(s)"));
        assertEquals(RecurrenceType.DAY, RecurrenceType.getRecurrenceTypeByValue("Day(s)"));
        assertEquals(RecurrenceType.HOUR, RecurrenceType.getRecurrenceTypeByValue("Hour(s)"));
        assertEquals(RecurrenceType.MINUTE, RecurrenceType.getRecurrenceTypeByValue("Minute(s)"));
        assertEquals(RecurrenceType.FOREVER, RecurrenceType.getRecurrenceTypeByValue("Forever"));
        assertEquals(RecurrenceType.NEVER, RecurrenceType.getRecurrenceTypeByValue("Never"));
    }

    @Test
    public void getRecurrenceTypeByValue_invalidValue_returnsNull() {
        assertNull(RecurrenceType.getRecurrenceTypeByValue("Invalid"));
        assertNull(RecurrenceType.getRecurrenceTypeByValue(""));
        assertNull(RecurrenceType.getRecurrenceTypeByValue(null));
    }

    @Test
    public void getRecurrenceTypeByValue_caseSensitive() {
        // The lookup is case-sensitive
        assertNull(RecurrenceType.getRecurrenceTypeByValue("year(s)"));
        assertNull(RecurrenceType.getRecurrenceTypeByValue("YEAR(S)"));
        assertNull(RecurrenceType.getRecurrenceTypeByValue("day(s)"));
    }

    // ==================== valueOf Tests ====================

    @Test
    public void valueOf_validNames_returnsCorrectEnum() {
        assertEquals(RecurrenceType.YEAR, RecurrenceType.valueOf("YEAR"));
        assertEquals(RecurrenceType.MONTH, RecurrenceType.valueOf("MONTH"));
        assertEquals(RecurrenceType.DAY, RecurrenceType.valueOf("DAY"));
        assertEquals(RecurrenceType.HOUR, RecurrenceType.valueOf("HOUR"));
        assertEquals(RecurrenceType.MINUTE, RecurrenceType.valueOf("MINUTE"));
        assertEquals(RecurrenceType.FOREVER, RecurrenceType.valueOf("FOREVER"));
        assertEquals(RecurrenceType.NEVER, RecurrenceType.valueOf("NEVER"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void valueOf_invalidName_throwsException() {
        RecurrenceType.valueOf("INVALID");
    }

    // ==================== Enum Properties Tests ====================

    @Test
    public void values_returnsAllSevenTypes() {
        RecurrenceType[] values = RecurrenceType.values();
        assertEquals(7, values.length);
    }

    @Test
    public void ordinal_maintainsOrder() {
        // Verify the declaration order
        assertEquals(0, RecurrenceType.YEAR.ordinal());
        assertEquals(1, RecurrenceType.MONTH.ordinal());
        assertEquals(2, RecurrenceType.DAY.ordinal());
        assertEquals(3, RecurrenceType.HOUR.ordinal());
        assertEquals(4, RecurrenceType.MINUTE.ordinal());
        assertEquals(5, RecurrenceType.FOREVER.ordinal());
        assertEquals(6, RecurrenceType.NEVER.ordinal());
    }

    // ==================== Interval Relationship Tests ====================

    @Test
    public void intervals_maintainCorrectRelationships() {
        // Verify relative sizes are correct
        assertTrue("Hour should be 60x Minute",
                RecurrenceType.HOUR.getMillis() == RecurrenceType.MINUTE.getMillis() * 60);
        assertTrue("Day should be 24x Hour",
                RecurrenceType.DAY.getMillis() == RecurrenceType.HOUR.getMillis() * 24);
        assertTrue("Month should be 31x Day",
                RecurrenceType.MONTH.getMillis() == RecurrenceType.DAY.getMillis() * 31);
        assertTrue("Year should be > Month",
                RecurrenceType.YEAR.getMillis() > RecurrenceType.MONTH.getMillis());
    }

    @Test
    public void nonRecurringTypes_returnZeroMillis() {
        // Both FOREVER and NEVER should return 0 for getMillis
        assertEquals("FOREVER should return 0", 0L, RecurrenceType.FOREVER.getMillis());
        assertEquals("NEVER should return 0", 0L, RecurrenceType.NEVER.getMillis());
    }
}
