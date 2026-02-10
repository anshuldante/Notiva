package com.ava.notiva;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ava.notiva.data.ReminderDao;
import com.ava.notiva.data.RemindersDb;
import com.ava.notiva.model.RecurrenceType;
import com.ava.notiva.model.ReminderModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Instrumented tests for {@link ReminderDao}.
 * Uses Room's in-memory database for isolated, fast testing.
 */
@RunWith(AndroidJUnit4.class)
public class ReminderDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private RemindersDb database;
    private ReminderDao reminderDao;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, RemindersDb.class)
                .allowMainThreadQueries()
                .build();
        reminderDao = database.reminderDao();
    }

    @After
    public void tearDown() {
        database.close();
    }

    // ==================== Helper Methods ====================

    private ReminderModel createTestReminder(String name) {
        ReminderModel reminder = new ReminderModel();
        reminder.setName(name);
        reminder.setActive(true);
        reminder.setRecurrenceType(RecurrenceType.DAY);
        reminder.setRecurrenceDelay(1);

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.MONTH, 1);

        reminder.setStartDateTime(start);
        reminder.setEndDateTime(end);

        return reminder;
    }

    private <T> T getLiveDataValue(LiveData<T> liveData) throws InterruptedException {
        final Object[] data = new Object[1];
        final CountDownLatch latch = new CountDownLatch(1);

        liveData.observeForever(value -> {
            data[0] = value;
            latch.countDown();
        });

        latch.await(2, TimeUnit.SECONDS);
        @SuppressWarnings("unchecked")
        T result = (T) data[0];
        return result;
    }

    // ==================== Insert Tests ====================

    @Test
    public void add_insertsReminderAndReturnsId() {
        ReminderModel reminder = createTestReminder("Test Reminder");

        long id = reminderDao.add(reminder);

        assertTrue("Should return positive ID", id > 0);
    }

    @Test
    public void add_multipleReminders_returnsUniqueIds() {
        ReminderModel reminder1 = createTestReminder("Reminder 1");
        ReminderModel reminder2 = createTestReminder("Reminder 2");
        ReminderModel reminder3 = createTestReminder("Reminder 3");

        long id1 = reminderDao.add(reminder1);
        long id2 = reminderDao.add(reminder2);
        long id3 = reminderDao.add(reminder3);

        assertTrue("IDs should be unique", id1 != id2 && id2 != id3 && id1 != id3);
    }

    @Test
    public void add_autoGeneratesIncrementingIds() {
        ReminderModel reminder1 = createTestReminder("Reminder 1");
        ReminderModel reminder2 = createTestReminder("Reminder 2");

        long id1 = reminderDao.add(reminder1);
        long id2 = reminderDao.add(reminder2);

        assertEquals("Second ID should be one more than first", id1 + 1, id2);
    }

    // ==================== Query Tests ====================

    @Test
    public void getAll_emptyDatabase_returnsEmptyList() throws InterruptedException {
        LiveData<List<ReminderModel>> liveData = reminderDao.getAll();
        List<ReminderModel> result = getLiveDataValue(liveData);

        assertNotNull(result);
        assertTrue("Empty database should return empty list", result.isEmpty());
    }

    @Test
    public void getAll_withReminders_returnsAllReminders() throws InterruptedException {
        reminderDao.add(createTestReminder("Reminder 1"));
        reminderDao.add(createTestReminder("Reminder 2"));
        reminderDao.add(createTestReminder("Reminder 3"));

        LiveData<List<ReminderModel>> liveData = reminderDao.getAll();
        List<ReminderModel> result = getLiveDataValue(liveData);

        assertNotNull(result);
        assertEquals("Should return all 3 reminders", 3, result.size());
    }

    @Test
    public void getAll_orderedByName() throws InterruptedException {
        reminderDao.add(createTestReminder("Charlie"));
        reminderDao.add(createTestReminder("Alpha"));
        reminderDao.add(createTestReminder("Bravo"));

        LiveData<List<ReminderModel>> liveData = reminderDao.getAll();
        List<ReminderModel> result = getLiveDataValue(liveData);

        assertEquals("Alpha", result.get(0).getName());
        assertEquals("Bravo", result.get(1).getName());
        assertEquals("Charlie", result.get(2).getName());
    }

    @Test
    public void getAllSync_returnsAllReminders() {
        reminderDao.add(createTestReminder("Reminder 1"));
        reminderDao.add(createTestReminder("Reminder 2"));

        List<ReminderModel> result = reminderDao.getAllSync();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void getAllSync_orderedByName() {
        reminderDao.add(createTestReminder("Zebra"));
        reminderDao.add(createTestReminder("Apple"));

        List<ReminderModel> result = reminderDao.getAllSync();

        assertEquals("Apple", result.get(0).getName());
        assertEquals("Zebra", result.get(1).getName());
    }

    @Test
    public void get_existingId_returnsReminder() throws InterruptedException {
        ReminderModel reminder = createTestReminder("Test");
        long id = reminderDao.add(reminder);

        LiveData<ReminderModel> liveData = reminderDao.get((int) id);
        ReminderModel result = getLiveDataValue(liveData);

        assertNotNull(result);
        assertEquals("Test", result.getName());
        assertEquals(id, result.getId());
    }

    @Test
    public void get_nonExistingId_returnsNull() throws InterruptedException {
        LiveData<ReminderModel> liveData = reminderDao.get(999);
        ReminderModel result = getLiveDataValue(liveData);

        assertNull("Non-existing ID should return null", result);
    }

    // ==================== Update Tests ====================

    @Test
    public void update_modifiesExistingReminder() throws InterruptedException {
        ReminderModel reminder = createTestReminder("Original Name");
        long id = reminderDao.add(reminder);

        reminder.setId((int) id);
        reminder.setName("Updated Name");
        reminder.setRecurrenceDelay(5);
        reminderDao.update(reminder);

        LiveData<ReminderModel> liveData = reminderDao.get((int) id);
        ReminderModel result = getLiveDataValue(liveData);

        assertEquals("Updated Name", result.getName());
        assertEquals(5, result.getRecurrenceDelay());
    }

    @Test
    public void updateStatus_changesActiveState() throws InterruptedException {
        ReminderModel reminder = createTestReminder("Test");
        reminder.setActive(true);
        long id = reminderDao.add(reminder);

        reminderDao.updateStatus((int) id, false);

        LiveData<ReminderModel> liveData = reminderDao.get((int) id);
        ReminderModel result = getLiveDataValue(liveData);

        assertFalse("Status should be updated to inactive", result.isActive());
    }

    @Test
    public void updateStatus_togglesBackToActive() throws InterruptedException {
        ReminderModel reminder = createTestReminder("Test");
        reminder.setActive(false);
        long id = reminderDao.add(reminder);

        reminderDao.updateStatus((int) id, true);

        LiveData<ReminderModel> liveData = reminderDao.get((int) id);
        ReminderModel result = getLiveDataValue(liveData);

        assertTrue("Status should be updated to active", result.isActive());
    }

    // ==================== Delete Tests ====================

    @Test
    public void delete_removesReminder() throws InterruptedException {
        ReminderModel reminder = createTestReminder("To Delete");
        long id = reminderDao.add(reminder);

        reminder.setId((int) id);
        reminderDao.delete(reminder);

        LiveData<ReminderModel> liveData = reminderDao.get((int) id);
        ReminderModel result = getLiveDataValue(liveData);

        assertNull("Deleted reminder should not exist", result);
    }

    @Test
    public void delete_onlyRemovesSpecifiedReminder() throws InterruptedException {
        ReminderModel reminder1 = createTestReminder("Keep");
        ReminderModel reminder2 = createTestReminder("Delete");

        long id1 = reminderDao.add(reminder1);
        long id2 = reminderDao.add(reminder2);

        reminder2.setId((int) id2);
        reminderDao.delete(reminder2);

        LiveData<List<ReminderModel>> liveData = reminderDao.getAll();
        List<ReminderModel> result = getLiveDataValue(liveData);

        assertEquals(1, result.size());
        assertEquals("Keep", result.get(0).getName());
    }

    @Test
    public void deleteAll_removesAllReminders() throws InterruptedException {
        reminderDao.add(createTestReminder("Reminder 1"));
        reminderDao.add(createTestReminder("Reminder 2"));
        reminderDao.add(createTestReminder("Reminder 3"));

        reminderDao.deleteAll();

        LiveData<List<ReminderModel>> liveData = reminderDao.getAll();
        List<ReminderModel> result = getLiveDataValue(liveData);

        assertTrue("All reminders should be deleted", result.isEmpty());
    }

    @Test
    public void deleteAll_onEmptyDatabase_doesNotThrow() {
        // Should not throw exception
        reminderDao.deleteAll();

        List<ReminderModel> result = reminderDao.getAllSync();
        assertTrue(result.isEmpty());
    }

    // ==================== Data Integrity Tests ====================

    @Test
    public void add_preservesAllFields() throws InterruptedException {
        Calendar startTime = Calendar.getInstance();
        startTime.set(2024, Calendar.JUNE, 15, 10, 30, 0);
        startTime.set(Calendar.MILLISECOND, 0);

        Calendar endTime = Calendar.getInstance();
        endTime.set(2024, Calendar.DECEMBER, 31, 23, 59, 59);
        endTime.set(Calendar.MILLISECOND, 0);

        ReminderModel reminder = new ReminderModel();
        reminder.setName("Full Test");
        reminder.setActive(false);
        reminder.setRecurrenceType(RecurrenceType.HOUR);
        reminder.setRecurrenceDelay(3);
        reminder.setStartDateTime(startTime);
        reminder.setEndDateTime(endTime);

        long id = reminderDao.add(reminder);

        LiveData<ReminderModel> liveData = reminderDao.get((int) id);
        ReminderModel result = getLiveDataValue(liveData);

        assertEquals("Full Test", result.getName());
        assertFalse(result.isActive());
        assertEquals(RecurrenceType.HOUR, result.getRecurrenceType());
        assertEquals(3, result.getRecurrenceDelay());
        assertEquals(startTime.getTimeInMillis(), result.getStartDateTime().getTimeInMillis());
        assertEquals(endTime.getTimeInMillis(), result.getEndDateTime().getTimeInMillis());
    }

    @Test
    public void add_handlesAllRecurrenceTypes() {
        for (RecurrenceType type : RecurrenceType.values()) {
            ReminderModel reminder = createTestReminder("Type " + type.name());
            reminder.setRecurrenceType(type);

            long id = reminderDao.add(reminder);
            assertTrue("Should insert " + type.name(), id > 0);
        }

        List<ReminderModel> all = reminderDao.getAllSync();
        assertEquals(RecurrenceType.values().length, all.size());
    }

    @Test
    public void add_handlesSpecialCharactersInName() throws InterruptedException {
        ReminderModel reminder = createTestReminder("Test with 'quotes' and \"double quotes\" & special <chars>");
        long id = reminderDao.add(reminder);

        LiveData<ReminderModel> liveData = reminderDao.get((int) id);
        ReminderModel result = getLiveDataValue(liveData);

        assertEquals("Test with 'quotes' and \"double quotes\" & special <chars>", result.getName());
    }

    @Test
    public void add_handlesEmptyName() throws InterruptedException {
        ReminderModel reminder = createTestReminder("");
        long id = reminderDao.add(reminder);

        LiveData<ReminderModel> liveData = reminderDao.get((int) id);
        ReminderModel result = getLiveDataValue(liveData);

        assertEquals("", result.getName());
    }

    @Test
    public void add_handlesNullName() throws InterruptedException {
        ReminderModel reminder = new ReminderModel();
        reminder.setName(null);
        reminder.setActive(true);
        reminder.setRecurrenceType(RecurrenceType.DAY);
        reminder.setRecurrenceDelay(1);
        reminder.setStartDateTime(Calendar.getInstance());
        reminder.setEndDateTime(Calendar.getInstance());

        long id = reminderDao.add(reminder);

        LiveData<ReminderModel> liveData = reminderDao.get((int) id);
        ReminderModel result = getLiveDataValue(liveData);

        assertNull(result.getName());
    }

    // ==================== Edge Case Tests ====================

    @Test
    public void add_withZeroRecurrenceDelay() throws InterruptedException {
        ReminderModel reminder = createTestReminder("Zero Delay");
        reminder.setRecurrenceDelay(0);

        long id = reminderDao.add(reminder);

        LiveData<ReminderModel> liveData = reminderDao.get((int) id);
        ReminderModel result = getLiveDataValue(liveData);

        assertEquals(0, result.getRecurrenceDelay());
    }

    @Test
    public void add_withLargeRecurrenceDelay() throws InterruptedException {
        ReminderModel reminder = createTestReminder("Large Delay");
        reminder.setRecurrenceDelay(Integer.MAX_VALUE);

        long id = reminderDao.add(reminder);

        LiveData<ReminderModel> liveData = reminderDao.get((int) id);
        ReminderModel result = getLiveDataValue(liveData);

        assertEquals(Integer.MAX_VALUE, result.getRecurrenceDelay());
    }

    // ==================== Tracking Field Tests (Phase 11) ====================

    @Test
    public void updateLastFiredAt_setsTimestamp() throws InterruptedException {
        ReminderModel reminder = createTestReminder("Fire Test");
        long id = reminderDao.add(reminder);

        long timestamp = System.currentTimeMillis();
        reminderDao.updateLastFiredAt((int) id, timestamp);

        LiveData<ReminderModel> liveData = reminderDao.get((int) id);
        ReminderModel result = getLiveDataValue(liveData);

        assertNotNull(result);
        assertEquals(Long.valueOf(timestamp), result.getLastFiredAt());
    }

    @Test
    public void updateLastFiredAt_withNull_clearsTimestamp() throws InterruptedException {
        ReminderModel reminder = createTestReminder("Fire Clear Test");
        long id = reminderDao.add(reminder);

        reminderDao.updateLastFiredAt((int) id, System.currentTimeMillis());
        reminderDao.updateLastFiredAt((int) id, null);

        LiveData<ReminderModel> liveData = reminderDao.get((int) id);
        ReminderModel result = getLiveDataValue(liveData);

        assertNotNull(result);
        assertNull("lastFiredAt should be cleared", result.getLastFiredAt());
    }

    @Test
    public void updateLastAcknowledgedAt_setsTimestamp() throws InterruptedException {
        ReminderModel reminder = createTestReminder("Ack Test");
        long id = reminderDao.add(reminder);

        long timestamp = System.currentTimeMillis();
        reminderDao.updateLastAcknowledgedAt((int) id, timestamp);

        LiveData<ReminderModel> liveData = reminderDao.get((int) id);
        ReminderModel result = getLiveDataValue(liveData);

        assertNotNull(result);
        assertEquals(Long.valueOf(timestamp), result.getLastAcknowledgedAt());
    }

    @Test
    public void updateLastAcknowledgedAt_withNull_clearsTimestamp() throws InterruptedException {
        ReminderModel reminder = createTestReminder("Ack Clear Test");
        long id = reminderDao.add(reminder);

        reminderDao.updateLastAcknowledgedAt((int) id, System.currentTimeMillis());
        reminderDao.updateLastAcknowledgedAt((int) id, null);

        LiveData<ReminderModel> liveData = reminderDao.get((int) id);
        ReminderModel result = getLiveDataValue(liveData);

        assertNotNull(result);
        assertNull("lastAcknowledgedAt should be cleared", result.getLastAcknowledgedAt());
    }

    @Test
    public void add_preservesNewTrackingFields() throws InterruptedException {
        ReminderModel reminder = createTestReminder("Tracking Fields");
        reminder.setLastFiredAt(111111L);
        reminder.setLastAcknowledgedAt(222222L);
        reminder.setRingtoneUri("content://media/audio/ringtone/42");

        long id = reminderDao.add(reminder);

        LiveData<ReminderModel> liveData = reminderDao.get((int) id);
        ReminderModel result = getLiveDataValue(liveData);

        assertNotNull(result);
        assertEquals(Long.valueOf(111111L), result.getLastFiredAt());
        assertEquals(Long.valueOf(222222L), result.getLastAcknowledgedAt());
        assertEquals("content://media/audio/ringtone/42", result.getRingtoneUri());
    }

    @Test
    public void updateLastFiredAt_doesNotAffectLastAcknowledgedAt() throws InterruptedException {
        ReminderModel reminder = createTestReminder("Independence Test");
        long id = reminderDao.add(reminder);

        long firedTimestamp = 100000L;
        long ackTimestamp = 200000L;
        reminderDao.updateLastAcknowledgedAt((int) id, ackTimestamp);
        reminderDao.updateLastFiredAt((int) id, firedTimestamp);

        LiveData<ReminderModel> liveData = reminderDao.get((int) id);
        ReminderModel result = getLiveDataValue(liveData);

        assertNotNull(result);
        assertEquals("lastFiredAt should be set independently",
                Long.valueOf(firedTimestamp), result.getLastFiredAt());
        assertEquals("lastAcknowledgedAt should be unaffected",
                Long.valueOf(ackTimestamp), result.getLastAcknowledgedAt());
    }

    // ==================== Edge Case Tests (continued) ====================

    @Test
    public void update_nonExistingReminder_doesNotThrow() {
        ReminderModel reminder = createTestReminder("Ghost");
        reminder.setId(99999);

        // Should not throw
        reminderDao.update(reminder);

        // Verify it wasn't inserted
        List<ReminderModel> all = reminderDao.getAllSync();
        assertTrue(all.isEmpty());
    }
}
