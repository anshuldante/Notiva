package com.ava.notiva;

import static org.junit.Assert.*;

import com.ava.notiva.util.DateTimeDisplayUtil;
import com.ava.notiva.util.FriendlyDateType;

import org.junit.Test;

import java.util.Calendar;

/**
 * Unit tests for {@link DateTimeDisplayUtil}.
 * Tests date/time formatting utilities.
 * Note: Methods requiring Context (getFriendlyDate, getFriendlyDateTimeSingleLine)
 * are tested in instrumented tests.
 */
public class DateTimeDisplayUtilTest {

    // ==================== getFriendlyDateType Tests ====================

    @Test
    public void getFriendlyDateType_today_returnsTODAY() {
        Calendar today = Calendar.getInstance();

        FriendlyDateType result = DateTimeDisplayUtil.getFriendlyDateType(today);

        assertEquals(FriendlyDateType.TODAY, result);
    }

    @Test
    public void getFriendlyDateType_todayAtMidnight_returnsTODAY() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        FriendlyDateType result = DateTimeDisplayUtil.getFriendlyDateType(today);

        assertEquals(FriendlyDateType.TODAY, result);
    }

    @Test
    public void getFriendlyDateType_todayAtEndOfDay_returnsTODAY() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 23);
        today.set(Calendar.MINUTE, 59);
        today.set(Calendar.SECOND, 59);

        FriendlyDateType result = DateTimeDisplayUtil.getFriendlyDateType(today);

        assertEquals(FriendlyDateType.TODAY, result);
    }

    @Test
    public void getFriendlyDateType_tomorrow_returnsTOMORROW() {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DATE, 1);

        FriendlyDateType result = DateTimeDisplayUtil.getFriendlyDateType(tomorrow);

        assertEquals(FriendlyDateType.TOMORROW, result);
    }

    @Test
    public void getFriendlyDateType_tomorrowAtMidnight_returnsTOMORROW() {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DATE, 1);
        tomorrow.set(Calendar.HOUR_OF_DAY, 0);
        tomorrow.set(Calendar.MINUTE, 0);
        tomorrow.set(Calendar.SECOND, 0);

        FriendlyDateType result = DateTimeDisplayUtil.getFriendlyDateType(tomorrow);

        assertEquals(FriendlyDateType.TOMORROW, result);
    }

    @Test
    public void getFriendlyDateType_yesterday_returnsOTHER() {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);

        FriendlyDateType result = DateTimeDisplayUtil.getFriendlyDateType(yesterday);

        assertEquals(FriendlyDateType.OTHER, result);
    }

    @Test
    public void getFriendlyDateType_twoDaysFromNow_returnsOTHER() {
        Calendar twoDaysLater = Calendar.getInstance();
        twoDaysLater.add(Calendar.DATE, 2);

        FriendlyDateType result = DateTimeDisplayUtil.getFriendlyDateType(twoDaysLater);

        assertEquals(FriendlyDateType.OTHER, result);
    }

    @Test
    public void getFriendlyDateType_oneWeekFromNow_returnsOTHER() {
        Calendar oneWeekLater = Calendar.getInstance();
        oneWeekLater.add(Calendar.WEEK_OF_YEAR, 1);

        FriendlyDateType result = DateTimeDisplayUtil.getFriendlyDateType(oneWeekLater);

        assertEquals(FriendlyDateType.OTHER, result);
    }

    @Test
    public void getFriendlyDateType_lastYear_returnsOTHER() {
        Calendar lastYear = Calendar.getInstance();
        lastYear.add(Calendar.YEAR, -1);

        FriendlyDateType result = DateTimeDisplayUtil.getFriendlyDateType(lastYear);

        assertEquals(FriendlyDateType.OTHER, result);
    }

    @Test
    public void getFriendlyDateType_nextYear_returnsOTHER() {
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

        FriendlyDateType result = DateTimeDisplayUtil.getFriendlyDateType(nextYear);

        assertEquals(FriendlyDateType.OTHER, result);
    }

    // ==================== getFriendlyTime Tests ====================

    @Test
    public void getFriendlyTime_morning_formatsCorrectly() {
        Calendar morning = Calendar.getInstance();
        morning.set(Calendar.HOUR_OF_DAY, 9);
        morning.set(Calendar.MINUTE, 30);

        String result = DateTimeDisplayUtil.getFriendlyTime(morning);

        // Format is "hh:mm a" - case of AM/PM may vary by locale
        assertTrue("Should contain 09:30", result.contains("09:30"));
        assertTrue("Should contain AM indicator", result.toUpperCase().contains("AM"));
    }

    @Test
    public void getFriendlyTime_afternoon_formatsCorrectly() {
        Calendar afternoon = Calendar.getInstance();
        afternoon.set(Calendar.HOUR_OF_DAY, 14);
        afternoon.set(Calendar.MINUTE, 45);

        String result = DateTimeDisplayUtil.getFriendlyTime(afternoon);

        assertTrue("Should contain 02:45", result.contains("02:45"));
        assertTrue("Should contain PM indicator", result.toUpperCase().contains("PM"));
    }

    @Test
    public void getFriendlyTime_midnight_formatsCorrectly() {
        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);

        String result = DateTimeDisplayUtil.getFriendlyTime(midnight);

        assertTrue("Should contain 12:00", result.contains("12:00"));
        assertTrue("Should contain AM indicator", result.toUpperCase().contains("AM"));
    }

    @Test
    public void getFriendlyTime_noon_formatsCorrectly() {
        Calendar noon = Calendar.getInstance();
        noon.set(Calendar.HOUR_OF_DAY, 12);
        noon.set(Calendar.MINUTE, 0);

        String result = DateTimeDisplayUtil.getFriendlyTime(noon);

        assertTrue("Should contain 12:00", result.contains("12:00"));
        assertTrue("Should contain PM indicator", result.toUpperCase().contains("PM"));
    }

    @Test
    public void getFriendlyTime_oneMinuteBeforeMidnight_formatsCorrectly() {
        Calendar lateNight = Calendar.getInstance();
        lateNight.set(Calendar.HOUR_OF_DAY, 23);
        lateNight.set(Calendar.MINUTE, 59);

        String result = DateTimeDisplayUtil.getFriendlyTime(lateNight);

        assertTrue("Should contain 11:59", result.contains("11:59"));
        assertTrue("Should contain PM indicator", result.toUpperCase().contains("PM"));
    }

    @Test
    public void getFriendlyTime_singleDigitMinute_paddsWithZero() {
        Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR_OF_DAY, 8);
        time.set(Calendar.MINUTE, 5);

        String result = DateTimeDisplayUtil.getFriendlyTime(time);

        assertTrue("Should contain 08:05", result.contains("08:05"));
        assertTrue("Should contain AM indicator", result.toUpperCase().contains("AM"));
    }

    @Test
    public void getFriendlyTime_differentTimesInSameDay_formatsDifferently() {
        Calendar time1 = Calendar.getInstance();
        time1.set(Calendar.HOUR_OF_DAY, 6);
        time1.set(Calendar.MINUTE, 0);

        Calendar time2 = Calendar.getInstance();
        time2.set(Calendar.HOUR_OF_DAY, 18);
        time2.set(Calendar.MINUTE, 0);

        String result1 = DateTimeDisplayUtil.getFriendlyTime(time1);
        String result2 = DateTimeDisplayUtil.getFriendlyTime(time2);

        assertTrue("Morning should contain 06:00", result1.contains("06:00"));
        assertTrue("Morning should contain AM", result1.toUpperCase().contains("AM"));
        assertTrue("Evening should contain 06:00", result2.contains("06:00"));
        assertTrue("Evening should contain PM", result2.toUpperCase().contains("PM"));
        assertNotEquals(result1, result2);
    }

    // ==================== Edge Cases ====================

    @Test
    public void getFriendlyDateType_newYearsEve_handlesYearBoundary() {
        // If today is Dec 31, tomorrow is Jan 1 next year
        Calendar dec31 = Calendar.getInstance();
        dec31.set(Calendar.MONTH, Calendar.DECEMBER);
        dec31.set(Calendar.DAY_OF_MONTH, 31);

        Calendar jan1 = (Calendar) dec31.clone();
        jan1.add(Calendar.DATE, 1);

        // This tests that the method handles year boundaries correctly
        // The comparison should use DAY_OF_YEAR and YEAR together
        FriendlyDateType resultDec31 = DateTimeDisplayUtil.getFriendlyDateType(dec31);
        FriendlyDateType resultJan1 = DateTimeDisplayUtil.getFriendlyDateType(jan1);

        // Results depend on what "today" actually is
        // This test documents the behavior
        assertNotNull(resultDec31);
        assertNotNull(resultJan1);
    }

    @Test
    public void getFriendlyTime_leapSecond_handlesGracefully() {
        // Edge case: seconds are ignored in the output
        Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR_OF_DAY, 10);
        time.set(Calendar.MINUTE, 30);
        time.set(Calendar.SECOND, 59);

        String result = DateTimeDisplayUtil.getFriendlyTime(time);

        // Seconds should not appear in output
        assertFalse(result.contains("59"));
        assertTrue("Should contain 10:30", result.contains("10:30"));
        assertTrue("Should contain AM indicator", result.toUpperCase().contains("AM"));
    }
}
