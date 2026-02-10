package com.ava.notiva;

import static com.ava.notiva.util.ReminderConstants.REMINDER_ACTIVE;
import static com.ava.notiva.util.ReminderConstants.REMINDER_END_TIME;
import static com.ava.notiva.util.ReminderConstants.REMINDER_ID;
import static com.ava.notiva.util.ReminderConstants.REMINDER_NAME;
import static com.ava.notiva.util.ReminderConstants.REMINDER_RECURRENCE_DELAY;
import static com.ava.notiva.util.ReminderConstants.REMINDER_RECURRENCE_TYPE;
import static com.ava.notiva.util.ReminderConstants.REMINDER_START_TIME;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.ava.notiva.adapter.ReminderItemAdapter;
import com.ava.notiva.data.GetAllRemindersViewModel;
import com.ava.notiva.data.ReminderDmlViewModel;
import com.ava.notiva.model.ReminderModel;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

  public static final String TAG = "Notiva.MainActivity";
  @Inject
  ReminderDmlViewModel reminderDml;
  @Inject
  GetAllRemindersViewModel getAllRemindersViewModel;
  private ReminderItemAdapter reminderItemAdapter;
  private RecyclerView reminderRecyclerView;
  private TextView emptyReminderList;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    checkPermissions();

    findViewById(R.id.am_fab_add_reminder).setOnClickListener(view -> startUpsertReminderActivity(null));

    emptyReminderList = findViewById(R.id.am_tv_no_reminders);

    reminderItemAdapter =
        new ReminderItemAdapter(this, reminderDml, this::startUpsertReminderActivity);
    reminderRecyclerView = findViewById(R.id.am_rv_reminders);
    reminderRecyclerView.setAdapter(reminderItemAdapter);
    attachItemClickHelper();
    observeAllReminders();
  }

  private void startUpsertReminderActivity(ReminderModel model) {
    Intent intent = new Intent(this, UpsertReminderActivity.class);
    if (model != null) {
      intent.putExtra(REMINDER_ID, model.getId());
      intent.putExtra(REMINDER_ACTIVE, model.isActive());
      intent.putExtra(REMINDER_NAME, model.getName());
      intent.putExtra(REMINDER_START_TIME, model.getStartDateTime().getTimeInMillis());
      intent.putExtra(REMINDER_RECURRENCE_DELAY, model.getRecurrenceDelay());
      intent.putExtra(REMINDER_RECURRENCE_TYPE, model.getRecurrenceType().toString());
      if (model.getEndDateTime() != null) {
        intent.putExtra(REMINDER_END_TIME, model.getEndDateTime().getTimeInMillis());
      }
    }
    startActivity(intent);
  }

  private void attachItemClickHelper() {
    new ItemTouchHelper(
        new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
          @Override
          public boolean onMove(
              @NonNull RecyclerView recyclerView,
              @NonNull RecyclerView.ViewHolder viewHolder,
              @NonNull RecyclerView.ViewHolder target) {
            return false;
          }

          @Override
          public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            try {
              ReminderModel reminder = reminderItemAdapter.getReminderAt(viewHolder.getBindingAdapterPosition());
              if (reminder != null) {
                Log.i(TAG, "Deleting reminder: ID=" + reminder.getId() + ", Name=" + reminder.getName());
                reminderDml.deleteReminder(reminder);
                Toast.makeText(MainActivity.this, "Deleted reminder: " + (reminder.getName() != null ? reminder.getName() : ""), Toast.LENGTH_SHORT).show();
              }
            } catch (Exception e) {
              Log.e(TAG, "Error deleting reminder", e);
              Toast.makeText(MainActivity.this, "Error deleting reminder", Toast.LENGTH_SHORT).show();
            }
          }
        })
        .attachToRecyclerView(reminderRecyclerView);
  }

  private void observeAllReminders() {
    getAllRemindersViewModel
        .getAllReminders()
        .observe(
            this,
            reminders -> {
              try {
                Log.i(TAG, "All reminders: " + reminders.toString());
                if (reminders.isEmpty()) {
                  reminderRecyclerView.setVisibility(View.GONE);
                  emptyReminderList.setVisibility(View.VISIBLE);
                } else {
                  reminderRecyclerView.setVisibility(View.VISIBLE);
                  emptyReminderList.setVisibility(View.GONE);
                }
                reminderItemAdapter.submitList(reminders);
              } catch (Exception e) {
                Log.e(TAG, "Error observing reminders", e);
                Toast.makeText(this, "Error loading reminders", Toast.LENGTH_SHORT).show();
              }
            });
  }

  private void checkPermissions() {
    checkExactAlarmPermission();
    checkNotificationPermission();
  }

  private void checkExactAlarmPermission() {
    android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(ALARM_SERVICE);
    if (!alarmManager.canScheduleExactAlarms()) {
      Toast.makeText(this, "Exact alarm permission required for reminders.", Toast.LENGTH_LONG).show();
      Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
      startActivity(intent);
    }
  }

  private void checkNotificationPermission() {
    ActivityResultLauncher<String> requestNotificationPermissionLauncher = registerForActivityResult(
        new ActivityResultContracts.RequestPermission(),
        isGranted -> {
          if (!isGranted) {
            Toast.makeText(this, "Notification permission denied. Reminders may not show notifications.", Toast.LENGTH_LONG).show();
          }
        });
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED) {
      requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == R.id.action_settings) {
      startActivity(new Intent(this, SettingsActivity.class));
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }
}
