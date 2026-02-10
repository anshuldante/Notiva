package com.ava.notiva.service;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;
import static com.ava.notiva.util.ReminderConstants.ACTION_SNOOZE;
import static com.ava.notiva.util.ReminderConstants.REMINDER_ID;
import static com.ava.notiva.util.ReminderConstants.REMINDER_NAME;
import static com.ava.notiva.util.ReminderConstants.SCHEDULED_FIRE_EPOCH;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

import com.ava.notiva.data.ReminderDao;
import com.ava.notiva.util.NotificationGroupManager;
import com.ava.notiva.util.NotificationPreferences;
import com.ava.notiva.util.PendingIntentRequestCodes;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NotificationStopperService extends Service {

  public static final String TAG = "Notiva.NotificationStopperService";

  @Inject
  ReminderDao reminderDao;

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

    Log.i(TAG, "NotificationStopperService starting up");
    String action = intent != null ? intent.getAction() : null;
    Log.i(TAG, "Action received: " + action);

    // Extract bit-packed notification ID (for cancellation) and reminder ID (for DB operations)
    int notificationId = intent != null ? intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1) : -1;
    int reminderId = intent != null ? intent.getIntExtra(REMINDER_ID, -1) : -1;
    Log.i(TAG, "reminderId=" + reminderId + ", notificationId=" + notificationId);

    // Cancel the specific notification using its bit-packed ID
    if (notificationId != -1) {
      NotificationManagerCompat.from(this).cancel(notificationId);
      Log.i(TAG, "Cancelled notification ID " + notificationId + " for reminder " + reminderId);
    }

    // Update last_acknowledged_at using the original reminder ID
    if (reminderId != -1) {
      new Thread(() -> {
        try {
          reminderDao.updateLastAcknowledgedAt(reminderId, System.currentTimeMillis());
          Log.i(TAG, "Updated last_acknowledged_at for reminder " + reminderId);
        } catch (Exception e) {
          Log.e(TAG, "Failed to update last_acknowledged_at for reminder " + reminderId, e);
        }
      }).start();
    }

    // Update summary and apply collapse logic after cancellation
    NotificationManager platformManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    NotificationGroupManager.updateSummaryAndCollapse(this, platformManager);

    if (ACTION_SNOOZE.equals(action)) {
      scheduleSnoozeAlarm(intent);
      int snoozeDuration = NotificationPreferences.getSnoozeDurationMinutes(this);
      Toast.makeText(getApplicationContext(),
          "Reminder snoozed for " + snoozeDuration + " minutes",
          Toast.LENGTH_SHORT).show();
    }

    stopSelf();
    return START_NOT_STICKY;
  }

  private void scheduleSnoozeAlarm(Intent intent) {
    int reminderId = intent.getIntExtra(REMINDER_ID, -1);
    String reminderName = intent.getStringExtra(REMINDER_NAME);

    if (reminderId == -1) {
      Log.w(TAG, "Cannot snooze: reminder ID not found in intent");
      return;
    }

    // Schedule alarm using preference-driven snooze duration
    int snoozeDurationMinutes = NotificationPreferences.getSnoozeDurationMinutes(this);
    long snoozeDelayMillis = snoozeDurationMinutes * 60 * 1000L;
    long snoozeTime = System.currentTimeMillis() + snoozeDelayMillis;

    // Mark reminder as snoozed in database so regular scheduling skips it
    new Thread(() -> {
      try {
        reminderDao.updateSnoozedUntil(reminderId, snoozeTime);
        Log.i(TAG, "Set snoozedUntil=" + snoozeTime + " for reminder " + reminderId);
      } catch (Exception e) {
        Log.e(TAG, "Failed to update snoozedUntil for reminder " + reminderId, e);
      }
    }).start();

    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    Intent alarmIntent = new Intent(this, NotificationStarterService.class);
    alarmIntent.putExtra(REMINDER_ID, reminderId);
    alarmIntent.putExtra(REMINDER_NAME, reminderName);
    alarmIntent.putExtra(SCHEDULED_FIRE_EPOCH, snoozeTime);

    PendingIntent pendingIntent = PendingIntent.getForegroundService(
        this, PendingIntentRequestCodes.forSnoozeAlarm(reminderId), alarmIntent,
        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent);
    Log.i(TAG, "Snoozed reminder " + reminderId + " for " + snoozeDurationMinutes + " minutes");
  }
}
