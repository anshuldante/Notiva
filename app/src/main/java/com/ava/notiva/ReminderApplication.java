package com.ava.notiva;

import android.app.Application;

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
    // WorkManager enforces a minimum interval of 15 minutes for periodic work.
    // Requesting a shorter interval (e.g., 1 minute) is silently increased to 15 minutes.
    // For time-sensitive reminders, the actual precision comes from AlarmManager
    // used in snooze functionality and could be extended for all reminders if needed.
    PeriodicWorkRequest periodicWorkRequest =
        new PeriodicWorkRequest.Builder(ReminderTriggerWorker.class, 15, TimeUnit.MINUTES)
            .build();
    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        "ReminderSync",
        ExistingPeriodicWorkPolicy.KEEP,
        periodicWorkRequest
    );
  }
}
