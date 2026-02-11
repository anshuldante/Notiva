package com.ava.notiva;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ava.notiva.data.ReminderDao;
import com.ava.notiva.data.ReminderRepository;
import com.ava.notiva.model.ReminderModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for {@link ReminderRepository}.
 * Tests delegation to DAO for both async and sync methods.
 */
public class ReminderRepositoryTest {

    @Mock
    private ReminderDao mockDao;

    private ExecutorService realExecutor;
    private ReminderRepository repository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockDao.getAll()).thenReturn(new MutableLiveData<>(Collections.emptyList()));
        realExecutor = Executors.newSingleThreadExecutor();
        repository = new ReminderRepository(mockDao, realExecutor);
    }

    @After
    public void tearDown() {
        realExecutor.shutdownNow();
    }

    // ==================== Constructor ====================

    @Test
    public void constructor_callsDaoGetAll() {
        verify(mockDao).getAll();
    }

    @Test
    public void getAll_returnsLiveDataFromDao() {
        MutableLiveData<List<ReminderModel>> liveData = new MutableLiveData<>();
        when(mockDao.getAll()).thenReturn(liveData);

        ReminderRepository repo = new ReminderRepository(mockDao, realExecutor);

        assertSame(liveData, repo.getAll());
    }

    // ==================== addWithCallback ====================

    @Test
    public void addWithCallback_delegatesToDao() throws InterruptedException {
        ReminderModel reminder = new ReminderModel("Test");
        when(mockDao.add(reminder)).thenReturn(42L);
        CountDownLatch latch = new CountDownLatch(1);

        repository.addWithCallback(reminder, id -> latch.countDown());

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(mockDao).add(reminder);
    }

    @Test
    public void addWithCallback_passesReturnedIdToCallback() throws InterruptedException {
        ReminderModel reminder = new ReminderModel("Test");
        when(mockDao.add(reminder)).thenReturn(99L);
        long[] capturedId = {-1};
        CountDownLatch latch = new CountDownLatch(1);

        repository.addWithCallback(reminder, id -> {
            capturedId[0] = id;
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(99L, capturedId[0]);
    }

    @Test
    public void addWithCallback_nullCallback_doesNotCrash() throws InterruptedException {
        ReminderModel reminder = new ReminderModel("Test");
        when(mockDao.add(reminder)).thenReturn(1L);
        CountDownLatch latch = new CountDownLatch(1);

        repository.addWithCallback(reminder, null);

        // Submit a follow-up task to ensure the add completed
        realExecutor.submit(latch::countDown);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(mockDao, atLeastOnce()).add(reminder);
    }

    @Test
    public void addWithCallback_daoThrows_callbackReceivesMinusOne() throws InterruptedException {
        ReminderModel reminder = new ReminderModel("Test");
        when(mockDao.add(reminder)).thenThrow(new RuntimeException("DB error"));
        long[] capturedId = {0};
        CountDownLatch latch = new CountDownLatch(1);

        repository.addWithCallback(reminder, id -> {
            capturedId[0] = id;
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals("Should pass -1 on error", -1L, capturedId[0]);
    }

    // ==================== delete ====================

    @Test
    public void delete_delegatesToDao() throws InterruptedException {
        ReminderModel reminder = new ReminderModel("To Delete");
        CountDownLatch latch = new CountDownLatch(1);

        repository.delete(reminder);

        realExecutor.submit(latch::countDown);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(mockDao).delete(reminder);
    }

    // ==================== deleteAll ====================

    @Test
    public void deleteAll_delegatesToDao() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        repository.deleteAll();

        realExecutor.submit(latch::countDown);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(mockDao).deleteAll();
    }

    // ==================== update ====================

    @Test
    public void update_delegatesToDao() throws InterruptedException {
        ReminderModel reminder = new ReminderModel("Updated");
        CountDownLatch latch = new CountDownLatch(1);

        repository.update(reminder);

        realExecutor.submit(latch::countDown);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(mockDao).update(reminder);
    }

    // ==================== updateStatus ====================

    @Test
    public void updateStatus_delegatesToDao() throws InterruptedException {
        ReminderModel reminder = new ReminderModel("Status Test");
        reminder.setId(5);
        CountDownLatch latch = new CountDownLatch(1);

        repository.updateStatus(reminder, false);

        realExecutor.submit(latch::countDown);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(mockDao).updateStatus(5, false);
    }

    // ==================== Sync Methods ====================

    @Test
    public void getAllSync_delegatesToDao() {
        List<ReminderModel> expected = Arrays.asList(new ReminderModel("A"), new ReminderModel("B"));
        when(mockDao.getAllSync()).thenReturn(expected);

        List<ReminderModel> result = repository.getAllSync();

        assertSame(expected, result);
        verify(mockDao).getAllSync();
    }

    @Test
    public void updateSnoozedUntilSync_delegatesToDao() {
        repository.updateSnoozedUntilSync(42, 999L);

        verify(mockDao).updateSnoozedUntil(42, 999L);
    }

    @Test
    public void updateLastFiredAtSync_delegatesToDao() {
        repository.updateLastFiredAtSync(7, 123456L);

        verify(mockDao).updateLastFiredAt(7, 123456L);
    }

    @Test
    public void updateLastAcknowledgedAtSync_delegatesToDao() {
        repository.updateLastAcknowledgedAtSync(3, 789012L);

        verify(mockDao).updateLastAcknowledgedAt(3, 789012L);
    }

    // ==================== Async Tracking Methods ====================

    @Test
    public void updateSnoozedUntil_delegatesToDao() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        repository.updateSnoozedUntil(10, 5000L);

        realExecutor.submit(latch::countDown);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(mockDao).updateSnoozedUntil(10, 5000L);
    }

    @Test
    public void updateLastFiredAt_delegatesToDao() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        repository.updateLastFiredAt(10, 5000L);

        realExecutor.submit(latch::countDown);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(mockDao).updateLastFiredAt(10, 5000L);
    }

    @Test
    public void updateLastAcknowledgedAt_delegatesToDao() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        repository.updateLastAcknowledgedAt(10, 5000L);

        realExecutor.submit(latch::countDown);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(mockDao).updateLastAcknowledgedAt(10, 5000L);
    }

    @Test
    public void updateSnoozedUntil_withNull_delegatesToDao() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        repository.updateSnoozedUntil(10, null);

        realExecutor.submit(latch::countDown);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(mockDao).updateSnoozedUntil(10, null);
    }

    @Test
    public void updateSnoozedUntilSync_withNull_delegatesToDao() {
        repository.updateSnoozedUntilSync(10, null);

        verify(mockDao).updateSnoozedUntil(10, null);
    }
}
