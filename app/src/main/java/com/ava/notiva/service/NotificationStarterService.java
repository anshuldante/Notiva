package com.ava.notiva.service;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;
import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static com.ava.notiva.util.ReminderConstants.ACTION_DISMISS;
import static com.ava.notiva.util.ReminderConstants.ACTION_SNOOZE;
import static com.ava.notiva.util.ReminderConstants.CHANNEL_ID;
import static com.ava.notiva.util.ReminderConstants.CHANNEL_NAME;
import static com.ava.notiva.util.ReminderConstants.FOREGROUND_CHANNEL_ID;
import static com.ava.notiva.util.ReminderConstants.FOREGROUND_CHANNEL_NAME;
import static com.ava.notiva.util.ReminderConstants.NOTIFICATION_GROUP_KEY;
import static com.ava.notiva.util.ReminderConstants.REMINDER_ID;
import static com.ava.notiva.util.ReminderConstants.REMINDER_NAME;
import static com.ava.notiva.util.ReminderConstants.SCHEDULED_FIRE_EPOCH;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.ava.notiva.R;
import com.ava.notiva.data.ReminderDao;
import com.ava.notiva.util.NotificationGroupManager;
import com.ava.notiva.util.NotificationIdGenerator;
import com.ava.notiva.util.NotificationPreferences;
import com.ava.notiva.util.PendingIntentRequestCodes;

import java.util.Date;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NotificationStarterService extends Service {

  public static final String TAG = "Notiva.NotificationStarterService";

  /** Notification ID for the persistent foreground service notification. */
  private static final int FOREGROUND_NOTIFICATION_ID = Integer.MAX_VALUE;

  /** Channel ID suffix for burst (silent) alarm notifications. */
  private static final String BURST_CHANNEL_SUFFIX = "_burst";

  /** Self-stop timeout: 5 minutes after the last reminder fires. */
  private static final long SELF_STOP_TIMEOUT_MILLIS = 5 * 60 * 1000L;

  @Inject
  ReminderDao reminderDao;

  private NotificationManagerCompat notificationManager;

  /** Tracks when the last notification sound was played for burst window logic. */
  private long lastSoundPlayedAt = 0;

  /** Handler for the 5-minute self-stop timeout. */
  private final Handler timeoutHandler = new Handler(Looper.getMainLooper());

  /** Runnable that stops the service after timeout. */
  private final Runnable selfStopRunnable = () -> {
    Log.i(TAG, "Self-stop timeout reached (5 min), stopping service");
    stopForeground(STOP_FOREGROUND_REMOVE);
    stopSelf();
  };

  @Override
  public void onCreate() {
    super.onCreate();
    Log.i(TAG, "Inside onCreate");

    notificationManager = NotificationManagerCompat.from(this);

    createForegroundChannel();
    Notification persistentNotification = buildForegroundNotification();
    startForeground(FOREGROUND_NOTIFICATION_ID, persistentNotification);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    // Guard against null intent (can happen on service restart by system)
    if (intent == null) {
      Log.w(TAG, "onStartCommand received null intent, stopping service");
      stopSelf();
      return START_NOT_STICKY;
    }

    int reminderId = intent.getIntExtra(REMINDER_ID, -1);
    String notificationName = intent.getStringExtra(REMINDER_NAME);
    long scheduledFireEpoch = intent.getLongExtra(SCHEDULED_FIRE_EPOCH, System.currentTimeMillis());
    int notificationId = NotificationIdGenerator.generate(reminderId, scheduledFireEpoch);
    Log.i(TAG, "Inside onStartCommand, reminderId=" + reminderId
        + ", notificationId=" + notificationId + ", scheduledFireEpoch=" + scheduledFireEpoch);
    Log.i(TAG, "Starting alarm at: " + new Date());

    // Clear snooze state and update last_fired_at
    if (reminderId != -1) {
      new Thread(() -> {
        try {
          reminderDao.updateSnoozedUntil(reminderId, null);
          Log.i(TAG, "Cleared snooze state for reminder " + reminderId);
          reminderDao.updateLastFiredAt(reminderId, System.currentTimeMillis());
          Log.i(TAG, "Updated last_fired_at for reminder " + reminderId);
        } catch (Exception e) {
          Log.e(TAG, "Failed to update state for reminder " + reminderId, e);
        }
      }).start();
    }

    // Step 1: Determine if this notification is within a burst window
    boolean withinBurst = isWithinBurstWindow();

    // Step 2: Create/recreate the appropriate alarm channel
    String channelId;
    if (withinBurst) {
      channelId = CHANNEL_ID + BURST_CHANNEL_SUFFIX;
      createAlarmChannel(channelId, false);
    } else {
      channelId = CHANNEL_ID;
      createAlarmChannel(channelId, true);
      lastSoundPlayedAt = System.currentTimeMillis();
    }

    // Step 3: Build and post the reminder notification
    Notification notification = buildAlarmNotification(channelId, reminderId, notificationId, notificationName);
    notificationManager.notify(notificationId, notification);

    // Step 4: Update summary and apply collapse logic
    NotificationManager platformManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    NotificationGroupManager.updateSummaryAndCollapse(this, platformManager);

    // Reset the 5-minute self-stop timer
    resetSelfStopTimeout();

    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.i(TAG, "Service destroyed, cancelling timeout handler");
    timeoutHandler.removeCallbacks(selfStopRunnable);
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  // -------------------------------------------------------------------------
  // Foreground service notification (persistent, low-priority)
  // -------------------------------------------------------------------------

  private void createForegroundChannel() {
    NotificationChannel channel = new NotificationChannel(
        FOREGROUND_CHANNEL_ID,
        FOREGROUND_CHANNEL_NAME,
        NotificationManager.IMPORTANCE_LOW);
    channel.setDescription("Keeps Notiva active for reliable reminder delivery");
    notificationManager.createNotificationChannel(channel);
  }

  private Notification buildForegroundNotification() {
    return new NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_alarm)
        .setContentTitle("Notiva")
        .setContentText("Notiva is running")
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .build();
  }

  // -------------------------------------------------------------------------
  // Alarm notification channels (with-sound and burst/silent variants)
  // -------------------------------------------------------------------------

  /**
   * Creates (or recreates) an alarm notification channel.
   * <p>
   * Android caches channel settings after first creation. To allow preference
   * changes to take effect, the channel is deleted and recreated each time.
   *
   * @param channelId  the channel ID to create
   * @param withSound  true for the main channel (plays ringtone), false for burst (silent)
   */
  private void createAlarmChannel(String channelId, boolean withSound) {
    // Delete first to pick up preference changes
    notificationManager.deleteNotificationChannel(channelId);

    NotificationChannel channel = new NotificationChannel(
        channelId,
        CHANNEL_NAME,
        NotificationManager.IMPORTANCE_HIGH);
    channel.setDescription("Notiva reminder alerts");
    channel.setLightColor(Color.BLUE);
    channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

    if (withSound) {
      Uri ringtoneUri = NotificationPreferences.getRingtoneUri(this);
      channel.setSound(ringtoneUri, Notification.AUDIO_ATTRIBUTES_DEFAULT);
    } else {
      channel.setSound(null, null);
    }

    // Vibration is preference-driven on both channel types
    boolean vibrationEnabled = NotificationPreferences.isVibrationEnabled(this);
    if (vibrationEnabled) {
      channel.enableVibration(true);
      channel.setVibrationPattern(new long[]{0, 500, 300, 500});
    } else {
      channel.enableVibration(false);
    }

    notificationManager.createNotificationChannel(channel);
  }

  // -------------------------------------------------------------------------
  // Alarm notification building
  // -------------------------------------------------------------------------

  private Notification buildAlarmNotification(String channelId, int reminderId, int notificationId, String notificationName) {
    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
        .setSmallIcon(R.drawable.ic_alarm)
        .setContentText(buildReminderText(reminderId, notificationName))
        .setContentTitle(notificationName)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setTicker(getText(R.string.ticker_text))
        .setAutoCancel(true)
        .setGroup(NOTIFICATION_GROUP_KEY);

    attachSnoozeAction(builder, reminderId, notificationId, notificationName);
    attachDismissAction(builder, reminderId, notificationId, notificationName);

    Log.i(TAG, "Notification Built for reminderId=" + reminderId + ", notificationId=" + notificationId);
    return builder.build();
  }

  private void attachDismissAction(NotificationCompat.Builder builder, int reminderId, int notificationId, String notificationName) {
    Intent dismissIntent = new Intent(this, NotificationStopperService.class);
    dismissIntent.setAction(ACTION_DISMISS);
    dismissIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
    dismissIntent.putExtra(REMINDER_ID, reminderId);
    dismissIntent.putExtra(REMINDER_NAME, notificationName);
    PendingIntent dismissPendingIntent =
        PendingIntent.getService(this, PendingIntentRequestCodes.forDismiss(notificationId), dismissIntent, FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT);
    builder.addAction(
        R.drawable.ic_baseline_cancel_24, getString(R.string.dismiss), dismissPendingIntent);
    builder.setContentIntent(dismissPendingIntent);
    // Swipe-dismiss also triggers NotificationStopperService to update last_acknowledged_at
    builder.setDeleteIntent(dismissPendingIntent);
  }

  private void attachSnoozeAction(NotificationCompat.Builder builder, int reminderId, int notificationId, String notificationName) {
    Intent snoozeIntent = new Intent(this, NotificationStopperService.class);
    snoozeIntent.setAction(ACTION_SNOOZE);
    snoozeIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
    snoozeIntent.putExtra(REMINDER_ID, reminderId);
    snoozeIntent.putExtra(REMINDER_NAME, notificationName);
    PendingIntent snoozePendingIntent =
        PendingIntent.getService(this, PendingIntentRequestCodes.forSnooze(notificationId), snoozeIntent, FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT);
    builder.addAction(
        R.drawable.ic_baseline_snooze_24, getString(R.string.snooze), snoozePendingIntent);
  }

  @NonNull
  private String buildReminderText(int reminderId, String notificationName) {
    StringBuilder contentTextBuilder = new StringBuilder();
    contentTextBuilder.append("Ring Ring...Ring Ring");
    if (reminderId != -1) {
      contentTextBuilder.append(" | ID: ").append(reminderId);
    } else {
      Log.i(TAG, "Reminder ID was -1");
    }
    if (notificationName != null && !notificationName.trim().isEmpty()) {
      contentTextBuilder.append(" | Name: ").append(notificationName);
    } else {
      Log.i(TAG, "Reminder name was null or empty");
    }
    return contentTextBuilder.toString();
  }

  // -------------------------------------------------------------------------
  // Burst window tracking
  // -------------------------------------------------------------------------

  /**
   * Returns true if the current time is within the burst window of the last
   * notification that played sound. Prevents sound cascade when multiple
   * reminders fire in quick succession.
   */
  private boolean isWithinBurstWindow() {
    return lastSoundPlayedAt > 0
        && (System.currentTimeMillis() - lastSoundPlayedAt) < NotificationPreferences.getBurstWindowMillis();
  }

  // -------------------------------------------------------------------------
  // Self-stop timeout (5 minutes)
  // -------------------------------------------------------------------------

  /**
   * Resets the 5-minute self-stop timer. Called each time a new reminder
   * fires. If no new reminders fire within 5 minutes, the service stops itself.
   */
  private void resetSelfStopTimeout() {
    timeoutHandler.removeCallbacks(selfStopRunnable);
    timeoutHandler.postDelayed(selfStopRunnable, SELF_STOP_TIMEOUT_MILLIS);
  }
}
