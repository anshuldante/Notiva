package com.ava.notiva.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.preference.PreferenceManager;

import com.ava.notiva.model.CollapseStrategy;
import com.ava.notiva.model.NotificationPolicy;
import com.ava.notiva.model.SoundStrategy;

/**
 * Centralized reader for all notification-related preferences.
 * <p>
 * Bridges Phase 12's SharedPreferences keys (ringtone, vibration, snooze)
 * with Phase 13's notification policy system (collapse strategy, sound strategy,
 * max active notifications).
 */
public final class NotificationPreferences {

  // Phase 12 preference keys (match preferences.xml)
  private static final String KEY_RINGTONE = "pref_notification_ringtone";
  private static final String KEY_VIBRATION = "pref_notification_vibration";
  private static final String KEY_SNOOZE_DURATION = "pref_snooze_duration";

  // Phase 13 policy keys (not yet exposed in preferences UI)
  private static final String KEY_COLLAPSE_STRATEGY = "pref_notification_collapse_strategy";
  private static final String KEY_SOUND_STRATEGY = "pref_notification_sound_strategy";
  private static final String KEY_MAX_ACTIVE = "pref_notification_max_active";

  /** Burst window duration in milliseconds (60 seconds). */
  private static final long BURST_WINDOW_MILLIS = 60_000L;

  private NotificationPreferences() {
    // Utility class
  }

  /**
   * Returns the user's chosen notification ringtone URI.
   * <p>
   * If no ringtone has been set (empty or null preference), returns the system
   * default alarm URI as a sensible fallback.
   *
   * @param context application or activity context
   * @return the ringtone URI, or system default alarm URI if not configured
   */
  public static Uri getRingtoneUri(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    String uriString = prefs.getString(KEY_RINGTONE, null);
    if (uriString == null || uriString.isEmpty()) {
      return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    }
    return Uri.parse(uriString);
  }

  /**
   * Returns whether vibration is enabled for notifications.
   *
   * @param context application or activity context
   * @return true if vibration is enabled (default: true)
   */
  public static boolean isVibrationEnabled(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    return prefs.getBoolean(KEY_VIBRATION, true);
  }

  /**
   * Returns the snooze duration in minutes.
   * <p>
   * The value is stored as a String by the ListPreference (Phase 12),
   * so it is parsed to int here. Falls back to 10 minutes on parse failure.
   *
   * @param context application or activity context
   * @return snooze duration in minutes (default: 10)
   */
  public static int getSnoozeDurationMinutes(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    String value = prefs.getString(KEY_SNOOZE_DURATION, "10");
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      return 10;
    }
  }

  /**
   * Returns the current notification policy read from SharedPreferences.
   * <p>
   * If policy preferences have not been set, returns the defaults
   * (STACK collapse, ONCE sound, 5 max active). Falls back to defaults
   * on any parse failure.
   *
   * @param context application or activity context
   * @return the current NotificationPolicy
   */
  public static NotificationPolicy getNotificationPolicy(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

    CollapseStrategy collapseStrategy;
    try {
      String collapseValue = prefs.getString(KEY_COLLAPSE_STRATEGY, "STACK");
      collapseStrategy = CollapseStrategy.valueOf(collapseValue);
    } catch (IllegalArgumentException e) {
      collapseStrategy = CollapseStrategy.STACK;
    }

    SoundStrategy soundStrategy;
    try {
      String soundValue = prefs.getString(KEY_SOUND_STRATEGY, "ONCE");
      soundStrategy = SoundStrategy.valueOf(soundValue);
    } catch (IllegalArgumentException e) {
      soundStrategy = SoundStrategy.ONCE;
    }

    int maxActive = prefs.getInt(KEY_MAX_ACTIVE, 5);

    return new NotificationPolicy(collapseStrategy, soundStrategy, maxActive);
  }

  /**
   * Returns the burst window duration in milliseconds.
   * <p>
   * Notifications firing within this window of the first notification are
   * considered part of the same "burst" for sound strategy purposes.
   *
   * @return burst window in milliseconds (60,000 ms = 60 seconds)
   */
  public static long getBurstWindowMillis() {
    return BURST_WINDOW_MILLIS;
  }
}
