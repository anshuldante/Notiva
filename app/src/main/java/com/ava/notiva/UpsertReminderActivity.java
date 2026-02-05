package com.ava.notiva;

import static com.ava.notiva.util.ReminderConstants.REMINDER_ACTIVE;
import static com.ava.notiva.util.ReminderConstants.REMINDER_END_TIME;
import static com.ava.notiva.util.ReminderConstants.REMINDER_ID;
import static com.ava.notiva.util.ReminderConstants.REMINDER_NAME;
import static com.ava.notiva.util.ReminderConstants.REMINDER_RECURRENCE_DELAY;
import static com.ava.notiva.util.ReminderConstants.REMINDER_RECURRENCE_TYPE;
import static com.ava.notiva.util.ReminderConstants.REMINDER_START_TIME;
import static java.util.Calendar.DATE;
import static java.util.Calendar.HOUR;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MILLISECOND;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.SECOND;
import static java.util.Calendar.YEAR;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ava.notiva.data.ReminderDmlViewModel;
import com.ava.notiva.listener.RecurrenceDelayChangedListener;
import com.ava.notiva.listener.RecurrenceTypeListener;
import com.ava.notiva.listener.ReminderNameChangedListener;
import com.ava.notiva.model.RecurrenceType;
import com.ava.notiva.model.ReminderModel;
import com.ava.notiva.util.DateTimeDisplayUtil;
import com.ava.notiva.util.FriendlyDateType;
import com.ava.notiva.util.InputFilterMinMax;
import com.ava.notiva.util.RecurrenceDisplayUtil;
import com.ava.notiva.util.ReminderWorkerUtils;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class UpsertReminderActivity extends AppCompatActivity {
  public static final String TAG = "Notiva.UpsertReminderActivity";

  private final Calendar currentTime = Calendar.getInstance();

  @Inject ReminderDmlViewModel reminderDmlViewModel;

  private ArrayAdapter<CharSequence> spinnerAdapter;
  private ConstraintLayout recurrenceDetailsCl;
  private Spinner recurrenceTypeSpinner;
  private SwitchCompat recurrenceSwitch;
  private ImageView startDateImageView;
  private ReminderModel reminderModel;
  private EditText recurrenceDelayEt;
  private TimePicker startTimePicker;
  private TextView startDateTextView;
  private TextView endDateTextView;
  private TextView endTimeTextView;
  private EditText reminderName;
  private TextView recurrenceSummaryTv;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_upsert_reminder);
    try {
      reminderModel = buildReminderAndSetTitle();
      initComponentMappings();
      initPrimaryComponents();
      initRecurrenceComponents();
    } catch (Exception e) {
      Log.e(TAG, "Error during activity initialization", e);
      Toast.makeText(this, "Initialization error", Toast.LENGTH_SHORT).show();
      finish();
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    try {
      findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());
      findViewById(R.id.btn_save).setOnClickListener(v -> {
        try {
          saveReminder();
        } catch (Exception e) {
          Log.e(TAG, "Error saving reminder", e);
          Toast.makeText(this, "Error saving reminder", Toast.LENGTH_SHORT).show();
        }
      });
    } catch (Exception e) {
      Log.e(TAG, "Error setting up button listeners", e);
      Toast.makeText(this, "Error initializing buttons", Toast.LENGTH_SHORT).show();
    }
  }

  private void saveReminder() {
    if (recurrenceSwitch.isChecked()) {
      String recurrenceNumber = recurrenceDelayEt.getText() == null ? "" : recurrenceDelayEt.getText().toString().trim();
      if (recurrenceNumber.isEmpty()) {
        recurrenceDelayEt.setError("Please enter a recurrence number");
        recurrenceDelayEt.requestFocus();
        return;
      }
    } else {
      reminderModel.setRecurrenceType(RecurrenceType.NEVER);
      reminderModel.setRecurrenceDelay(0);
      reminderModel.setEndDateTime(null);
    }

    if (reminderModel.getId() > 0) {
      reminderDmlViewModel.updateReminder(reminderModel);
      ReminderWorkerUtils.enqueueReminderWorker(this);
      finish();
    } else {
      reminderDmlViewModel.addReminderWithCallback(reminderModel, (newId) -> {
        if (newId > 0) {
          reminderModel.setId(newId.intValue());
          runOnUiThread(() -> {
            ReminderWorkerUtils.enqueueReminderWorker(this);
            finish();
          });
        } else {
          Log.w(TAG, "Reminder not saved, skipping notification scheduling. Invalid ID: " + newId);
          runOnUiThread(this::finish);
        }
      });
    }
  }

  private ReminderModel buildReminderAndSetTitle() {
    Intent intent = getIntent();
    int id = intent.getIntExtra(REMINDER_ID, -1);
    ReminderModel model;
    if (id != -1) {
      model = new ReminderModel(
          id,
          intent.getStringExtra(REMINDER_NAME),
          intent.getBooleanExtra(REMINDER_ACTIVE, false),
          intent.getLongExtra(REMINDER_START_TIME, 0),
          intent.getIntExtra(REMINDER_RECURRENCE_DELAY, 0),
          intent.getStringExtra(REMINDER_RECURRENCE_TYPE),
          intent.getLongExtra(REMINDER_END_TIME, 0));
      if (model.getRecurrenceType() == RecurrenceType.FOREVER) {
        model.setEndDateTime(null);
      }
      return model;
    }
    return new ReminderModel();
  }

  private void initComponentMappings() {
    spinnerAdapter = ArrayAdapter.createFromResource(
        this, R.array.recurrence_type_array, android.R.layout.simple_spinner_item);
    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    recurrenceTypeSpinner = findViewById(R.id.ara_sn_recurrence_type);
    recurrenceDetailsCl = findViewById(R.id.ara_cl_recurrence_delay);
    recurrenceDelayEt = findViewById(R.id.ara_et_recurrence_number);
    recurrenceSwitch = findViewById(R.id.ara_sc_recurring_reminder);
    startDateTextView = findViewById(R.id.ara_display_date);
    startTimePicker = findViewById(R.id.ara_time_picker_primary);
    startDateImageView = findViewById(R.id.ara_iv_calendar);
    reminderName = findViewById(R.id.ara_et_reminder_name);
    endDateTextView = findViewById(R.id.ara_tv_end_date);
    endTimeTextView = findViewById(R.id.ara_tv_end_time);
    recurrenceSummaryTv = findViewById(R.id.ara_tv_recurrence_summary);
  }

  private void initPrimaryComponents() {
    initStartTimeComponents();
    initStartDateComponents();
    initReminderNameComponents();
    initRecurrenceSwitchListener();
  }

  private void initRecurrenceComponents() {
    initRecurrenceTypeSpinner();
    initRecurrenceDelayComponent();
    initEndDateComponents();
    initEndTimeComponents();
  }

  private void initStartTimeComponents() {
    initStartTimeView();

    Calendar startDateTime = reminderModel.getStartDateTime();
    startTimePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
      Calendar now = Calendar.getInstance();
      FriendlyDateType type = DateTimeDisplayUtil.getFriendlyDateType(startDateTime);

      switch (type) {
        case TODAY:
          if (hourOfDay < now.get(Calendar.HOUR_OF_DAY) ||
              (hourOfDay == now.get(Calendar.HOUR_OF_DAY) && minute < now.get(Calendar.MINUTE))) {
            startDateTime.add(Calendar.DATE, 1);
          }
          break;
        case TOMORROW:
          if (hourOfDay > now.get(Calendar.HOUR_OF_DAY) ||
              (hourOfDay == now.get(Calendar.HOUR_OF_DAY) && minute >= now.get(Calendar.MINUTE))) {
            startDateTime.add(Calendar.DATE, -1);
          }
          break;
        default:
          break;
      }

      startDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
      startDateTime.set(Calendar.MINUTE, minute);
      startDateTime.set(Calendar.SECOND, 0);
      startDateTime.set(Calendar.MILLISECOND, 0);

      initStartDateView();
    });
  }

  private void initStartDateComponents() {
    initStartDateView();
    Calendar startDateTime = reminderModel.getStartDateTime();
    startDateImageView.setOnClickListener(view -> showMaterialDatePicker(startDateTime, this::initStartDateView, true));
  }

  private void showMaterialDatePicker(Calendar dateTime, Runnable updateView, boolean isStartDate) {
    CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
    if (isStartDate) {
      constraintsBuilder.setValidator(DateValidatorPointForward.now());
    } else {
      constraintsBuilder.setValidator(DateValidatorPointForward.from(reminderModel.getStartDateTime().getTimeInMillis()));
    }

    MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
        .setTitleText(isStartDate ? "Select Start Date" : "Select End Date")
        .setSelection(dateTime.getTimeInMillis())
        .setCalendarConstraints(constraintsBuilder.build())
        .build();

    datePicker.addOnPositiveButtonClickListener(selection -> {
      // MaterialDatePicker returns UTC midnight timestamps, so we must extract
      // date components in UTC to avoid off-by-one-day errors in negative UTC offsets
      Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
      utcCalendar.setTimeInMillis(selection);

      // Create a local calendar with the UTC date components for comparison
      Calendar selected = Calendar.getInstance();
      selected.set(YEAR, utcCalendar.get(YEAR));
      selected.set(MONTH, utcCalendar.get(MONTH));
      selected.set(DATE, utcCalendar.get(DATE));
      selected.set(HOUR_OF_DAY, 0);
      selected.set(MINUTE, 0);
      selected.set(SECOND, 0);
      selected.set(MILLISECOND, 0);

      currentTime.setTimeInMillis(System.currentTimeMillis());
      if (isStartDate && selected.before(currentTime)) {
        Toast.makeText(this, "Can't setup alarms for a time in the past!", Toast.LENGTH_LONG).show();
      } else if (!isStartDate && selected.before(reminderModel.getStartDateTime())) {
        Toast.makeText(this, "Recurrence date can't be before alarm start date!", Toast.LENGTH_LONG).show();
      } else {
        // Apply UTC date components to the target dateTime (preserving existing time)
        dateTime.set(YEAR, utcCalendar.get(YEAR));
        dateTime.set(MONTH, utcCalendar.get(MONTH));
        dateTime.set(DATE, utcCalendar.get(DATE));
        dateTime.set(SECOND, 0);
        dateTime.set(MILLISECOND, 0);
        updateView.run();
      }
    });
    datePicker.show(getSupportFragmentManager(), isStartDate ? "start_date_picker" : "end_date_picker");
  }

  private void initReminderNameComponents() {
    initReminderNameView();
    reminderName.addTextChangedListener(new ReminderNameChangedListener(reminderModel));
  }

  private void initRecurrenceSwitchListener() {
    initRecurrenceSwitchView();
    recurrenceSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
      if (isChecked) {
        if (reminderModel.getRecurrenceDelay() <= 0) {
          reminderModel.setRecurrenceDelay(1);
          recurrenceDelayEt.setText("1");
        }
        if (reminderModel.getEndDateTime() == null) {
          Calendar start = reminderModel.getStartDateTime();
          RecurrenceType type = reminderModel.getRecurrenceType();
          int delay = reminderModel.getRecurrenceDelay();
          Calendar end = (Calendar) start.clone();
          end.setTimeInMillis(start.getTimeInMillis() + delay * type.getMillis());
          reminderModel.setEndDateTime(end);
        }
      } else {
        reminderModel.setRecurrenceType(RecurrenceType.NEVER);
        reminderModel.setRecurrenceDelay(0);
        reminderModel.setEndDateTime(null);
      }
      recurrenceDetailsCl.setVisibility(isChecked ? View.VISIBLE : View.GONE);
      updateRecurrenceSummary();
    });
  }

  private RecurrenceType getSelectedRecurrenceType() {
    Object selected = recurrenceTypeSpinner.getSelectedItem();
    if (selected instanceof RecurrenceType) {
      return (RecurrenceType) selected;
    } else if (selected != null) {
      return RecurrenceType.getRecurrenceTypeByValue(selected.toString());
    } else {
      return RecurrenceType.NEVER;
    }
  }

  private void setEndDateTimeEnabled(boolean enabled) {
    endDateTextView.setEnabled(enabled);
    endTimeTextView.setEnabled(enabled);
    ImageView endDateImageView = findViewById(R.id.ara_iv_end_calendar);
    ImageView endTimeImageView = findViewById(R.id.ara_iv_end_clock);
    endDateImageView.setEnabled(enabled);
    endTimeImageView.setEnabled(enabled);
    float alpha = enabled ? 1.0f : 0.4f;
    endDateTextView.setAlpha(alpha);
    endTimeTextView.setAlpha(alpha);
    endDateImageView.setAlpha(alpha);
    endTimeImageView.setAlpha(alpha);
  }

  private void recalculateAndSetEndDate() {
    RecurrenceType type = getSelectedRecurrenceType();
    int count = 0;
    try {
      String countStr = recurrenceDelayEt.getText() == null ? "" : recurrenceDelayEt.getText().toString().trim();
      count = Integer.parseInt(countStr);
    } catch (Exception ignored) {
    }
    if (type == RecurrenceType.FOREVER || count <= 0) {
      reminderModel.setEndDateTime(null);
      initEndDateView();
      initEndTimeView();
      return;
    }
    Calendar start = reminderModel.getStartDateTime();
    Calendar end = (Calendar) start.clone();
    end.setTimeInMillis(start.getTimeInMillis() + (long) count * type.getMillis());
    reminderModel.setEndDateTime(end);
    initEndDateView();
    initEndTimeView();
  }

  private void initRecurrenceTypeSpinner() {
    recurrenceTypeSpinner.setAdapter(spinnerAdapter);
    initRecurrenceTypeView();
    recurrenceTypeSpinner.setOnItemSelectedListener(new RecurrenceTypeListener(reminderModel, () -> {
      setEndDateTimeEnabled(getSelectedRecurrenceType() != RecurrenceType.FOREVER);
      recalculateAndSetEndDate();
      updateRecurrenceSummary();
    }));
    setEndDateTimeEnabled(getSelectedRecurrenceType() != RecurrenceType.FOREVER);
  }

  private void initRecurrenceDelayComponent() {
    initRecurrenceDelayView();
    recurrenceDelayEt.setFilters(new InputFilter[]{new InputFilterMinMax(1, 1000)});
    recurrenceDelayEt.addTextChangedListener(new RecurrenceDelayChangedListener(reminderModel, () -> {
      recalculateAndSetEndDate();
      updateRecurrenceSummary();
    }));
  }

  private void initEndDateComponents() {
    initEndDateView();
    ImageView endDateImageView = findViewById(R.id.ara_iv_end_calendar);
    endDateImageView.setOnClickListener(
        view -> {
          Calendar endDateTime = reminderModel.getEndDateTime();
          if (endDateTime == null) {
            endDateTime = Calendar.getInstance();
          }
          showMaterialDatePicker(endDateTime, this::initEndDateView, false);
        });
  }

  private void initEndTimeComponents() {
    initEndTimeView();
    ImageView endTimeImageView = findViewById(R.id.ara_iv_end_clock);
    endTimeImageView.setOnClickListener(
        view -> {
          Calendar endDateTime = reminderModel.getEndDateTime();
          if (endDateTime == null) {
            endDateTime = Calendar.getInstance();
          }
          attachTimePickerDialog(endDateTime, this::initEndTimeView);
        });
  }

  private void attachTimePickerDialog(Calendar dateTime, Runnable runnable) {
    TimePickerDialog tpd =
        new TimePickerDialog(
            this,
            ((view, hourOfDay, minute) -> timeSetListener(view, dateTime, runnable)),
            dateTime.get(HOUR_OF_DAY),
            dateTime.get(MINUTE),
            true);
    Objects.requireNonNull(tpd.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
    tpd.show();
  }

  private void timeSetListener(TimePicker view, Calendar dateTime, Runnable runnable) {
    Log.i(TAG, "View to Time: " + dateTime.get(HOUR) + ":" + dateTime.get(MINUTE));

    currentTime.setTimeInMillis(System.currentTimeMillis());
    int hour = view.getHour();
    int minute = view.getMinute();

    if (dateTime.before(currentTime)) {
      dateTime.set(DATE, dateTime.get(DATE) + 1);
    }

    dateTime.set(HOUR_OF_DAY, hour);
    dateTime.set(MINUTE, minute);
    runnable.run();
  }

  private void initStartTimeView() {
    Calendar startDateTime = reminderModel.getStartDateTime();
    Log.i(
        TAG,
        "Start time to view: " + startDateTime.get(HOUR_OF_DAY) + ":" + startDateTime.get(MINUTE));
    startTimePicker.setHour(startDateTime.get(HOUR_OF_DAY));
    startTimePicker.setMinute(startDateTime.get(MINUTE));
  }

  private void initStartDateView() {
    Calendar startDateTime = reminderModel.getStartDateTime();
    Log.i(
        TAG,
        "Start Date to view: "
            + startDateTime.get(DATE)
            + "/"
            + (startDateTime.get(MONTH) + 1)
            + "/"
            + startDateTime.get(YEAR));
    startDateTextView.setText(DateTimeDisplayUtil.getFriendlyDate(this, startDateTime));
  }

  private void initReminderNameView() {
    reminderName.setText(reminderModel.getName());
  }

  private void initRecurrenceSwitchView() {
    boolean isRepeating = reminderModel.getRecurrenceDelay() > 0;
    recurrenceSwitch.setChecked(isRepeating);
    if (!isRepeating) {
      reminderModel.setRecurrenceType(RecurrenceType.NEVER);
      reminderModel.setRecurrenceDelay(0);
      reminderModel.setEndDateTime(null);
    }
    recurrenceDetailsCl.setVisibility(recurrenceSwitch.isChecked() ? View.VISIBLE : View.GONE);
  }

  private int getSpinnerPositionForRecurrenceType(RecurrenceType type) {
    if (type == null || type == RecurrenceType.NEVER) return 0;
    String[] spinnerValues = getResources().getStringArray(R.array.recurrence_type_array);
    String typeText = type.getValue();
    for (int i = 0; i < spinnerValues.length; i++) {
      if (spinnerValues[i].equals(typeText)) {
        return i;
      }
    }
    Log.w(TAG, "Recurrence type '" + typeText + "' not found in spinner. Defaulting to 'Forever'.");
    return 0;
  }

  private void initRecurrenceTypeView() {
    RecurrenceType recurrenceType = reminderModel.getRecurrenceType();
    int position = getSpinnerPositionForRecurrenceType(recurrenceType);
    recurrenceTypeSpinner.setSelection(position);
    if (recurrenceType == RecurrenceType.NEVER) {
      recurrenceDetailsCl.setVisibility(View.GONE);
    } else {
      recurrenceDetailsCl.setVisibility(View.VISIBLE);
    }
  }

  private void initRecurrenceDelayView() {
    int delay = reminderModel.getRecurrenceDelay();
    if (delay > 0) {
      recurrenceDelayEt.setText(String.valueOf(delay));
    } else {
      recurrenceDelayEt.setText("");
    }
  }

  private void initEndDateView() {
    Calendar dateTime = reminderModel.getEndDateTime();
    if (dateTime == null) {
      endDateTextView.setText(R.string.no_end_date);
      updateRecurrenceSummary();
      return;
    }
    Log.i(
        TAG,
        "End Date to view: "
            + dateTime.get(DATE)
            + "/"
            + (dateTime.get(MONTH) + 1)
            + "/"
            + dateTime.get(YEAR));
    endDateTextView.setText(DateTimeDisplayUtil.getFriendlyDate(this, dateTime));
    updateRecurrenceSummary();
  }

  private void initEndTimeView() {
    Calendar dateTime = reminderModel.getEndDateTime();
    if (dateTime == null) {
      endTimeTextView.setText(R.string.no_end_time);
      updateRecurrenceSummary();
      return;
    }
    Log.i(TAG, "End time to view: " + dateTime.get(HOUR_OF_DAY) + ":" + dateTime.get(MINUTE));
    endTimeTextView.setText(DateTimeDisplayUtil.getFriendlyTime(dateTime));
    updateRecurrenceSummary();
  }

  private void updateRecurrenceSummary() {
    String number = recurrenceDelayEt.getText() == null ? "" : recurrenceDelayEt.getText().toString().trim();
    RecurrenceType type = getSelectedRecurrenceType();
    if (type == RecurrenceType.FOREVER) {
      reminderModel.setRecurrenceType(RecurrenceType.FOREVER);
      reminderModel.setEndDateTime(null);
    }
    String endDate = endDateTextView.getText() != null ? endDateTextView.getText().toString().trim() : "";
    String endTime = endTimeTextView.getText() != null ? endTimeTextView.getText().toString().trim() : "";

    String summary = RecurrenceDisplayUtil.getRecurrenceSummary(
        this,
        number,
        type,
        endDate,
        endTime
    );
    recurrenceSummaryTv.setText(summary);
  }
}
