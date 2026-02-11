package com.ava.notiva;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import android.content.Context;

import com.ava.notiva.model.RecurrenceType;
import com.ava.notiva.util.RecurrenceDisplayUtil;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link RecurrenceDisplayUtil}.
 * Uses mocked Context to avoid Android dependency.
 */
public class RecurrenceDisplayUtilTest {

    @Mock
    private Context mockContext;

    private static final String DEFAULT_SUMMARY = "Repeats as per your settings";
    private static final String NUMBER_REQUIRED = "Please enter a recurrence number";
    private static final String DATE_DEFAULT = "Ends on: Tue, 29 Jun";
    private static final String TIME_DEFAULT = "Ends at: 11:52 AM";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockContext.getString(R.string.ara_recurrence_summary_default)).thenReturn(DEFAULT_SUMMARY);
        when(mockContext.getString(R.string.ara_recurrence_number_required)).thenReturn(NUMBER_REQUIRED);
        when(mockContext.getString(R.string.ara_date_default)).thenReturn(DATE_DEFAULT);
        when(mockContext.getString(R.string.ara_time_default)).thenReturn(TIME_DEFAULT);
    }

    // ==================== Null/NEVER Type ====================

    @Test
    public void getRecurrenceSummary_nullType_returnsDefault() {
        String result = RecurrenceDisplayUtil.getRecurrenceSummary(
                mockContext, "1", null, null, null);
        assertEquals(DEFAULT_SUMMARY, result);
    }

    @Test
    public void getRecurrenceSummary_neverType_returnsDefault() {
        String result = RecurrenceDisplayUtil.getRecurrenceSummary(
                mockContext, "1", RecurrenceType.NEVER, null, null);
        assertEquals(DEFAULT_SUMMARY, result);
    }

    // ==================== Empty/Null Number ====================

    @Test
    public void getRecurrenceSummary_nullNumber_returnsNumberRequired() {
        String result = RecurrenceDisplayUtil.getRecurrenceSummary(
                mockContext, null, RecurrenceType.DAY, null, null);
        assertEquals(NUMBER_REQUIRED, result);
    }

    @Test
    public void getRecurrenceSummary_emptyNumber_returnsNumberRequired() {
        String result = RecurrenceDisplayUtil.getRecurrenceSummary(
                mockContext, "", RecurrenceType.DAY, null, null);
        assertEquals(NUMBER_REQUIRED, result);
    }

    @Test
    public void getRecurrenceSummary_whitespaceNumber_returnsNumberRequired() {
        String result = RecurrenceDisplayUtil.getRecurrenceSummary(
                mockContext, "   ", RecurrenceType.DAY, null, null);
        assertEquals(NUMBER_REQUIRED, result);
    }

    // ==================== FOREVER Type ====================

    @Test
    public void getRecurrenceSummary_foreverType_returnsForever() {
        String result = RecurrenceDisplayUtil.getRecurrenceSummary(
                mockContext, "1", RecurrenceType.FOREVER, null, null);
        assertEquals("forever", result);
    }

    // ==================== Singular Pluralization (count = 1) ====================

    @Test
    public void getRecurrenceSummary_oneDay_singular() {
        String result = RecurrenceDisplayUtil.getRecurrenceSummary(
                mockContext, "1", RecurrenceType.DAY, null, null);
        assertEquals("Every 1 day", result);
    }

    @Test
    public void getRecurrenceSummary_oneHour_singular() {
        String result = RecurrenceDisplayUtil.getRecurrenceSummary(
                mockContext, "1", RecurrenceType.HOUR, null, null);
        assertEquals("Every 1 hour", result);
    }

    @Test
    public void getRecurrenceSummary_oneMinute_singular() {
        String result = RecurrenceDisplayUtil.getRecurrenceSummary(
                mockContext, "1", RecurrenceType.MINUTE, null, null);
        assertEquals("Every 1 minute", result);
    }

    @Test
    public void getRecurrenceSummary_oneMonth_singular() {
        String result = RecurrenceDisplayUtil.getRecurrenceSummary(
                mockContext, "1", RecurrenceType.MONTH, null, null);
        assertEquals("Every 1 month", result);
    }

    @Test
    public void getRecurrenceSummary_oneYear_singular() {
        String result = RecurrenceDisplayUtil.getRecurrenceSummary(
                mockContext, "1", RecurrenceType.YEAR, null, null);
        assertEquals("Every 1 year", result);
    }

    // ==================== Plural (count > 1) ====================

    @Test
    public void getRecurrenceSummary_twoDays_plural() {
        String result = RecurrenceDisplayUtil.getRecurrenceSummary(
                mockContext, "2", RecurrenceType.DAY, null, null);
        assertEquals("Every 2 days", result);
    }

    @Test
    public void getRecurrenceSummary_threeHours_plural() {
        String result = RecurrenceDisplayUtil.getRecurrenceSummary(
                mockContext, "3", RecurrenceType.HOUR, null, null);
        assertEquals("Every 3 hours", result);
    }

    @Test
    public void getRecurrenceSummary_fiveMinutes_plural() {
        String result = RecurrenceDisplayUtil.getRecurrenceSummary(
                mockContext, "5", RecurrenceType.MINUTE, null, null);
        assertEquals("Every 5 minutes", result);
    }

    @Test
    public void getRecurrenceSummary_sixMonths_plural() {
        String result = RecurrenceDisplayUtil.getRecurrenceSummary(
                mockContext, "6", RecurrenceType.MONTH, null, null);
        assertEquals("Every 6 months", result);
    }

    @Test
    public void getRecurrenceSummary_twoYears_plural() {
        String result = RecurrenceDisplayUtil.getRecurrenceSummary(
                mockContext, "2", RecurrenceType.YEAR, null, null);
        assertEquals("Every 2 years", result);
    }

    // ==================== End Date/Time ====================

    @Test
    public void getRecurrenceSummary_withEndDate_appendsTill() {
        String result = RecurrenceDisplayUtil.getRecurrenceSummary(
                mockContext, "1", RecurrenceType.DAY, "Mon, 15 Jul", null);
        assertEquals("Every 1 day till Mon, 15 Jul", result);
    }

    @Test
    public void getRecurrenceSummary_withEndTime_appendsTill() {
        String result = RecurrenceDisplayUtil.getRecurrenceSummary(
                mockContext, "1", RecurrenceType.DAY, null, "3:30 PM");
        assertEquals("Every 1 day till 3:30 PM", result);
    }

    @Test
    public void getRecurrenceSummary_withBothEndDateAndTime_appendsBoth() {
        String result = RecurrenceDisplayUtil.getRecurrenceSummary(
                mockContext, "1", RecurrenceType.DAY, "Mon, 15 Jul", "3:30 PM");
        assertEquals("Every 1 day till 3:30 PM Mon, 15 Jul", result);
    }

    @Test
    public void getRecurrenceSummary_defaultEndDate_ignored() {
        // When endDate equals the default placeholder, it should be treated as "not set"
        String result = RecurrenceDisplayUtil.getRecurrenceSummary(
                mockContext, "1", RecurrenceType.DAY, DATE_DEFAULT, null);
        assertEquals("Every 1 day", result);
    }

    @Test
    public void getRecurrenceSummary_defaultEndTime_ignored() {
        String result = RecurrenceDisplayUtil.getRecurrenceSummary(
                mockContext, "1", RecurrenceType.DAY, null, TIME_DEFAULT);
        assertEquals("Every 1 day", result);
    }

    @Test
    public void getRecurrenceSummary_emptyEndDate_ignored() {
        String result = RecurrenceDisplayUtil.getRecurrenceSummary(
                mockContext, "1", RecurrenceType.DAY, "", null);
        assertEquals("Every 1 day", result);
    }

    @Test
    public void getRecurrenceSummary_whitespaceEndDate_ignored() {
        String result = RecurrenceDisplayUtil.getRecurrenceSummary(
                mockContext, "1", RecurrenceType.DAY, "   ", null);
        assertEquals("Every 1 day", result);
    }

    // ==================== Non-Numeric Number Input ====================

    @Test
    public void getRecurrenceSummary_nonNumericNumber_defaultsToSingular() {
        // parseInt will throw, caught by catch block, count defaults to 1
        String result = RecurrenceDisplayUtil.getRecurrenceSummary(
                mockContext, "abc", RecurrenceType.DAY, null, null);
        assertEquals("Every abc day", result);
    }

    // ==================== Edge Cases ====================

    @Test
    public void getRecurrenceSummary_largeNumber_plural() {
        String result = RecurrenceDisplayUtil.getRecurrenceSummary(
                mockContext, "100", RecurrenceType.MINUTE, null, null);
        assertEquals("Every 100 minutes", result);
    }

    @Test
    public void getRecurrenceSummary_zeroNumber_plural() {
        // 0 != 1, so should be plural
        String result = RecurrenceDisplayUtil.getRecurrenceSummary(
                mockContext, "0", RecurrenceType.DAY, null, null);
        assertEquals("Every 0 days", result);
    }

    @Test
    public void getRecurrenceSummary_negativeNumber_plural() {
        String result = RecurrenceDisplayUtil.getRecurrenceSummary(
                mockContext, "-2", RecurrenceType.DAY, null, null);
        assertEquals("Every -2 days", result);
    }

    @Test
    public void getRecurrenceSummary_forever_ignoresEndDate() {
        // FOREVER should just return "forever" regardless of end date
        String result = RecurrenceDisplayUtil.getRecurrenceSummary(
                mockContext, "1", RecurrenceType.FOREVER, "Mon, 15 Jul", "3:30 PM");
        assertEquals("forever", result);
    }
}
