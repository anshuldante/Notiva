package com.ava.notiva.service;

import static com.ava.notiva.util.ReminderConstants.REMINDER_ID;
import static com.ava.notiva.util.ReminderConstants.REMINDER_NAME;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.room.Room;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.ava.notiva.data.ReminderDao;
import com.ava.notiva.data.RemindersDb;
import com.ava.notiva.model.ReminderModel;
import com.ava.notiva.util.PendingIntentRequestCodes;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class BootReceiver extends BroadcastReceiver {

  private static final String TAG = "Notiva.BootReceiver";

  @Override
  public void onReceive(Context context, Intent intent) {
    if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
      return;
    }

    Log.i(TAG, "Device booted, re-enqueuing WorkManager and recovering reminders");

    // Step 1: Re-enqueue WorkManager periodic task with REPLACE policy for fresh scheduling
    PeriodicWorkRequest periodicWorkRequest =
        new PeriodicWorkRequest.Builder(ReminderTriggerWorker.class, 15, TimeUnit.MINUTES)
            .build();
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "ReminderSync",
        ExistingPeriodicWorkPolicy.UPDATE,
        periodicWorkRequest
    );
    Log.i(TAG, "WorkManager periodic work re-enqueued with REPLACE policy");

    // Step 2: Check for overdue reminders and reschedule future ones on a background thread.
    // BootReceiver is a plain BroadcastReceiver (not @AndroidEntryPoint), so we build
    // the Room database directly instead of using Hilt injection.
    Context appContext = context.getApplicationContext();
    new Thread(() -> processRemindersAfterBoot(appContext)).start();
  }

  /**
   * Processes all active reminders after boot:
   * - Overdue reminders are fired immediately via startForegroundService
   * - Future reminders are scheduled via AlarmManager exact alarms
   *
   * <p>All alarms are lost on device reboot, so this method reschedules everything.
   * The NotificationStarterService's burst channel logic handles sound cascade
   * prevention when multiple overdue reminders fire in rapid succession.
   */
  private void processRemindersAfterBoot(Context context) {
    RemindersDb db = null;
    try {
      db = Room.databaseBuilder(context, RemindersDb.class, "Reminders-DB")
          .addMigrations(RemindersDb.MIGRATION_1_2, RemindersDb.MIGRATION_2_3)
          .build();
      ReminderDao dao = db.reminderDao();
      List<ReminderModel> reminders = dao.getAllSync();

      Calendar now = Calendar.getInstance();
      AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
      boolean canScheduleExact = canScheduleExactAlarms(alarmMgr);
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

      int overdueCount = 0;
      int scheduledCount = 0;
      int skippedCount = 0;

      for (ReminderModel reminder : reminders) {
        if (!reminder.isActive()) {
          skippedCount++;
          continue;
        }
        if (reminder.isSnoozed()) {
          skippedCount++;
          Log.d(TAG, "Skipping snoozed reminder: ID=" + reminder.getId());
          continue;
        }

        Calendar next = reminder.getNextOccurrenceAfter(now);
        if (next == null) {
          skippedCount++;
          continue;
        }

        if (!next.after(now)) {
          // Overdue: fire immediately. The service's burst channel logic handles
          // sound cascade prevention for multiple overdue reminders.
          overdueCount++;
          Log.i(TAG, "Firing overdue reminder: ID=" + reminder.getId()
              + ", Name='" + reminder.getName() + "'"
              + ", ScheduledTime=" + sdf.format(next.getTime()));
          fireReminderImmediately(context, reminder);
        } else {
          // Future: schedule via AlarmManager
          scheduledCount++;
          scheduleAlarm(context, alarmMgr, reminder, next, canScheduleExact);
          Log.i(TAG, "Scheduled future reminder: ID=" + reminder.getId()
              + ", Name='" + reminder.getName() + "'"
              + ", AlarmTime=" + sdf.format(next.getTime())
              + ", Exact=" + canScheduleExact);
        }
      }

      Log.i(TAG, "Boot recovery complete: " + reminders.size() + " total reminders"
          + ", " + overdueCount + " fired immediately (overdue)"
          + ", " + scheduledCount + " alarms scheduled (future)"
          + ", " + skippedCount + " skipped (inactive/snoozed/no-next)");

    } catch (Exception e) {
      Log.e(TAG, "Error processing reminders after boot", e);
    } finally {
      if (db != null) {
        db.close();
      }
    }
  }

  /**
   * Fires a reminder immediately by starting NotificationStarterService
   * as a foreground service.
   */
  private void fireReminderImmediately(Context context, ReminderModel reminder) {
    Intent serviceIntent = new Intent(context, NotificationStarterService.class);
    serviceIntent.putExtra(REMINDER_ID, reminder.getId());
    serviceIntent.putExtra(REMINDER_NAME, reminder.getName());
    ContextCompat.startForegroundService(context, serviceIntent);
  }

  /**
   * Schedules an AlarmManager alarm for a future reminder occurrence.
   * Uses exact alarm if permission is granted, otherwise falls back to inexact.
   */
  private void scheduleAlarm(Context context, AlarmManager alarmMgr,
                             ReminderModel reminder, Calendar alarmTime, boolean canScheduleExact) {
    Intent alarmIntent = new Intent(context, NotificationStarterService.class);
    alarmIntent.putExtra(REMINDER_ID, reminder.getId());
    alarmIntent.putExtra(REMINDER_NAME, reminder.getName());
    PendingIntent pendingIntent = PendingIntent.getForegroundService(
        context,
        PendingIntentRequestCodes.forAlarm(reminder.getId()),
        alarmIntent,
        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

    if (canScheduleExact) {
      alarmMgr.setExactAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          alarmTime.getTimeInMillis(),
          pendingIntent);
    } else {
      alarmMgr.setAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          alarmTime.getTimeInMillis(),
          pendingIntent);
    }
  }

  /**
   * Checks whether exact alarm scheduling is permitted.
   */
  private boolean canScheduleExactAlarms(AlarmManager alarmMgr) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      return alarmMgr.canScheduleExactAlarms();
    }
    return true;
  }
}
