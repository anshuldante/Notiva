package com.ava.notiva.model;

/**
 * Determines when notification sound is played during a burst of reminders.
 */
public enum SoundStrategy {

  /** First notification in a burst plays sound; subsequent are silent or vibrate-only. */
  ONCE,

  /** Every notification plays sound. */
  EVERY_FIRE,

  /** Only the summary notification plays sound. */
  SUMMARY_ONLY
}
