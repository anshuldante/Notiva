package com.ava.notiva;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.text.Editable;
import android.util.Log;

import com.ava.notiva.listener.ReminderNameChangedListener;
import com.ava.notiva.model.ReminderModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link ReminderNameChangedListener}.
 * Tests that text changes correctly update the ReminderModel name.
 */
public class ReminderNameChangedListenerTest {

    @Mock
    private Editable mockEditable;

    private ReminderModel reminder;
    private ReminderNameChangedListener listener;
    private MockedStatic<Log> mockedLog;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockedLog = mockStatic(Log.class);
        mockedLog.when(() -> Log.i(anyString(), anyString())).thenReturn(0);
        reminder = new ReminderModel();
        listener = new ReminderNameChangedListener(reminder);
    }

    @After
    public void tearDown() {
        mockedLog.close();
    }

    // ==================== afterTextChanged Tests ====================

    @Test
    public void afterTextChanged_updatesReminderName() {
        when(mockEditable.toString()).thenReturn("New Name");

        listener.afterTextChanged(mockEditable);

        assertEquals("New Name", reminder.getName());
    }

    @Test
    public void afterTextChanged_trimsWhitespace() {
        when(mockEditable.toString()).thenReturn("  Trimmed Name  ");

        listener.afterTextChanged(mockEditable);

        assertEquals("Trimmed Name", reminder.getName());
    }

    @Test
    public void afterTextChanged_handlesEmptyString() {
        when(mockEditable.toString()).thenReturn("");

        listener.afterTextChanged(mockEditable);

        assertEquals("", reminder.getName());
    }

    @Test
    public void afterTextChanged_handlesWhitespaceOnly() {
        when(mockEditable.toString()).thenReturn("   ");

        listener.afterTextChanged(mockEditable);

        assertEquals("", reminder.getName());
    }

    @Test
    public void afterTextChanged_nullEditable_setsNullName() {
        listener.afterTextChanged(null);

        assertNull(reminder.getName());
    }

    @Test
    public void afterTextChanged_specialCharacters_preserved() {
        when(mockEditable.toString()).thenReturn("Name with @#$% chars!");

        listener.afterTextChanged(mockEditable);

        assertEquals("Name with @#$% chars!", reminder.getName());
    }

    @Test
    public void afterTextChanged_unicode_preserved() {
        when(mockEditable.toString()).thenReturn("Name with emoji");

        listener.afterTextChanged(mockEditable);

        assertEquals("Name with emoji", reminder.getName());
    }

    @Test
    public void afterTextChanged_newlines_trimmed() {
        when(mockEditable.toString()).thenReturn("\n\nName\n\n");

        listener.afterTextChanged(mockEditable);

        assertEquals("Name", reminder.getName());
    }

    @Test
    public void afterTextChanged_tabs_trimmed() {
        when(mockEditable.toString()).thenReturn("\t\tName\t\t");

        listener.afterTextChanged(mockEditable);

        assertEquals("Name", reminder.getName());
    }

    @Test
    public void afterTextChanged_multipleConsecutiveCalls_updatesCorrectly() {
        when(mockEditable.toString()).thenReturn("First");
        listener.afterTextChanged(mockEditable);
        assertEquals("First", reminder.getName());

        when(mockEditable.toString()).thenReturn("Second");
        listener.afterTextChanged(mockEditable);
        assertEquals("Second", reminder.getName());

        when(mockEditable.toString()).thenReturn("Third");
        listener.afterTextChanged(mockEditable);
        assertEquals("Third", reminder.getName());
    }

    // ==================== beforeTextChanged Tests ====================

    @Test
    public void beforeTextChanged_doesNothing() {
        reminder.setName("Original");

        // Should not throw or change anything
        listener.beforeTextChanged("test", 0, 1, 1);

        assertEquals("Original", reminder.getName());
    }

    // ==================== onTextChanged Tests ====================

    @Test
    public void onTextChanged_doesNothing() {
        reminder.setName("Original");

        // Should not throw or change anything
        listener.onTextChanged("test", 0, 1, 1);

        assertEquals("Original", reminder.getName());
    }

    // ==================== Multiple Reminders ====================

    @Test
    public void listener_boundToSpecificReminder() {
        ReminderModel reminder1 = new ReminderModel();
        ReminderModel reminder2 = new ReminderModel();

        ReminderNameChangedListener listener1 = new ReminderNameChangedListener(reminder1);
        ReminderNameChangedListener listener2 = new ReminderNameChangedListener(reminder2);

        when(mockEditable.toString()).thenReturn("Name 1");
        listener1.afterTextChanged(mockEditable);

        when(mockEditable.toString()).thenReturn("Name 2");
        listener2.afterTextChanged(mockEditable);

        assertEquals("Name 1", reminder1.getName());
        assertEquals("Name 2", reminder2.getName());
    }
}
