package com.ava.notiva.service;

import static com.ava.notiva.util.ReminderConstants.REMINDER_ID;
import static com.ava.notiva.util.ReminderConstants.REMINDER_NAME;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.ava.notiva.data.ReminderRepository;
import com.ava.notiva.model.ReminderModel;
import com.ava.notiva.util.PendingIntentRequestCodes;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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
      Context appContext = getApplicationContext();
      AlarmManager alarmMgr = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);

      boolean canScheduleExact = canScheduleExactAlarms(alarmMgr);
      if (!canScheduleExact) {
        Log.w(TAG, "SCHEDULE_EXACT_ALARM permission not granted; falling back to inexact alarms");
      }

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
      int scheduledCount = 0;
      int overdueCount = 0;
      int skippedCount = 0;

      for (ReminderModel reminder : reminders) {
        if (!reminder.isActive()) {
          skippedCount++;
          continue;
        }
        if (reminder.isSnoozed()) {
          skippedCount++;
          Log.i(TAG, "Skipping snoozed reminder: ID=" + reminder.getId()
              + ", snoozedUntil=" + reminder.getSnoozedUntil());
          continue;
        }

        Calendar next = reminder.getNextOccurrenceAfter(now);
        if (next == null) {
          skippedCount++;
          continue;
        }

        // Cancel any existing stale alarm for this reminder before rescheduling
        cancelExistingAlarm(appContext, alarmMgr, reminder.getId());

        if (!next.after(now)) {
          // Overdue: next occurrence is now or in the past. Fire immediately.
          overdueCount++;
          Log.i(TAG, "Firing overdue reminder immediately: ID=" + reminder.getId()
              + ", Name='" + reminder.getName() + "'"
              + ", ScheduledTime=" + sdf.format(next.getTime()));
          fireReminderImmediately(appContext, reminder);
        } else {
          // Future: schedule via AlarmManager
          scheduledCount++;
          Intent alarmIntent = new Intent(appContext, NotificationStarterService.class);
          alarmIntent.putExtra(REMINDER_ID, reminder.getId());
          alarmIntent.putExtra(REMINDER_NAME, reminder.getName());
          PendingIntent pendingIntent = PendingIntent.getForegroundService(
              appContext,
              PendingIntentRequestCodes.forAlarm(reminder.getId()),
              alarmIntent,
              PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

          if (canScheduleExact) {
            alarmMgr.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                next.getTimeInMillis(),
                pendingIntent);
          } else {
            alarmMgr.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                next.getTimeInMillis(),
                pendingIntent);
          }
          Log.i(TAG, "Scheduled reminder: ID=" + reminder.getId()
              + ", Name='" + reminder.getName() + "'"
              + ", AlarmTime=" + sdf.format(next.getTime())
              + ", Exact=" + canScheduleExact);
        }
      }

      Log.i(TAG, "Worker complete: " + reminders.size() + " total reminders processed"
          + ", " + scheduledCount + " alarms scheduled"
          + ", " + overdueCount + " fired immediately (overdue)"
          + ", " + skippedCount + " skipped (inactive/snoozed/no-next)");
      return Result.success();
    } catch (Exception e) {
      Log.e(TAG, "Error scheduling reminders", e);
      return Result.failure();
    }
  }

  /**
   * Checks whether exact alarm scheduling is permitted.
   * On Android 12+ (API 31+), SCHEDULE_EXACT_ALARM requires user grant in system settings.
   * On earlier versions, exact alarms are always allowed.
   */
  private boolean canScheduleExactAlarms(AlarmManager alarmMgr) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      return alarmMgr.canScheduleExactAlarms();
    }
    return true;
  }

  /**
   * Cancels any existing PendingIntent alarm for the given reminder ID.
   * Prevents duplicate firings when rescheduling.
   */
  private void cancelExistingAlarm(Context context, AlarmManager alarmMgr, int reminderId) {
    Intent alarmIntent = new Intent(context, NotificationStarterService.class);
    PendingIntent existing = PendingIntent.getForegroundService(
        context,
        PendingIntentRequestCodes.forAlarm(reminderId),
        alarmIntent,
        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE);
    if (existing != null) {
      alarmMgr.cancel(existing);
      existing.cancel();
      Log.d(TAG, "Cancelled stale alarm for reminder ID=" + reminderId);
    }
  }

  /**
   * Fires a reminder immediately by starting NotificationStarterService
   * as a foreground service. Used for overdue reminders whose scheduled
   * time has already passed.
   */
  private void fireReminderImmediately(Context context, ReminderModel reminder) {
    Intent serviceIntent = new Intent(context, NotificationStarterService.class);
    serviceIntent.putExtra(REMINDER_ID, reminder.getId());
    serviceIntent.putExtra(REMINDER_NAME, reminder.getName());
    ContextCompat.startForegroundService(context, serviceIntent);
  }
}
