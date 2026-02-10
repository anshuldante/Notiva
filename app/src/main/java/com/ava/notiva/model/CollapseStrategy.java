package com.ava.notiva.model;

/**
 * Determines how multiple concurrent notifications are displayed.
 */
public enum CollapseStrategy {

  /** Each reminder gets its own notification. */
  STACK,

  /** New notification replaces the previous one. */
  REPLACE,

  /** Only a summary notification is shown. */
  SUMMARY_ONLY
}
