package com.ava.notiva;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.text.Spanned;

import com.ava.notiva.util.InputFilterMinMax;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link InputFilterMinMax}.
 * Tests input filtering for numeric range validation.
 */
public class InputFilterMinMaxTest {

    @Mock
    private Spanned mockDest;

    private InputFilterMinMax filter;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ==================== Basic Range Tests ====================

    @Test
    public void filter_valueWithinRange_returnsNull() {
        filter = new InputFilterMinMax(1, 100);
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.length()).thenReturn(0);

        // Typing "50" into empty field
        CharSequence result = filter.filter("50", 0, 2, mockDest, 0, 0);

        assertNull("Value within range should be accepted (null means accept)", result);
    }

    @Test
    public void filter_valueAtMinBoundary_returnsNull() {
        filter = new InputFilterMinMax(1, 100);
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.length()).thenReturn(0);

        CharSequence result = filter.filter("1", 0, 1, mockDest, 0, 0);

        assertNull("Min boundary value should be accepted", result);
    }

    @Test
    public void filter_valueAtMaxBoundary_returnsNull() {
        filter = new InputFilterMinMax(1, 100);
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.length()).thenReturn(0);

        CharSequence result = filter.filter("100", 0, 3, mockDest, 0, 0);

        assertNull("Max boundary value should be accepted", result);
    }

    @Test
    public void filter_valueBelowMin_returnsEmptyString() {
        filter = new InputFilterMinMax(10, 100);
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.length()).thenReturn(0);

        CharSequence result = filter.filter("5", 0, 1, mockDest, 0, 0);

        assertEquals("Value below min should be rejected", "", result);
    }

    @Test
    public void filter_valueAboveMax_returnsEmptyString() {
        filter = new InputFilterMinMax(1, 100);
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.length()).thenReturn(0);

        CharSequence result = filter.filter("101", 0, 3, mockDest, 0, 0);

        assertEquals("Value above max should be rejected", "", result);
    }

    // ==================== Appending to Existing Value ====================

    @Test
    public void filter_appendDigitWithinRange_returnsNull() {
        filter = new InputFilterMinMax(1, 100);
        // Existing value is "5", appending "0" to make "50"
        when(mockDest.subSequence(0, 1)).thenReturn("5");
        when(mockDest.subSequence(1, 1)).thenReturn("");
        when(mockDest.length()).thenReturn(1);

        CharSequence result = filter.filter("0", 0, 1, mockDest, 1, 1);

        assertNull("Appending to make valid value should be accepted", result);
    }

    @Test
    public void filter_appendDigitExceedsMax_returnsEmptyString() {
        filter = new InputFilterMinMax(1, 100);
        // Existing value is "10", appending "1" to make "101"
        when(mockDest.subSequence(0, 2)).thenReturn("10");
        when(mockDest.subSequence(2, 2)).thenReturn("");
        when(mockDest.length()).thenReturn(2);

        CharSequence result = filter.filter("1", 0, 1, mockDest, 2, 2);

        assertEquals("Appending to exceed max should be rejected", "", result);
    }

    // ==================== Empty Input ====================

    @Test
    public void filter_emptyInput_returnsNull() {
        filter = new InputFilterMinMax(1, 100);
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.length()).thenReturn(0);

        // Empty string (clearing field)
        CharSequence result = filter.filter("", 0, 0, mockDest, 0, 0);

        assertNull("Empty input should be allowed (for clearing)", result);
    }

    // ==================== Non-numeric Input ====================

    @Test
    public void filter_nonNumericInput_returnsEmptyString() {
        filter = new InputFilterMinMax(1, 100);
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.length()).thenReturn(0);

        CharSequence result = filter.filter("abc", 0, 3, mockDest, 0, 0);

        assertEquals("Non-numeric input should be rejected", "", result);
    }

    @Test
    public void filter_mixedInput_returnsEmptyString() {
        filter = new InputFilterMinMax(1, 100);
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.length()).thenReturn(0);

        CharSequence result = filter.filter("12a", 0, 3, mockDest, 0, 0);

        assertEquals("Mixed alphanumeric input should be rejected", "", result);
    }

    // ==================== Zero Value ====================

    @Test
    public void filter_zeroWithinRange_returnsNull() {
        filter = new InputFilterMinMax(0, 100);
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.length()).thenReturn(0);

        CharSequence result = filter.filter("0", 0, 1, mockDest, 0, 0);

        assertNull("Zero within range should be accepted", result);
    }

    @Test
    public void filter_zeroOutsideRange_returnsEmptyString() {
        filter = new InputFilterMinMax(1, 100);
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.length()).thenReturn(0);

        CharSequence result = filter.filter("0", 0, 1, mockDest, 0, 0);

        assertEquals("Zero outside range should be rejected", "", result);
    }

    // ==================== Reversed Range (min > max) ====================

    @Test
    public void filter_reversedRange_stillWorksCorrectly() {
        // Filter handles reversed ranges (isInRange checks both directions)
        filter = new InputFilterMinMax(100, 1); // Reversed!
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.length()).thenReturn(0);

        CharSequence result = filter.filter("50", 0, 2, mockDest, 0, 0);

        assertNull("Value within reversed range should be accepted", result);
    }

    // ==================== Large Numbers ====================

    @Test
    public void filter_largeNumberWithinRange_returnsNull() {
        filter = new InputFilterMinMax(0, Integer.MAX_VALUE);
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.length()).thenReturn(0);

        CharSequence result = filter.filter("999999", 0, 6, mockDest, 0, 0);

        assertNull("Large number within range should be accepted", result);
    }

    // ==================== Negative Numbers ====================

    @Test
    public void filter_negativeNumberWithinRange_returnsNull() {
        filter = new InputFilterMinMax(-100, 100);
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.length()).thenReturn(0);

        CharSequence result = filter.filter("-50", 0, 3, mockDest, 0, 0);

        assertNull("Negative number within range should be accepted", result);
    }

    @Test
    public void filter_negativeNumberOutsideRange_returnsEmptyString() {
        filter = new InputFilterMinMax(0, 100);
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.length()).thenReturn(0);

        CharSequence result = filter.filter("-5", 0, 2, mockDest, 0, 0);

        assertEquals("Negative number outside range should be rejected", "", result);
    }

    // ==================== Single Digit Range ====================

    @Test
    public void filter_singleValueRange_onlyAcceptsThatValue() {
        filter = new InputFilterMinMax(5, 5);
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.subSequence(0, 0)).thenReturn("");
        when(mockDest.length()).thenReturn(0);

        CharSequence result5 = filter.filter("5", 0, 1, mockDest, 0, 0);
        CharSequence result4 = filter.filter("4", 0, 1, mockDest, 0, 0);
        CharSequence result6 = filter.filter("6", 0, 1, mockDest, 0, 0);

        assertNull("Exact value should be accepted", result5);
        assertEquals("Value below should be rejected", "", result4);
        assertEquals("Value above should be rejected", "", result6);
    }
}
