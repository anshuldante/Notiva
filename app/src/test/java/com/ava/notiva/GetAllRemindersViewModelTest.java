package com.ava.notiva;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ava.notiva.data.GetAllRemindersViewModel;
import com.ava.notiva.data.ReminderRepository;
import com.ava.notiva.model.ReminderModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link GetAllRemindersViewModel}.
 * Tests that the ViewModel correctly exposes LiveData from the repository.
 */
public class GetAllRemindersViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private ReminderRepository mockRepository;

    private GetAllRemindersViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ==================== Constructor Tests ====================

    @Test
    public void constructor_callsRepositoryGetAll() {
        MutableLiveData<List<ReminderModel>> liveData = new MutableLiveData<>();
        when(mockRepository.getAll()).thenReturn(liveData);

        viewModel = new GetAllRemindersViewModel(mockRepository);

        verify(mockRepository).getAll();
    }

    @Test
    public void constructor_cachesLiveDataFromRepository() {
        MutableLiveData<List<ReminderModel>> liveData = new MutableLiveData<>();
        when(mockRepository.getAll()).thenReturn(liveData);

        viewModel = new GetAllRemindersViewModel(mockRepository);
        LiveData<List<ReminderModel>> result1 = viewModel.getAllReminders();
        LiveData<List<ReminderModel>> result2 = viewModel.getAllReminders();

        // Should return the same cached instance
        assertSame(result1, result2);
        // Repository should only be called once (in constructor)
        verify(mockRepository, times(1)).getAll();
    }

    // ==================== getAllReminders Tests ====================

    @Test
    public void getAllReminders_returnsLiveDataFromRepository() {
        MutableLiveData<List<ReminderModel>> expectedLiveData = new MutableLiveData<>();
        when(mockRepository.getAll()).thenReturn(expectedLiveData);

        viewModel = new GetAllRemindersViewModel(mockRepository);
        LiveData<List<ReminderModel>> result = viewModel.getAllReminders();

        assertSame(expectedLiveData, result);
    }

    @Test
    public void getAllReminders_exposesEmptyList() {
        MutableLiveData<List<ReminderModel>> liveData = new MutableLiveData<>();
        liveData.setValue(Collections.emptyList());
        when(mockRepository.getAll()).thenReturn(liveData);

        viewModel = new GetAllRemindersViewModel(mockRepository);
        List<ReminderModel> result = viewModel.getAllReminders().getValue();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getAllReminders_exposesPopulatedList() {
        ReminderModel reminder1 = new ReminderModel("Reminder 1");
        ReminderModel reminder2 = new ReminderModel("Reminder 2");
        List<ReminderModel> reminders = Arrays.asList(reminder1, reminder2);

        MutableLiveData<List<ReminderModel>> liveData = new MutableLiveData<>();
        liveData.setValue(reminders);
        when(mockRepository.getAll()).thenReturn(liveData);

        viewModel = new GetAllRemindersViewModel(mockRepository);
        List<ReminderModel> result = viewModel.getAllReminders().getValue();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Reminder 1", result.get(0).getName());
        assertEquals("Reminder 2", result.get(1).getName());
    }

    @Test
    public void getAllReminders_reflectsRepositoryUpdates() {
        ReminderModel reminder1 = new ReminderModel("Initial");

        MutableLiveData<List<ReminderModel>> liveData = new MutableLiveData<>();
        liveData.setValue(Collections.singletonList(reminder1));
        when(mockRepository.getAll()).thenReturn(liveData);

        viewModel = new GetAllRemindersViewModel(mockRepository);

        // Initial value
        assertEquals(1, viewModel.getAllReminders().getValue().size());

        // Simulate repository update
        ReminderModel reminder2 = new ReminderModel("New");
        liveData.setValue(Arrays.asList(reminder1, reminder2));

        // ViewModel should reflect the update
        assertEquals(2, viewModel.getAllReminders().getValue().size());
    }

    @Test
    public void getAllReminders_handlesNullValue() {
        MutableLiveData<List<ReminderModel>> liveData = new MutableLiveData<>();
        liveData.setValue(null);
        when(mockRepository.getAll()).thenReturn(liveData);

        viewModel = new GetAllRemindersViewModel(mockRepository);
        List<ReminderModel> result = viewModel.getAllReminders().getValue();

        assertNull(result);
    }
}
