package com.ava.notiva.util;

public class ReminderConstants {


  public static final String ACTION_SNOOZE = "Snooze";
  public static final String ACTION_DISMISS = "Dismiss";
  public static final long DEFAULT_SNOOZE_TIME_10_MINUTES = 10 * 1000L;
  public static final String CHANNEL_ID = "NOTIVA_CHANNEL";
  public static final String CHANNEL_NAME = "com.ava.notiva";
  public static final String CHANNEL_DESCRIPTION =
      "This channel is used by Notiva for displaying Alarms";
  public static final String FOREGROUND_CHANNEL_ID = "NOTIVA_FOREGROUND_CHANNEL";
  public static final String FOREGROUND_CHANNEL_NAME = "Notiva Service";
  public static final String REMINDER_ID = "com.ava.notiva.REMINDER_ID";
  public static final String REMINDER_ACTIVE = "com.ava.notiva.REMINDER_ACTIVE";
  public static final String REMINDER_NAME = "com.ava.notiva.REMINDER_NAME";
  public static final String REMINDER_START_TIME = "com.ava.notiva.REMINDER_START_TIME";
  public static final String REMINDER_RECURRENCE_DELAY = "com.ava.notiva.REMINDER_REC_DELAY";
  public static final String REMINDER_RECURRENCE_TYPE = "com.ava.notiva.REMINDER_REC_TYPE";
  public static final String REMINDER_END_TIME = "com.ava.notiva.REMINDER_END_TIME";
  private static final int MAX_RECURRENCE_NUMBER = 1000;

  // --- Phase 14: Notification grouping and collapse constants ---

  /** Silent channel for the summary notification (no sound, no vibration). */
  public static final String SUMMARY_CHANNEL_ID = "NOTIVA_SUMMARY_CHANNEL";

  /** Display name for the summary notification channel. */
  public static final String SUMMARY_CHANNEL_NAME = "Notiva Summary";

  /**
   * Fixed notification ID for the summary notification.
   * <p>
   * Set to {@code Integer.MAX_VALUE - 1} to avoid collision with the foreground
   * service notification (which uses {@code Integer.MAX_VALUE}) and with
   * reminder notification IDs (which are small positive ints from bit-packing).
   */
  public static final int SUMMARY_NOTIFICATION_ID = Integer.MAX_VALUE - 1;

  /** Group key for Android notification grouping of reminder notifications. */
  public static final String NOTIFICATION_GROUP_KEY = "com.ava.notiva.REMINDER_GROUP";

  /** Intent extra key for passing the scheduled fire epoch time (UTC millis). */
  public static final String SCHEDULED_FIRE_EPOCH = "com.ava.notiva.SCHEDULED_FIRE_EPOCH";

  /**
   * Per-reminder collapse threshold. When a single reminder has this many
   * or more active (unacknowledged) notifications, only the latest is kept
   * and the rest are cancelled.
   */
  public static final int PER_REMINDER_COLLAPSE_THRESHOLD = 3;
}
