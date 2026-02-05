package com.ava.notiva.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ava.notiva.R;
import com.ava.notiva.data.ReminderDmlViewModel;
import com.ava.notiva.model.RecurrenceType;
import com.ava.notiva.model.ReminderModel;
import com.ava.notiva.util.DateTimeDisplayUtil;
import com.ava.notiva.util.RecurrenceDisplayUtil;

import java.util.Calendar;

public class ReminderItemAdapter
    extends ListAdapter<ReminderModel, ReminderItemAdapter.ReminderItemViewHolder> {

  private static final ReminderDiffCallback DIFF_CALLBACK = new ReminderDiffCallback();
  private final Context context;
  private final ReminderDmlViewModel dmlViewModel;
  private final ReminderItemClickListener itemClickListener;

  public ReminderItemAdapter(
      Context context,
      ReminderDmlViewModel dmlViewModel,
      ReminderItemClickListener itemClickListener) {
    super(DIFF_CALLBACK);
    this.context = context;
    this.dmlViewModel = dmlViewModel;
    this.itemClickListener = itemClickListener;
  }

  @NonNull
  @Override
  public ReminderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ReminderItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_reminder, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ReminderItemViewHolder holder, int position) {
    ReminderModel reminder = getItem(position);
    holder.reminder = reminder;

    String name = reminder.getName();
    if (name == null || name.trim().isEmpty()) {
      holder.alarmName.setVisibility(View.GONE);
    } else {
      holder.alarmName.setVisibility(View.VISIBLE);
      holder.alarmName.setText(name);
    }

    // Use getNextOccurrenceAfter() to show the actual next trigger time, not the original start
    Calendar nextOccurrence = reminder.getNextOccurrenceAfter(Calendar.getInstance());
    if (nextOccurrence == null) {
      // Fallback for non-recurring or expired reminders
      nextOccurrence = reminder.getStartDateTime();
    }
    String nextOccurrenceStr = DateTimeDisplayUtil.getFriendlyDateTimeSingleLine(context, nextOccurrence);
    holder.nextOccurrence.setText(nextOccurrenceStr);

    boolean recurrenceEnabled = reminder.getRecurrenceDelay() > 0
        && reminder.getRecurrenceType() != RecurrenceType.NEVER;

    if (recurrenceEnabled) {
      String number = String.valueOf(reminder.getRecurrenceDelay());
      RecurrenceType type = reminder.getRecurrenceType();
      String endDate = reminder.getEndDateTime() != null ? DateTimeDisplayUtil.getFriendlyDate(context, reminder.getEndDateTime()) : "";
      String endTime = reminder.getEndDateTime() != null ? DateTimeDisplayUtil.getFriendlyTime(reminder.getEndDateTime()) : "";

      String summary = RecurrenceDisplayUtil.getRecurrenceSummary(
          context,
          number,
          type,
          endDate,
          endTime
      );
      holder.summary.setVisibility(View.VISIBLE);
      holder.summary.setText(summary);
    } else {
      holder.summary.setVisibility(View.GONE);
    }

    holder.activeSwitch.setChecked(reminder.isActive());

    if (isReminderDisabledOrExpired(reminder)) {
      holder.itemView.setAlpha(0.5f);
      holder.alarmName.setTextColor(ContextCompat.getColor(context, R.color.gray));
      holder.alarmName.setPaintFlags(holder.alarmName.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
    } else {
      holder.itemView.setAlpha(1.0f);
      holder.alarmName.setTextColor(ContextCompat.getColor(context, R.color.primary_text));
      holder.alarmName.setPaintFlags(holder.alarmName.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
    }
  }

  private boolean isReminderDisabledOrExpired(ReminderModel reminder) {
    boolean isDisabled = !reminder.isActive();
    boolean isExpired = reminder.getEndDateTime() != null && reminder.getEndDateTime().before(Calendar.getInstance());
    return isDisabled || isExpired;
  }

  public ReminderModel getReminderAt(int position) {
    return getItem(position);
  }

  public interface ReminderItemClickListener {
    void onItemClick(ReminderModel reminderAt);
  }

  public class ReminderItemViewHolder extends RecyclerView.ViewHolder {

    private final TextView alarmName;
    private final TextView nextOccurrence;
    private final TextView summary;
    private final SwitchCompat activeSwitch;

    private ReminderModel reminder;

    public ReminderItemViewHolder(View itemView) {
      super(itemView);
      alarmName = itemView.findViewById(R.id.rir_tv_alarm_name);
      nextOccurrence = itemView.findViewById(R.id.rir_tv_next_occurrence);
      summary = itemView.findViewById(R.id.rir_tv_summary);
      activeSwitch = itemView.findViewById(R.id.rir_sw_active);

      activeSwitch.setOnCheckedChangeListener(this::toggleReminderStatus);
      itemView.setOnClickListener(this::openReminderEditor);
    }

    private void openReminderEditor(View view) {
      int position = getBindingAdapterPosition();
      if (itemClickListener != null && position != RecyclerView.NO_POSITION) {
        itemClickListener.onItemClick(getReminderAt(position));
      }
    }

    private void toggleReminderStatus(CompoundButton buttonView, boolean isChecked) {
      if (reminder != null && isChecked != reminder.isActive()) {
        dmlViewModel.updateReminderStatus(reminder, isChecked);
      }
    }
  }
}
