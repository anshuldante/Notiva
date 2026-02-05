package com.ava.notiva.service;

import static com.ava.notiva.util.ReminderConstants.REMINDER_ID;
import static com.ava.notiva.util.ReminderConstants.REMINDER_NAME;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.ava.notiva.data.ReminderRepository;
import com.ava.notiva.model.ReminderModel;

import java.util.Calendar;
import java.util.List;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class ReminderTriggerWorker extends Worker {
  public static final String TAG = "ReminderTriggerWorker";
  private final ReminderRepository reminderRepository;

  @AssistedInject
  public ReminderTriggerWorker(@Assisted @NonNull Context context,
                               @Assisted @NonNull WorkerParameters params,
                               ReminderRepository reminderRepository) {
    super(context, params);
    this.reminderRepository = reminderRepository;
  }

  @NonNull
  @Override
  public Result doWork() {
    try {
      List<ReminderModel> reminders = reminderRepository.getAllSync();
      Calendar now = Calendar.getInstance();
      AlarmManager alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
      for (ReminderModel reminder : reminders) {
        if (!reminder.isActive()) continue;
        if (reminder.isSnoozed()) {
          Log.i(TAG, "Skipping snoozed reminder: ID=" + reminder.getId() + ", snoozedUntil=" + reminder.getSnoozedUntil());
          continue;
        }
        Calendar next = reminder.getNextOccurrenceAfter(now);
        if (next != null && next.after(now)) {
          Intent alarmIntent = new Intent(getApplicationContext(), NotificationStarterService.class);
          alarmIntent.putExtra(REMINDER_ID, reminder.getId());
          alarmIntent.putExtra(REMINDER_NAME, reminder.getName());
          PendingIntent pendingIntent = PendingIntent.getService(
              getApplicationContext(), reminder.getId(), alarmIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
          alarmMgr.setExactAndAllowWhileIdle(
              AlarmManager.RTC_WAKEUP,
              next.getTimeInMillis(),
              pendingIntent
          );
          Log.i(TAG, "Scheduled reminder: ID=" + reminder.getId() + ", Name='" + reminder.getName() + "', Time=" + next.getTime());
        }
      }
      Log.i(TAG, "Total reminders scheduled: " + reminders.size());
      return Result.success();
    } catch (Exception e) {
      Log.e(TAG, "Error scheduling reminders", e);
      return Result.failure();
    }
  }
}
