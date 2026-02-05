package com.ava.notiva;

import static org.junit.Assert.*;

import com.ava.notiva.converter.DbTypeConverters;

import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Unit tests for {@link DbTypeConverters}.
 * Tests Calendar to Long and Long to Calendar conversions for Room database.
 */
public class DbTypeConvertersTest {

    // ==================== toCalendar Tests ====================

    @Test
    public void toCalendar_validMillis_returnsCalendar() {
        long timeInMillis = 1704067200000L; // 2024-01-01 00:00:00 UTC

        Calendar result = DbTypeConverters.toCalendar(timeInMillis);

        assertNotNull(result);
        assertEquals(timeInMillis, result.getTimeInMillis());
    }

    @Test
    public void toCalendar_nullMillis_returnsNull() {
        Calendar result = DbTypeConverters.toCalendar(null);

        assertNull(result);
    }

    @Test
    public void toCalendar_zeroMillis_returnsEpoch() {
        Calendar result = DbTypeConverters.toCalendar(0L);

        assertNotNull(result);
        assertEquals(0L, result.getTimeInMillis());

        // Should be epoch time (1970-01-01 00:00:00 UTC)
        Calendar epoch = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        epoch.setTimeInMillis(0);
        assertEquals(1970, epoch.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, epoch.get(Calendar.MONTH));
        assertEquals(1, epoch.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void toCalendar_negativeMillis_returnsPreEpochCalendar() {
        // Negative millis represents time before epoch
        long preEpoch = -86400000L; // 1 day before epoch

        Calendar result = DbTypeConverters.toCalendar(preEpoch);

        assertNotNull(result);
        assertEquals(preEpoch, result.getTimeInMillis());
    }

    @Test
    public void toCalendar_maxLong_handlesLargeValue() {
        // Test with a large but reasonable future date
        long farFuture = 4102444800000L; // 2100-01-01 00:00:00 UTC

        Calendar result = DbTypeConverters.toCalendar(farFuture);

        assertNotNull(result);
        assertEquals(farFuture, result.getTimeInMillis());
    }

    // ==================== fromCalendar Tests ====================

    @Test
    public void fromCalendar_validCalendar_returnsMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2024, Calendar.JANUARY, 15, 10, 30, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Long result = DbTypeConverters.fromCalendar(calendar);

        assertNotNull(result);
        assertEquals(calendar.getTimeInMillis(), (long) result);
    }

    @Test
    public void fromCalendar_nullCalendar_returnsNull() {
        Long result = DbTypeConverters.fromCalendar(null);

        assertNull(result);
    }

    @Test
    public void fromCalendar_epochCalendar_returnsZero() {
        Calendar epoch = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        epoch.setTimeInMillis(0);

        Long result = DbTypeConverters.fromCalendar(epoch);

        assertNotNull(result);
        assertEquals(0L, (long) result);
    }

    // ==================== Round-trip Tests ====================

    @Test
    public void roundTrip_calendarToMillisToCalendar_preservesValue() {
        Calendar original = Calendar.getInstance();
        original.set(2024, Calendar.MARCH, 15, 14, 30, 45);
        original.set(Calendar.MILLISECOND, 123);

        Long millis = DbTypeConverters.fromCalendar(original);
        Calendar restored = DbTypeConverters.toCalendar(millis);

        assertNotNull(restored);
        assertEquals(original.getTimeInMillis(), restored.getTimeInMillis());
        assertEquals(original.get(Calendar.YEAR), restored.get(Calendar.YEAR));
        assertEquals(original.get(Calendar.MONTH), restored.get(Calendar.MONTH));
        assertEquals(original.get(Calendar.DAY_OF_MONTH), restored.get(Calendar.DAY_OF_MONTH));
        assertEquals(original.get(Calendar.HOUR_OF_DAY), restored.get(Calendar.HOUR_OF_DAY));
        assertEquals(original.get(Calendar.MINUTE), restored.get(Calendar.MINUTE));
        assertEquals(original.get(Calendar.SECOND), restored.get(Calendar.SECOND));
        assertEquals(original.get(Calendar.MILLISECOND), restored.get(Calendar.MILLISECOND));
    }

    @Test
    public void roundTrip_millisToCalendarToMillis_preservesValue() {
        long originalMillis = 1710510645123L; // Some arbitrary timestamp

        Calendar calendar = DbTypeConverters.toCalendar(originalMillis);
        Long restoredMillis = DbTypeConverters.fromCalendar(calendar);

        assertNotNull(restoredMillis);
        assertEquals(originalMillis, (long) restoredMillis);
    }

    @Test
    public void roundTrip_multipleConversions_preservesValue() {
        Calendar original = Calendar.getInstance();
        original.set(2025, Calendar.DECEMBER, 31, 23, 59, 59);
        original.set(Calendar.MILLISECOND, 999);

        // Multiple round trips
        Long millis1 = DbTypeConverters.fromCalendar(original);
        Calendar cal1 = DbTypeConverters.toCalendar(millis1);
        Long millis2 = DbTypeConverters.fromCalendar(cal1);
        Calendar cal2 = DbTypeConverters.toCalendar(millis2);
        Long millis3 = DbTypeConverters.fromCalendar(cal2);

        assertEquals(millis1, millis2);
        assertEquals(millis2, millis3);
        assertEquals(original.getTimeInMillis(), (long) millis3);
    }

    // ==================== Specific Date Tests ====================

    @Test
    public void toCalendar_specificDate_hasCorrectFields() {
        // Create a calendar at a known time and convert round-trip
        Calendar original = Calendar.getInstance();
        original.set(2024, Calendar.JUNE, 15, 9, 30, 0);
        original.set(Calendar.MILLISECOND, 0);

        long specificTime = original.getTimeInMillis();
        Calendar result = DbTypeConverters.toCalendar(specificTime);

        assertNotNull(result);
        assertEquals(2024, result.get(Calendar.YEAR));
        assertEquals(Calendar.JUNE, result.get(Calendar.MONTH));
        assertEquals(15, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(9, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, result.get(Calendar.MINUTE));
        assertEquals(0, result.get(Calendar.SECOND));
    }

    @Test
    public void fromCalendar_specificDate_returnsCorrectMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2024, Calendar.JUNE, 15, 9, 30, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Long result = DbTypeConverters.fromCalendar(calendar);

        assertNotNull(result);
        assertEquals(calendar.getTimeInMillis(), (long) result);
    }

    // ==================== Edge Cases ====================

    @Test
    public void toCalendar_leapYearDate_handlesCorrectly() {
        // Feb 29, 2024 (leap year)
        Calendar leapDay = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        leapDay.set(2024, Calendar.FEBRUARY, 29, 12, 0, 0);
        leapDay.set(Calendar.MILLISECOND, 0);

        Long millis = DbTypeConverters.fromCalendar(leapDay);
        Calendar restored = DbTypeConverters.toCalendar(millis);
        restored.setTimeZone(TimeZone.getTimeZone("UTC"));

        assertEquals(Calendar.FEBRUARY, restored.get(Calendar.MONTH));
        assertEquals(29, restored.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void toCalendar_endOfYear_handlesCorrectly() {
        // Dec 31, 2024 23:59:59.999
        Calendar endOfYear = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        endOfYear.set(2024, Calendar.DECEMBER, 31, 23, 59, 59);
        endOfYear.set(Calendar.MILLISECOND, 999);

        Long millis = DbTypeConverters.fromCalendar(endOfYear);
        Calendar restored = DbTypeConverters.toCalendar(millis);
        restored.setTimeZone(TimeZone.getTimeZone("UTC"));

        assertEquals(2024, restored.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, restored.get(Calendar.MONTH));
        assertEquals(31, restored.get(Calendar.DAY_OF_MONTH));
        assertEquals(23, restored.get(Calendar.HOUR_OF_DAY));
        assertEquals(59, restored.get(Calendar.MINUTE));
        assertEquals(59, restored.get(Calendar.SECOND));
        assertEquals(999, restored.get(Calendar.MILLISECOND));
    }

    @Test
    public void toCalendar_startOfYear_handlesCorrectly() {
        // Jan 1, 2024 00:00:00.000
        Calendar startOfYear = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        startOfYear.set(2024, Calendar.JANUARY, 1, 0, 0, 0);
        startOfYear.set(Calendar.MILLISECOND, 0);

        Long millis = DbTypeConverters.fromCalendar(startOfYear);
        Calendar restored = DbTypeConverters.toCalendar(millis);
        restored.setTimeZone(TimeZone.getTimeZone("UTC"));

        assertEquals(2024, restored.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, restored.get(Calendar.MONTH));
        assertEquals(1, restored.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, restored.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, restored.get(Calendar.MINUTE));
        assertEquals(0, restored.get(Calendar.SECOND));
        assertEquals(0, restored.get(Calendar.MILLISECOND));
    }
}
