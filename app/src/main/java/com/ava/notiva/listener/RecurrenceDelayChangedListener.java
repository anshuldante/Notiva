package com.ava.notiva.listener;

import android.text.Editable;
import android.text.TextWatcher;

import com.ava.notiva.model.ReminderModel;

public class RecurrenceDelayChangedListener implements TextWatcher {

  private final ReminderModel reminder;
  private final Runnable summaryUpdater;

  public RecurrenceDelayChangedListener(ReminderModel reminder, Runnable summaryUpdater) {
    this.reminder = reminder;
    this.summaryUpdater = summaryUpdater;
  }

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {
  }

  @Override
  public void onTextChanged(CharSequence s, int start, int before, int count) {
    summaryUpdater.run();
  }

  @Override
  public void afterTextChanged(Editable s) {
    String text = s.toString().trim();
    if (!text.isEmpty()) {
      try {
        reminder.setRecurrenceDelay(Integer.parseInt(text));
      } catch (NumberFormatException ignored) {
        // Invalid input, keep previous value
      }
    }
    summaryUpdater.run();
  }
}
