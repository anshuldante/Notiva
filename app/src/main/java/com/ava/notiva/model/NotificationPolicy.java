package com.ava.notiva.model;

/**
 * Immutable data class governing how multiple concurrent notifications behave.
 * <p>
 * Controls collapse strategy (how notifications are grouped), sound strategy
 * (when sound plays during a burst), and the maximum number of active
 * notifications before the collapse strategy takes effect.
 */
public final class NotificationPolicy {

  private final CollapseStrategy collapseStrategy;
  private final SoundStrategy soundStrategy;
  private final int maxActiveNotifications;

  public NotificationPolicy(CollapseStrategy collapseStrategy,
                            SoundStrategy soundStrategy,
                            int maxActiveNotifications) {
    this.collapseStrategy = collapseStrategy;
    this.soundStrategy = soundStrategy;
    this.maxActiveNotifications = maxActiveNotifications;
  }

  /**
   * Returns the default notification policy: STACK collapse, ONCE sound, 5 max active.
   */
  public static NotificationPolicy defaults() {
    return new NotificationPolicy(CollapseStrategy.STACK, SoundStrategy.ONCE, 5);
  }

  public CollapseStrategy getCollapseStrategy() {
    return collapseStrategy;
  }

  public SoundStrategy getSoundStrategy() {
    return soundStrategy;
  }

  public int getMaxActiveNotifications() {
    return maxActiveNotifications;
  }

  @Override
  public String toString() {
    return "NotificationPolicy{"
        + "collapseStrategy=" + collapseStrategy
        + ", soundStrategy=" + soundStrategy
        + ", maxActiveNotifications=" + maxActiveNotifications
        + '}';
  }
}
