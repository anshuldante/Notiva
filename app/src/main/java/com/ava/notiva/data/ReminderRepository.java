package com.ava.notiva.data;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.ava.notiva.model.ReminderModel;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class ReminderRepository {

  private static final String TAG = "Notiva.ReminderRepository: ";
  private final ReminderDao reminderDao;
  private final ExecutorService reminderDaoExecutor;
  private final LiveData<List<ReminderModel>> getAllObservable;

  public ReminderRepository(ReminderDao reminderDao, ExecutorService reminderDaoExecutor) {
    this.reminderDao = reminderDao;
    this.reminderDaoExecutor = reminderDaoExecutor;
    this.getAllObservable = reminderDao.getAll();
  }

  public void addWithCallback(ReminderModel model, Consumer<Long> callback) {
    reminderDaoExecutor.submit(() -> {
      long id = -1;
      try {
        id = reminderDao.add(model);
        Log.i(TAG, "Added reminder (async): " + model + ", id: " + id);
      } catch (Exception e) {
        Log.e(TAG, "Error while adding the reminder (async): " + model, e);
      }
      if (callback != null) {
        callback.accept(id);
      }
    });
  }

  public void deleteAll() {
    reminderDaoExecutor.submit(
        () -> {
          try {
            reminderDao.deleteAll();
            Log.i(TAG, "Deleted All reminders!");
          } catch (Exception e) {
            Log.e(TAG, "Exception while deleting all reminders", e);
          }
        });
  }

  public void delete(ReminderModel reminder) {
    reminderDaoExecutor.submit(
        () -> {
          try {
            reminderDao.delete(reminder);
            Log.i(TAG, "Deleted reminder: " + Optional.ofNullable(reminder.getName()).orElse(""));
          } catch (Exception e) {
            Log.e(TAG, "Exception while deleting reminder: " + reminder.getName(), e);
          }
        });
  }

  public void updateStatus(ReminderModel reminder, boolean isActive) {
    reminderDaoExecutor.submit(
        () -> {
          try {
            reminderDao.updateStatus(reminder.getId(), isActive);
            Log.i(TAG, "Updated reminder: " + reminder.getName() + "'s status to: " + isActive);
          } catch (Exception e) {
            Log.e(
                TAG,
                "Exception while Updating reminder: "
                    + reminder.getName()
                    + "'s status to: "
                    + isActive);
          }
        });
  }

  public void update(ReminderModel model) {
    reminderDaoExecutor.submit(
        () -> {
          try {
            reminderDao.update(model);
            Log.i(TAG, "Updated reminder: " + model);
          } catch (Exception e) {
            Log.e(TAG, "Error while updating the reminder: " + model, e);
          }
        });
  }

  public LiveData<List<ReminderModel>> getAll() {
    return getAllObservable;
  }

  public List<ReminderModel> getAllSync() {
    return reminderDao.getAllSync();
  }

  public void updateSnoozedUntil(int reminderId, Long snoozedUntil) {
    reminderDaoExecutor.submit(
        () -> {
          try {
            reminderDao.updateSnoozedUntil(reminderId, snoozedUntil);
            Log.i(TAG, "Updated reminder " + reminderId + " snoozedUntil to: " + snoozedUntil);
          } catch (Exception e) {
            Log.e(TAG, "Exception while updating snoozedUntil for reminder: " + reminderId, e);
          }
        });
  }

  public void updateSnoozedUntilSync(int reminderId, Long snoozedUntil) {
    reminderDao.updateSnoozedUntil(reminderId, snoozedUntil);
    Log.i(TAG, "Updated reminder " + reminderId + " snoozedUntil to: " + snoozedUntil + " (sync)");
  }
}
