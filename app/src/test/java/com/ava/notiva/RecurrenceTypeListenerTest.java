package com.ava.notiva;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.ava.notiva.listener.RecurrenceTypeListener;
import com.ava.notiva.model.RecurrenceType;
import com.ava.notiva.model.ReminderModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link RecurrenceTypeListener}.
 * Tests that spinner selection correctly updates the ReminderModel recurrence type.
 */
public class RecurrenceTypeListenerTest {

    @Mock
    private AdapterView<?> mockAdapterView;

    @Mock
    private View mockView;

    @Mock
    private Runnable mockSummaryUpdater;

    private ReminderModel reminder;
    private RecurrenceTypeListener listener;
    private MockedStatic<Log> mockedLog;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockedLog = mockStatic(Log.class);
        mockedLog.when(() -> Log.i(anyString(), anyString())).thenReturn(0);
        reminder = new ReminderModel();
        listener = new RecurrenceTypeListener(reminder, mockSummaryUpdater);
    }

    @After
    public void tearDown() {
        mockedLog.close();
    }

    // ==================== onItemSelected Tests ====================

    @Test
    public void onItemSelected_day_updatesRecurrenceType() {
        when(mockAdapterView.getItemAtPosition(0)).thenReturn("Day(s)");

        listener.onItemSelected(mockAdapterView, mockView, 0, 0);

        assertEquals(RecurrenceType.DAY, reminder.getRecurrenceType());
    }

    @Test
    public void onItemSelected_hour_updatesRecurrenceType() {
        when(mockAdapterView.getItemAtPosition(1)).thenReturn("Hour(s)");

        listener.onItemSelected(mockAdapterView, mockView, 1, 1);

        assertEquals(RecurrenceType.HOUR, reminder.getRecurrenceType());
    }

    @Test
    public void onItemSelected_minute_updatesRecurrenceType() {
        when(mockAdapterView.getItemAtPosition(2)).thenReturn("Minute(s)");

        listener.onItemSelected(mockAdapterView, mockView, 2, 2);

        assertEquals(RecurrenceType.MINUTE, reminder.getRecurrenceType());
    }

    @Test
    public void onItemSelected_month_updatesRecurrenceType() {
        when(mockAdapterView.getItemAtPosition(3)).thenReturn("Month(s)");

        listener.onItemSelected(mockAdapterView, mockView, 3, 3);

        assertEquals(RecurrenceType.MONTH, reminder.getRecurrenceType());
    }

    @Test
    public void onItemSelected_year_updatesRecurrenceType() {
        when(mockAdapterView.getItemAtPosition(4)).thenReturn("Year(s)");

        listener.onItemSelected(mockAdapterView, mockView, 4, 4);

        assertEquals(RecurrenceType.YEAR, reminder.getRecurrenceType());
    }

    @Test
    public void onItemSelected_forever_updatesRecurrenceType() {
        when(mockAdapterView.getItemAtPosition(5)).thenReturn("Forever");

        listener.onItemSelected(mockAdapterView, mockView, 5, 5);

        assertEquals(RecurrenceType.FOREVER, reminder.getRecurrenceType());
    }

    @Test
    public void onItemSelected_never_updatesRecurrenceType() {
        when(mockAdapterView.getItemAtPosition(6)).thenReturn("Never");

        listener.onItemSelected(mockAdapterView, mockView, 6, 6);

        assertEquals(RecurrenceType.NEVER, reminder.getRecurrenceType());
    }

    @Test
    public void onItemSelected_callsSummaryUpdater() {
        when(mockAdapterView.getItemAtPosition(0)).thenReturn("Day(s)");

        listener.onItemSelected(mockAdapterView, mockView, 0, 0);

        verify(mockSummaryUpdater).run();
    }

    @Test
    public void onItemSelected_invalidValue_setsNull() {
        when(mockAdapterView.getItemAtPosition(0)).thenReturn("Invalid");

        listener.onItemSelected(mockAdapterView, mockView, 0, 0);

        // getRecurrenceTypeByValue returns null for invalid values
        assertNull(reminder.getRecurrenceType());
    }

    @Test
    public void onItemSelected_multipleSelections_updatesEachTime() {
        when(mockAdapterView.getItemAtPosition(0)).thenReturn("Day(s)");
        listener.onItemSelected(mockAdapterView, mockView, 0, 0);
        assertEquals(RecurrenceType.DAY, reminder.getRecurrenceType());

        when(mockAdapterView.getItemAtPosition(1)).thenReturn("Hour(s)");
        listener.onItemSelected(mockAdapterView, mockView, 1, 1);
        assertEquals(RecurrenceType.HOUR, reminder.getRecurrenceType());

        when(mockAdapterView.getItemAtPosition(2)).thenReturn("Never");
        listener.onItemSelected(mockAdapterView, mockView, 2, 2);
        assertEquals(RecurrenceType.NEVER, reminder.getRecurrenceType());

        verify(mockSummaryUpdater, times(3)).run();
    }

    // ==================== onNothingSelected Tests ====================

    @Test
    public void onNothingSelected_doesNotChangeModel() {
        reminder.setRecurrenceType(RecurrenceType.DAY);

        listener.onNothingSelected(mockAdapterView);

        assertEquals(RecurrenceType.DAY, reminder.getRecurrenceType());
    }

    @Test
    public void onNothingSelected_doesNotCallSummaryUpdater() {
        listener.onNothingSelected(mockAdapterView);

        verify(mockSummaryUpdater, never()).run();
    }

    // ==================== Multiple Reminders ====================

    @Test
    public void listener_boundToSpecificReminder() {
        ReminderModel reminder1 = new ReminderModel();
        ReminderModel reminder2 = new ReminderModel();

        RecurrenceTypeListener listener1 = new RecurrenceTypeListener(reminder1, mockSummaryUpdater);
        RecurrenceTypeListener listener2 = new RecurrenceTypeListener(reminder2, mockSummaryUpdater);

        when(mockAdapterView.getItemAtPosition(0)).thenReturn("Day(s)");
        listener1.onItemSelected(mockAdapterView, mockView, 0, 0);

        when(mockAdapterView.getItemAtPosition(1)).thenReturn("Hour(s)");
        listener2.onItemSelected(mockAdapterView, mockView, 1, 1);

        assertEquals(RecurrenceType.DAY, reminder1.getRecurrenceType());
        assertEquals(RecurrenceType.HOUR, reminder2.getRecurrenceType());
    }

    // ==================== Edge Cases ====================

    @Test
    public void onItemSelected_nullView_stillUpdatesModel() {
        when(mockAdapterView.getItemAtPosition(0)).thenReturn("Day(s)");

        listener.onItemSelected(mockAdapterView, null, 0, 0);

        assertEquals(RecurrenceType.DAY, reminder.getRecurrenceType());
    }

    @Test
    public void onItemSelected_caseSensitiveMatching() {
        // Values are case-sensitive
        when(mockAdapterView.getItemAtPosition(0)).thenReturn("day(s)");

        listener.onItemSelected(mockAdapterView, mockView, 0, 0);

        // Should return null due to case mismatch
        assertNull(reminder.getRecurrenceType());
    }

    @Test
    public void nullSummaryUpdater_throwsNullPointerException() {
        RecurrenceTypeListener listenerWithNullUpdater =
            new RecurrenceTypeListener(reminder, null);

        when(mockAdapterView.getItemAtPosition(0)).thenReturn("Day(s)");

        try {
            listenerWithNullUpdater.onItemSelected(mockAdapterView, mockView, 0, 0);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected - no null check in implementation
        }
    }
}
