package com.ava.notiva.service;

import static com.ava.notiva.util.ReminderConstants.ACTION_SNOOZE;
import static com.ava.notiva.util.ReminderConstants.REMINDER_ID;
import static com.ava.notiva.util.ReminderConstants.REMINDER_NAME;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class NotificationStopperService extends Service {

  public static final String TAG = "Notiva.NotificationStopperService";

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

    Log.i(TAG, "NotificationStopperService starting up");
    String action = intent != null ? intent.getAction() : null;
    Log.i(TAG, "Action received: " + action);
    Intent intentService = new Intent(getApplicationContext(), NotificationStarterService.class);
    getApplicationContext().stopService(intentService);

    if (ACTION_SNOOZE.equals(action)) {
      scheduleSnoozeAlarm(intent);
      Toast.makeText(getApplicationContext(), "Reminder snoozed for 10 minutes", Toast.LENGTH_SHORT).show();
    }
    return super.onStartCommand(intent, flags, startId);
  }

  private void scheduleSnoozeAlarm(Intent intent) {
    int reminderId = intent.getIntExtra(REMINDER_ID, -1);
    String reminderName = intent.getStringExtra(REMINDER_NAME);

    if (reminderId == -1) {
      Log.w(TAG, "Cannot snooze: reminder ID not found in intent");
      return;
    }

    // Schedule alarm for 10 minutes from now
    long snoozeDelayMillis = 10 * 60 * 1000L;
    long snoozeTime = System.currentTimeMillis() + snoozeDelayMillis;

    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    Intent alarmIntent = new Intent(this, NotificationStarterService.class);
    alarmIntent.putExtra(REMINDER_ID, reminderId);
    alarmIntent.putExtra(REMINDER_NAME, reminderName);

    // Use unique request code (reminderId + 2000000) to avoid conflicts with dismiss/snooze PendingIntents
    PendingIntent pendingIntent = PendingIntent.getService(
        this, reminderId + 2000000, alarmIntent,
        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent);
    Log.i(TAG, "Snoozed reminder " + reminderId + " for 10 minutes");
  }
}
