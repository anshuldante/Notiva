package com.ava.notiva;

import static org.junit.Assert.*;

import com.ava.notiva.adapter.ReminderDiffCallback;
import com.ava.notiva.model.RecurrenceType;
import com.ava.notiva.model.ReminderModel;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

/**
 * Unit tests for {@link ReminderDiffCallback}.
 * Tests DiffUtil callback for efficient RecyclerView updates.
 */
public class ReminderDiffCallbackTest {

    private ReminderDiffCallback diffCallback;

    @Before
    public void setUp() {
        diffCallback = new ReminderDiffCallback();
    }

    // ==================== areItemsTheSame Tests ====================

    @Test
    public void areItemsTheSame_sameId_returnsTrue() {
        ReminderModel oldItem = createReminder(1, "Test");
        ReminderModel newItem = createReminder(1, "Different Name");

        assertTrue(diffCallback.areItemsTheSame(oldItem, newItem));
    }

    @Test
    public void areItemsTheSame_differentId_returnsFalse() {
        ReminderModel oldItem = createReminder(1, "Test");
        ReminderModel newItem = createReminder(2, "Test");

        assertFalse(diffCallback.areItemsTheSame(oldItem, newItem));
    }

    @Test
    public void areItemsTheSame_bothIdZero_returnsTrue() {
        ReminderModel oldItem = createReminder(0, "Test1");
        ReminderModel newItem = createReminder(0, "Test2");

        assertTrue(diffCallback.areItemsTheSame(oldItem, newItem));
    }

    @Test
    public void areItemsTheSame_negativeIds_comparesCorrectly() {
        ReminderModel oldItem = createReminder(-1, "Test");
        ReminderModel newItem = createReminder(-1, "Test");

        assertTrue(diffCallback.areItemsTheSame(oldItem, newItem));
    }

    // ==================== areContentsTheSame Tests ====================

    @Test
    public void areContentsTheSame_identicalReminders_returnsTrue() {
        Calendar time = Calendar.getInstance();

        ReminderModel oldItem = new ReminderModel();
        oldItem.setId(1);
        oldItem.setName("Test");
        oldItem.setActive(true);
        oldItem.setRecurrenceType(RecurrenceType.DAY);
        oldItem.setRecurrenceDelay(1);
        oldItem.setStartDateTime(time);
        oldItem.setEndDateTime(time);

        ReminderModel newItem = new ReminderModel();
        newItem.setId(1);
        newItem.setName("Test");
        newItem.setActive(true);
        newItem.setRecurrenceType(RecurrenceType.DAY);
        newItem.setRecurrenceDelay(1);
        newItem.setStartDateTime(time);
        newItem.setEndDateTime(time);

        assertTrue(diffCallback.areContentsTheSame(oldItem, newItem));
    }

    @Test
    public void areContentsTheSame_differentName_returnsFalse() {
        ReminderModel oldItem = createReminder(1, "Old Name");
        ReminderModel newItem = createReminder(1, "New Name");

        assertFalse(diffCallback.areContentsTheSame(oldItem, newItem));
    }

    @Test
    public void areContentsTheSame_differentActiveStatus_returnsFalse() {
        ReminderModel oldItem = createReminder(1, "Test");
        oldItem.setActive(true);

        ReminderModel newItem = createReminder(1, "Test");
        newItem.setActive(false);

        assertFalse(diffCallback.areContentsTheSame(oldItem, newItem));
    }

    @Test
    public void areContentsTheSame_differentRecurrenceType_returnsFalse() {
        ReminderModel oldItem = createReminder(1, "Test");
        oldItem.setRecurrenceType(RecurrenceType.DAY);

        ReminderModel newItem = createReminder(1, "Test");
        newItem.setRecurrenceType(RecurrenceType.HOUR);

        assertFalse(diffCallback.areContentsTheSame(oldItem, newItem));
    }

    @Test
    public void areContentsTheSame_differentRecurrenceDelay_returnsFalse() {
        ReminderModel oldItem = createReminder(1, "Test");
        oldItem.setRecurrenceDelay(1);

        ReminderModel newItem = createReminder(1, "Test");
        newItem.setRecurrenceDelay(5);

        assertFalse(diffCallback.areContentsTheSame(oldItem, newItem));
    }

    @Test
    public void areContentsTheSame_differentStartTime_returnsFalse() {
        Calendar time1 = Calendar.getInstance();
        Calendar time2 = Calendar.getInstance();
        time2.add(Calendar.HOUR, 1);

        ReminderModel oldItem = createReminder(1, "Test");
        oldItem.setStartDateTime(time1);

        ReminderModel newItem = createReminder(1, "Test");
        newItem.setStartDateTime(time2);

        assertFalse(diffCallback.areContentsTheSame(oldItem, newItem));
    }

    @Test
    public void areContentsTheSame_differentEndTime_returnsFalse() {
        Calendar time1 = Calendar.getInstance();
        Calendar time2 = Calendar.getInstance();
        time2.add(Calendar.DAY_OF_YEAR, 1);

        ReminderModel oldItem = createReminder(1, "Test");
        oldItem.setEndDateTime(time1);

        ReminderModel newItem = createReminder(1, "Test");
        newItem.setEndDateTime(time2);

        assertFalse(diffCallback.areContentsTheSame(oldItem, newItem));
    }

    // ==================== Combined Tests ====================

    @Test
    public void sameIdDifferentContents_itemsSameButContentsDifferent() {
        ReminderModel oldItem = createReminder(1, "Old");
        oldItem.setActive(true);

        ReminderModel newItem = createReminder(1, "New");
        newItem.setActive(false);

        assertTrue("Same ID means same item", diffCallback.areItemsTheSame(oldItem, newItem));
        assertFalse("Different contents should be detected", diffCallback.areContentsTheSame(oldItem, newItem));
    }

    @Test
    public void differentIdSameContents_itemsDifferentButContentsWouldBeSame() {
        Calendar time = Calendar.getInstance();

        ReminderModel oldItem = new ReminderModel();
        oldItem.setId(1);
        oldItem.setName("Test");
        oldItem.setActive(true);
        oldItem.setStartDateTime(time);
        oldItem.setEndDateTime(time);

        ReminderModel newItem = new ReminderModel();
        newItem.setId(2); // Different ID
        newItem.setName("Test");
        newItem.setActive(true);
        newItem.setStartDateTime(time);
        newItem.setEndDateTime(time);

        assertFalse("Different ID means different item", diffCallback.areItemsTheSame(oldItem, newItem));
        // Note: areContentsTheSame includes ID in equals(), so this would be false too
        assertFalse("Different ID also means different contents via equals()",
                    diffCallback.areContentsTheSame(oldItem, newItem));
    }

    // ==================== Edge Cases ====================

    @Test
    public void areContentsTheSame_nullNames_handlesCorrectly() {
        ReminderModel oldItem = createReminder(1, null);
        ReminderModel newItem = createReminder(1, null);

        // Both null names should be equal
        assertTrue(diffCallback.areContentsTheSame(oldItem, newItem));
    }

    @Test
    public void areContentsTheSame_oneNullName_returnsFalse() {
        ReminderModel oldItem = createReminder(1, "Test");
        ReminderModel newItem = createReminder(1, null);

        assertFalse(diffCallback.areContentsTheSame(oldItem, newItem));
    }

    @Test
    public void areContentsTheSame_emptyVsNullName_returnsFalse() {
        ReminderModel oldItem = createReminder(1, "");
        ReminderModel newItem = createReminder(1, null);

        assertFalse(diffCallback.areContentsTheSame(oldItem, newItem));
    }

    // ==================== Helper Methods ====================

    private ReminderModel createReminder(int id, String name) {
        ReminderModel reminder = new ReminderModel();
        reminder.setId(id);
        reminder.setName(name);
        reminder.setActive(true);
        reminder.setRecurrenceType(RecurrenceType.DAY);
        reminder.setRecurrenceDelay(1);
        Calendar now = Calendar.getInstance();
        reminder.setStartDateTime(now);
        reminder.setEndDateTime(now);
        return reminder;
    }
}
