package com.ava.notiva;

import android.app.AlarmManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.work.Configuration;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.ava.notiva.service.ReminderTriggerWorker;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class ReminderApplication extends Application implements Configuration.Provider {

  private static final String TAG = "Notiva.ReminderApplication";

  @Inject
  HiltWorkerFactory workerFactory;

  @NonNull
  @Override
  public Configuration getWorkManagerConfiguration() {
    return new Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .build();
  }

  @Override
  public void onCreate() {
    super.onCreate();

    // Log exact alarm permission status (informational)
    logExactAlarmPermissionStatus();

    // WorkManager enforces a minimum interval of 15 minutes for periodic work.
    // Requesting a shorter interval (e.g., 1 minute) is silently increased to 15 minutes.
    // For time-sensitive reminders, the actual precision comes from AlarmManager
    // exact alarms scheduled by the ReminderTriggerWorker for each active reminder.
    PeriodicWorkRequest periodicWorkRequest =
        new PeriodicWorkRequest.Builder(ReminderTriggerWorker.class, 15, TimeUnit.MINUTES)
            .build();
    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        "ReminderSync",
        ExistingPeriodicWorkPolicy.KEEP,
        periodicWorkRequest
    );
  }

  /**
   * Logs whether the SCHEDULE_EXACT_ALARM permission is granted.
   * On Android 12+ (API 31+), this permission requires user grant in system settings.
   * If not granted, the app falls back to inexact alarms which may be delayed.
   */
  private void logExactAlarmPermissionStatus() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
      boolean canSchedule = alarmMgr.canScheduleExactAlarms();
      if (canSchedule) {
        Log.i(TAG, "SCHEDULE_EXACT_ALARM permission granted -- exact alarms enabled");
      } else {
        Log.w(TAG, "SCHEDULE_EXACT_ALARM permission NOT granted -- falling back to inexact alarms. "
            + "For best reminder reliability, grant the permission in system Settings > Apps > Notiva > Alarms & reminders");
      }
    } else {
      Log.i(TAG, "Pre-Android 12 device -- exact alarms always available");
    }
  }
}
