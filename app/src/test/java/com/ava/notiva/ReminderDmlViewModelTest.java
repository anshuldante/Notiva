package com.ava.notiva;

import static org.mockito.Mockito.*;

import com.ava.notiva.data.ReminderDmlViewModel;
import com.ava.notiva.data.ReminderRepository;
import com.ava.notiva.model.RecurrenceType;
import com.ava.notiva.model.ReminderModel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Calendar;
import java.util.function.Consumer;

/**
 * Unit tests for {@link ReminderDmlViewModel}.
 * Tests that the ViewModel correctly delegates DML operations to the repository.
 */
public class ReminderDmlViewModelTest {

    @Mock
    private ReminderRepository mockRepository;

    @Mock
    private Consumer<Long> mockCallback;

    private ReminderDmlViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new ReminderDmlViewModel(mockRepository);
    }

    // ==================== updateReminder Tests ====================

    @Test
    public void updateReminder_delegatesToRepository() {
        ReminderModel reminder = createTestReminder(1, "Test");

        viewModel.updateReminder(reminder);

        verify(mockRepository).update(reminder);
    }

    @Test
    public void updateReminder_passesCorrectModel() {
        ReminderModel reminder = createTestReminder(42, "Specific Reminder");
        reminder.setRecurrenceType(RecurrenceType.HOUR);
        reminder.setRecurrenceDelay(5);

        viewModel.updateReminder(reminder);

        ArgumentCaptor<ReminderModel> captor = ArgumentCaptor.forClass(ReminderModel.class);
        verify(mockRepository).update(captor.capture());

        ReminderModel captured = captor.getValue();
        assertEquals(42, captured.getId());
        assertEquals("Specific Reminder", captured.getName());
        assertEquals(RecurrenceType.HOUR, captured.getRecurrenceType());
        assertEquals(5, captured.getRecurrenceDelay());
    }

    @Test
    public void updateReminder_multipleUpdates_callsRepositoryEachTime() {
        ReminderModel reminder1 = createTestReminder(1, "First");
        ReminderModel reminder2 = createTestReminder(2, "Second");

        viewModel.updateReminder(reminder1);
        viewModel.updateReminder(reminder2);

        verify(mockRepository, times(2)).update(any(ReminderModel.class));
        verify(mockRepository).update(reminder1);
        verify(mockRepository).update(reminder2);
    }

    // ==================== updateReminderStatus Tests ====================

    @Test
    public void updateReminderStatus_delegatesToRepository_setActive() {
        ReminderModel reminder = createTestReminder(1, "Test");

        viewModel.updateReminderStatus(reminder, true);

        verify(mockRepository).updateStatus(reminder, true);
    }

    @Test
    public void updateReminderStatus_delegatesToRepository_setInactive() {
        ReminderModel reminder = createTestReminder(1, "Test");

        viewModel.updateReminderStatus(reminder, false);

        verify(mockRepository).updateStatus(reminder, false);
    }

    @Test
    public void updateReminderStatus_passesCorrectParameters() {
        ReminderModel reminder = createTestReminder(99, "Status Test");

        viewModel.updateReminderStatus(reminder, false);

        verify(mockRepository).updateStatus(eq(reminder), eq(false));
    }

    // ==================== deleteReminder Tests ====================

    @Test
    public void deleteReminder_delegatesToRepository() {
        ReminderModel reminder = createTestReminder(1, "To Delete");

        viewModel.deleteReminder(reminder);

        verify(mockRepository).delete(reminder);
    }

    @Test
    public void deleteReminder_passesCorrectModel() {
        ReminderModel reminder = createTestReminder(77, "Delete Me");

        viewModel.deleteReminder(reminder);

        ArgumentCaptor<ReminderModel> captor = ArgumentCaptor.forClass(ReminderModel.class);
        verify(mockRepository).delete(captor.capture());

        assertEquals(77, captor.getValue().getId());
        assertEquals("Delete Me", captor.getValue().getName());
    }

    // ==================== deleteAllReminders Tests ====================

    @Test
    public void deleteAllReminders_delegatesToRepository() {
        viewModel.deleteAllReminders();

        verify(mockRepository).deleteAll();
    }

    @Test
    public void deleteAllReminders_calledMultipleTimes_delegatesEachTime() {
        viewModel.deleteAllReminders();
        viewModel.deleteAllReminders();

        verify(mockRepository, times(2)).deleteAll();
    }

    // ==================== addReminderWithCallback Tests ====================

    @Test
    public void addReminderWithCallback_delegatesToRepository() {
        ReminderModel reminder = createTestReminder(0, "New Reminder");

        viewModel.addReminderWithCallback(reminder, mockCallback);

        verify(mockRepository).addWithCallback(reminder, mockCallback);
    }

    @Test
    public void addReminderWithCallback_passesCorrectModelAndCallback() {
        ReminderModel reminder = createTestReminder(0, "Brand New");

        viewModel.addReminderWithCallback(reminder, mockCallback);

        verify(mockRepository).addWithCallback(eq(reminder), eq(mockCallback));
    }

    @Test
    public void addReminderWithCallback_withNullCallback_stillDelegates() {
        ReminderModel reminder = createTestReminder(0, "No Callback");

        viewModel.addReminderWithCallback(reminder, null);

        verify(mockRepository).addWithCallback(eq(reminder), isNull());
    }

    // ==================== Integration-style Tests ====================

    @Test
    public void multipleOperations_allDelegateCorrectly() {
        ReminderModel reminder1 = createTestReminder(0, "New");
        ReminderModel reminder2 = createTestReminder(1, "Existing");

        // Add
        viewModel.addReminderWithCallback(reminder1, mockCallback);

        // Update
        reminder2.setName("Updated");
        viewModel.updateReminder(reminder2);

        // Update status
        viewModel.updateReminderStatus(reminder2, false);

        // Delete
        viewModel.deleteReminder(reminder2);

        // Verify all operations
        verify(mockRepository).addWithCallback(reminder1, mockCallback);
        verify(mockRepository).update(reminder2);
        verify(mockRepository).updateStatus(reminder2, false);
        verify(mockRepository).delete(reminder2);
    }

    // ==================== Helper Methods ====================

    private ReminderModel createTestReminder(int id, String name) {
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

    private void assertEquals(int expected, int actual) {
        org.junit.Assert.assertEquals(expected, actual);
    }

    private void assertEquals(String expected, String actual) {
        org.junit.Assert.assertEquals(expected, actual);
    }

    private void assertEquals(Object expected, Object actual) {
        org.junit.Assert.assertEquals(expected, actual);
    }
}
