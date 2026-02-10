package com.ava.notiva.util;

import static com.ava.notiva.util.ReminderConstants.NOTIFICATION_GROUP_KEY;
import static com.ava.notiva.util.ReminderConstants.PER_REMINDER_COLLAPSE_THRESHOLD;
import static com.ava.notiva.util.ReminderConstants.SUMMARY_CHANNEL_ID;
import static com.ava.notiva.util.ReminderConstants.SUMMARY_CHANNEL_NAME;
import static com.ava.notiva.util.ReminderConstants.SUMMARY_NOTIFICATION_ID;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.ava.notiva.MainActivity;
import com.ava.notiva.R;
import com.ava.notiva.model.NotificationPolicy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the summary notification and collapse logic for reminder notifications.
 *
 * <p>This utility is called after every {@code notify()} or {@code cancel()} to
 * ensure the summary notification and per-reminder collapse state stay consistent.
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Count active reminder notifications (excluding foreground service and summary)</li>
 *   <li>Per-reminder collapse: when 3+ active from the same reminder, keep only the latest</li>
 *   <li>Global max collapse: when 5+ total active, aggressively collapse any reminder with 2+</li>
 *   <li>Post/update InboxStyle summary when 3+ active notifications</li>
 *   <li>Remove summary and group key when count drops below 3</li>
 * </ul>
 */
public final class NotificationGroupManager {

    private static final String TAG = "Notiva.NotificationGroupManager";

    /** Foreground service notification ID (matches NotificationStarterService). */
    private static final int FOREGROUND_NOTIFICATION_ID = Integer.MAX_VALUE;

    /** Threshold for showing the summary notification. */
    private static final int SUMMARY_THRESHOLD = 3;

    private NotificationGroupManager() {
        // Utility class, no instantiation
    }

    /**
     * Main entry point: updates the summary notification and applies collapse logic.
     *
     * <p>Call this after every {@code notify()} or {@code cancel()} on a reminder
     * notification to keep grouping state consistent.
     *
     * @param context  application or service context
     * @param manager  the platform NotificationManager (not NotificationManagerCompat)
     */
    public static void updateSummaryAndCollapse(Context context, NotificationManager manager) {
        // Step 1: Get current active reminder notifications
        StatusBarNotification[] active = getActiveReminderNotifications(manager);
        Log.i(TAG, "Active reminder notifications: " + active.length);

        // Step 2: Collapse frequent reminders (per-reminder and global max)
        int collapsed = collapseFrequentReminders(manager, active);

        // Step 3: Re-count after collapse
        active = getActiveReminderNotifications(manager);
        Log.i(TAG, "Active after collapse: " + active.length + " (collapsed " + collapsed + ")");

        // Step 4: Post or remove summary based on count
        if (active.length >= SUMMARY_THRESHOLD) {
            postSummary(context, manager, active, collapsed);
        } else {
            manager.cancel(SUMMARY_NOTIFICATION_ID);
            Log.i(TAG, "Summary removed (count below threshold)");

            // Re-issue remaining notifications without group key so they stand alone
            if (active.length > 0) {
                reissueWithoutGroup(context, manager, active);
            }
        }
    }

    /**
     * Filters active notifications to include only reminder notifications.
     *
     * <p>Excludes the foreground service notification and the summary notification.
     *
     * @param manager the platform NotificationManager
     * @return array of active reminder notifications only
     */
    private static StatusBarNotification[] getActiveReminderNotifications(
            NotificationManager manager) {
        StatusBarNotification[] all = manager.getActiveNotifications();
        List<StatusBarNotification> reminders = new ArrayList<>();

        for (StatusBarNotification sbn : all) {
            int id = sbn.getId();
            if (id != FOREGROUND_NOTIFICATION_ID && id != SUMMARY_NOTIFICATION_ID) {
                reminders.add(sbn);
            }
        }

        return reminders.toArray(new StatusBarNotification[0]);
    }

    /**
     * Enforces per-reminder and global max collapse rules.
     *
     * <p>Per-reminder: when a single reminder has {@code PER_REMINDER_COLLAPSE_THRESHOLD}
     * or more active notifications, only the one with the highest notification ID
     * (latest epoch) is kept.
     *
     * <p>Global max: when total active count exceeds
     * {@link NotificationPolicy#getMaxActiveNotifications()}, any reminder with 2+
     * active notifications is collapsed to just the latest.
     *
     * @param manager the platform NotificationManager
     * @param active  current active reminder notifications
     * @return the number of notifications that were collapsed (cancelled)
     */
    private static int collapseFrequentReminders(NotificationManager manager,
                                                  StatusBarNotification[] active) {
        // Group notifications by reminderId
        Map<Integer, List<StatusBarNotification>> byReminder = new HashMap<>();
        for (StatusBarNotification sbn : active) {
            int reminderId = NotificationIdGenerator.extractReminderId(sbn.getId());
            byReminder.computeIfAbsent(reminderId, k -> new ArrayList<>()).add(sbn);
        }

        int totalCollapsed = 0;

        // Per-reminder collapse: 3+ from same reminder -> keep only latest
        for (Map.Entry<Integer, List<StatusBarNotification>> entry : byReminder.entrySet()) {
            List<StatusBarNotification> group = entry.getValue();
            if (group.size() >= PER_REMINDER_COLLAPSE_THRESHOLD) {
                totalCollapsed += collapseToLatest(manager, group);
            }
        }

        // Global max collapse: if still over max, aggressively collapse any with 2+
        int globalMax = NotificationPolicy.defaults().getMaxActiveNotifications();
        if (active.length - totalCollapsed > globalMax) {
            // Re-scan after per-reminder collapse
            StatusBarNotification[] remaining = getActiveReminderNotifications(manager);
            Map<Integer, List<StatusBarNotification>> remainByReminder = new HashMap<>();
            for (StatusBarNotification sbn : remaining) {
                int reminderId = NotificationIdGenerator.extractReminderId(sbn.getId());
                remainByReminder.computeIfAbsent(reminderId, k -> new ArrayList<>()).add(sbn);
            }

            for (Map.Entry<Integer, List<StatusBarNotification>> entry
                    : remainByReminder.entrySet()) {
                List<StatusBarNotification> group = entry.getValue();
                if (group.size() >= 2) {
                    totalCollapsed += collapseToLatest(manager, group);
                }
            }
            Log.i(TAG, "Aggressive collapse applied (global max " + globalMax + " exceeded)");
        }

        return totalCollapsed;
    }

    /**
     * Keeps only the notification with the highest ID (latest epoch) from the group,
     * cancelling all others.
     *
     * @param manager the platform NotificationManager
     * @param group   notifications from the same reminder
     * @return number of notifications cancelled
     */
    private static int collapseToLatest(NotificationManager manager,
                                         List<StatusBarNotification> group) {
        // Find the notification with the highest ID (latest epoch in lower bits)
        StatusBarNotification latest = group.get(0);
        for (StatusBarNotification sbn : group) {
            if (sbn.getId() > latest.getId()) {
                latest = sbn;
            }
        }

        int cancelled = 0;
        for (StatusBarNotification sbn : group) {
            if (sbn.getId() != latest.getId()) {
                manager.cancel(sbn.getId());
                Log.i(TAG, "Collapsed notification ID " + sbn.getId()
                        + " (reminderId=" + NotificationIdGenerator.extractReminderId(sbn.getId())
                        + ")");
                cancelled++;
            }
        }
        return cancelled;
    }

    /**
     * Builds and posts the InboxStyle summary notification.
     *
     * <p>The summary notification uses a separate silent channel, lists each
     * active reminder by name, and notes how many older fires were collapsed.
     *
     * @param context   application or service context
     * @param manager   the platform NotificationManager
     * @param active    current active reminder notifications (after collapse)
     * @param collapsed number of notifications that were collapsed
     */
    private static void postSummary(Context context, NotificationManager manager,
                                     StatusBarNotification[] active, int collapsed) {
        // Ensure summary channel exists (silent, low importance)
        NotificationChannel summaryChannel = new NotificationChannel(
                SUMMARY_CHANNEL_ID,
                SUMMARY_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW);
        summaryChannel.setSound(null, null);
        summaryChannel.enableVibration(false);
        summaryChannel.setDescription("Groups multiple reminder notifications together");
        manager.createNotificationChannel(summaryChannel);

        // Build InboxStyle with each active reminder's name
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        for (StatusBarNotification sbn : active) {
            CharSequence title = sbn.getNotification().extras
                    .getCharSequence(Notification.EXTRA_TITLE);
            if (title != null) {
                inboxStyle.addLine(title);
            }
        }

        // Note collapsed fires if any
        if (collapsed > 0) {
            inboxStyle.addLine(collapsed + " older fire" + (collapsed == 1 ? "" : "s")
                    + " collapsed");
        }

        inboxStyle.setBigContentTitle(active.length + " reminders active");

        // Content intent: open MainActivity
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentPending = PendingIntent.getActivity(
                context,
                0,
                mainIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification summary = new NotificationCompat.Builder(context, SUMMARY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle(active.length + " reminders active")
                .setStyle(inboxStyle)
                .setGroup(NOTIFICATION_GROUP_KEY)
                .setGroupSummary(true)
                .setAutoCancel(false)
                .setContentIntent(contentPending)
                .build();

        manager.notify(SUMMARY_NOTIFICATION_ID, summary);
        Log.i(TAG, "Summary posted: " + active.length + " reminders active"
                + (collapsed > 0 ? " (" + collapsed + " collapsed)" : ""));
    }

    /**
     * Re-issues remaining notifications without the group key.
     *
     * <p>When the active count drops below the summary threshold, individual
     * notifications should stand fully alone (not grouped). This method recovers
     * each notification's builder, removes the group key, and re-posts it.
     *
     * @param context  application or service context
     * @param manager  the platform NotificationManager
     * @param active   remaining active reminder notifications
     */
    private static void reissueWithoutGroup(Context context, NotificationManager manager,
                                             StatusBarNotification[] active) {
        for (StatusBarNotification sbn : active) {
            try {
                Notification.Builder recovered = Notification.Builder.recoverBuilder(
                        context, sbn.getNotification());
                recovered.setGroup(null);
                manager.notify(sbn.getId(), recovered.build());
                Log.i(TAG, "Re-issued notification ID " + sbn.getId() + " without group key");
            } catch (Exception e) {
                Log.w(TAG, "Failed to re-issue notification ID " + sbn.getId()
                        + " without group: " + e.getMessage());
            }
        }
    }
}
