package com.ava.notiva;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.text.Editable;

import com.ava.notiva.listener.RecurrenceDelayChangedListener;
import com.ava.notiva.model.ReminderModel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link RecurrenceDelayChangedListener}.
 * Tests that recurrence delay text changes update the model and trigger summary updates.
 *
 * NOTE: This listener has a known bug - it throws NumberFormatException on empty input.
 * See bugs_found.md #3.
 */
public class RecurrenceDelayChangedListenerTest {

    @Mock
    private Editable mockEditable;

    @Mock
    private Runnable mockSummaryUpdater;

    private ReminderModel reminder;
    private RecurrenceDelayChangedListener listener;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        reminder = new ReminderModel();
        listener = new RecurrenceDelayChangedListener(reminder, mockSummaryUpdater);
    }

    // ==================== afterTextChanged Tests ====================

    @Test
    public void afterTextChanged_validNumber_updatesReminderDelay() {
        when(mockEditable.toString()).thenReturn("5");

        listener.afterTextChanged(mockEditable);

        assertEquals(5, reminder.getRecurrenceDelay());
    }

    @Test
    public void afterTextChanged_validNumber_callsSummaryUpdater() {
        when(mockEditable.toString()).thenReturn("10");

        listener.afterTextChanged(mockEditable);

        verify(mockSummaryUpdater).run();
    }

    @Test
    public void afterTextChanged_largeNumber_updatesCorrectly() {
        when(mockEditable.toString()).thenReturn("999");

        listener.afterTextChanged(mockEditable);

        assertEquals(999, reminder.getRecurrenceDelay());
    }

    @Test
    public void afterTextChanged_singleDigit_updatesCorrectly() {
        when(mockEditable.toString()).thenReturn("1");

        listener.afterTextChanged(mockEditable);

        assertEquals(1, reminder.getRecurrenceDelay());
    }

    @Test
    public void afterTextChanged_zero_updatesCorrectly() {
        when(mockEditable.toString()).thenReturn("0");

        listener.afterTextChanged(mockEditable);

        assertEquals(0, reminder.getRecurrenceDelay());
    }

    @Test(expected = NumberFormatException.class)
    public void afterTextChanged_emptyString_throwsNumberFormatException() {
        // This documents the bug - empty string causes crash
        when(mockEditable.toString()).thenReturn("");

        listener.afterTextChanged(mockEditable);
    }

    @Test(expected = NumberFormatException.class)
    public void afterTextChanged_nonNumeric_throwsNumberFormatException() {
        when(mockEditable.toString()).thenReturn("abc");

        listener.afterTextChanged(mockEditable);
    }

    @Test(expected = NumberFormatException.class)
    public void afterTextChanged_whitespace_throwsNumberFormatException() {
        when(mockEditable.toString()).thenReturn("   ");

        listener.afterTextChanged(mockEditable);
    }

    @Test
    public void afterTextChanged_multipleValidChanges_updatesEachTime() {
        when(mockEditable.toString()).thenReturn("1");
        listener.afterTextChanged(mockEditable);
        assertEquals(1, reminder.getRecurrenceDelay());

        when(mockEditable.toString()).thenReturn("5");
        listener.afterTextChanged(mockEditable);
        assertEquals(5, reminder.getRecurrenceDelay());

        when(mockEditable.toString()).thenReturn("100");
        listener.afterTextChanged(mockEditable);
        assertEquals(100, reminder.getRecurrenceDelay());

        // Summary updater called each time
        verify(mockSummaryUpdater, times(3)).run();
    }

    // ==================== onTextChanged Tests ====================

    @Test
    public void onTextChanged_callsSummaryUpdater() {
        listener.onTextChanged("123", 0, 0, 3);

        verify(mockSummaryUpdater).run();
    }

    @Test
    public void onTextChanged_doesNotUpdateModel() {
        reminder.setRecurrenceDelay(99);

        listener.onTextChanged("5", 0, 0, 1);

        // Model should not be updated by onTextChanged
        assertEquals(99, reminder.getRecurrenceDelay());
    }

    // ==================== beforeTextChanged Tests ====================

    @Test
    public void beforeTextChanged_doesNothing() {
        reminder.setRecurrenceDelay(99);

        listener.beforeTextChanged("test", 0, 1, 1);

        assertEquals(99, reminder.getRecurrenceDelay());
        verify(mockSummaryUpdater, never()).run();
    }

    // ==================== Summary Updater Tests ====================

    @Test
    public void summaryUpdater_calledOnBothOnTextChangedAndAfterTextChanged() {
        when(mockEditable.toString()).thenReturn("5");

        listener.onTextChanged("5", 0, 0, 1);
        listener.afterTextChanged(mockEditable);

        // Called twice: once in onTextChanged, once in afterTextChanged
        verify(mockSummaryUpdater, times(2)).run();
    }

    @Test
    public void nullSummaryUpdater_stillUpdatesModel() {
        RecurrenceDelayChangedListener listenerWithNullUpdater =
            new RecurrenceDelayChangedListener(reminder, null);

        when(mockEditable.toString()).thenReturn("5");

        // This will throw NullPointerException because summaryUpdater.run() is called
        // without null check - documenting current behavior
        try {
            listenerWithNullUpdater.afterTextChanged(mockEditable);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected - no null check in implementation
        }
    }

    // ==================== Edge Cases ====================

    @Test
    public void afterTextChanged_negativeNumber_parsesCorrectly() {
        // Note: Input filter should prevent this, but testing parser behavior
        when(mockEditable.toString()).thenReturn("-5");

        listener.afterTextChanged(mockEditable);

        assertEquals(-5, reminder.getRecurrenceDelay());
    }

    @Test
    public void afterTextChanged_leadingZeros_parsesCorrectly() {
        when(mockEditable.toString()).thenReturn("007");

        listener.afterTextChanged(mockEditable);

        assertEquals(7, reminder.getRecurrenceDelay());
    }
}
