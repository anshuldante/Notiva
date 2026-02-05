package com.ava.notiva.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.ava.notiva.converter.DbTypeConverters;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@Entity(tableName = "reminders")
@TypeConverters(DbTypeConverters.class)
public class ReminderModel {

  @PrimaryKey(autoGenerate = true)
  private int id;

  private boolean active;

  private String name;

  @ColumnInfo(name = "start_date")
  private Calendar startDateTime;

  @ColumnInfo(name = "recurrence_delay")
  private int recurrenceDelay;

  @ColumnInfo(name = "recurrence_type")
  private RecurrenceType recurrenceType;

  @ColumnInfo(name = "end_date")
  private Calendar endDateTime;

  @ColumnInfo(name = "snoozed_until")
  private Long snoozedUntil;  // Timestamp until which reminder is snoozed, null = not snoozed

  public ReminderModel() {
    this.active = true;
    this.recurrenceType = RecurrenceType.DAY;
    this.endDateTime = null;  // Only set when recurrence end is explicitly configured
    this.startDateTime = Calendar.getInstance();
  }

  @Ignore
  public ReminderModel(String name) {
    this();
    this.name = name;
  }

  @Ignore
  public ReminderModel(
      int id,
      String name,
      boolean active,
      long startTime,
      Integer recurrenceDelay,
      String recurrenceType,
      long endTime) {
    this.id = id;
    this.name = name;
    this.active = active;
    this.startDateTime = Calendar.getInstance();
    this.startDateTime.setTime(new Date(startTime));
    this.recurrenceDelay = recurrenceDelay;
    this.recurrenceType = RecurrenceType.valueOf(recurrenceType);
    this.endDateTime = Calendar.getInstance();
    this.endDateTime.setTime(new Date(endTime));
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getRecurrenceDelay() {
    return recurrenceDelay;
  }

  public void setRecurrenceDelay(int recurrenceDelay) {
    this.recurrenceDelay = recurrenceDelay;
  }

  public RecurrenceType getRecurrenceType() {
    return recurrenceType;
  }

  public void setRecurrenceType(RecurrenceType recurrenceType) {
    this.recurrenceType = recurrenceType;
  }

  public Calendar getStartDateTime() {
    return startDateTime;
  }

  public void setStartDateTime(Calendar startDateTime) {
    this.startDateTime = startDateTime;
  }

  public Calendar getEndDateTime() {
    return endDateTime;
  }

  public void setEndDateTime(Calendar endDateTime) {
    this.endDateTime = endDateTime;
  }

  public Long getSnoozedUntil() {
    return snoozedUntil;
  }

  public void setSnoozedUntil(Long snoozedUntil) {
    this.snoozedUntil = snoozedUntil;
  }

  public boolean isSnoozed() {
    return snoozedUntil != null && snoozedUntil > System.currentTimeMillis();
  }

  public Calendar getNextOccurrenceAfter(Calendar now) {
    if (startDateTime == null) {
      return null;
    }
    // NEVER/FOREVER have getMillis()=0, treat as one-time reminders
    if (recurrenceType == RecurrenceType.NEVER || recurrenceType == RecurrenceType.FOREVER || recurrenceDelay <= 0) {
      return startDateTime.after(now) ? (Calendar) startDateTime.clone() : null;
    }

    Calendar next = (Calendar) startDateTime.clone();
    Calendar end = recurrenceType == RecurrenceType.FOREVER ? null : endDateTime;

    // For MONTH/YEAR, use Calendar.add() to handle variable-length periods correctly
    // For MINUTE/HOUR/DAY, use millisecond math (fixed-length periods)
    if (recurrenceType == RecurrenceType.MONTH || recurrenceType == RecurrenceType.YEAR) {
      int calendarField = recurrenceType == RecurrenceType.MONTH ? Calendar.MONTH : Calendar.YEAR;
      // Iterate forward until we find the next occurrence after 'now'
      while (!next.after(now)) {
        next.add(calendarField, recurrenceDelay);
        // Safety check to prevent infinite loop if end date is in the past
        if (end != null && next.after(end)) {
          return null;
        }
      }
    } else {
      // MINUTE, HOUR, DAY - use fixed millisecond intervals
      long startMillis = startDateTime.getTimeInMillis();
      long nowMillis = now.getTimeInMillis();
      long interval = recurrenceType.getMillis() * recurrenceDelay;
      if (startMillis > nowMillis) {
        next.setTimeInMillis(startMillis);
      } else {
        long intervalsPassed = (nowMillis - startMillis) / interval;
        long nextMillis = startMillis + (intervalsPassed + 1) * interval;
        next.setTimeInMillis(nextMillis);
      }
    }

    if (end != null && next.after(end)) {
      return null;
    }
    return next;
  }

  @NonNull
  @Override
  public String toString() {
    return "ReminderDetails{"
        + "id='" + id + '\''
        + ", active=" + active
        + ", name='" + name + '\''
        + ", startDateTime=" + (startDateTime != null ? startDateTime.getTime() : "null")
        + ", recurrenceDelay=" + recurrenceDelay
        + ", recurrenceType=" + recurrenceType
        + ", endDateTime=" + (endDateTime != null ? endDateTime.getTime() : "null")
        + ", snoozedUntil=" + snoozedUntil
        + '}';
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ReminderModel that = (ReminderModel) o;
    return id == that.id
        && active == that.active
        && recurrenceDelay == that.recurrenceDelay
        && Objects.equals(name, that.name)
        && Objects.equals(startDateTime, that.startDateTime)
        && recurrenceType == that.recurrenceType
        && Objects.equals(endDateTime, that.endDateTime)
        && Objects.equals(snoozedUntil, that.snoozedUntil);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id, active, name, startDateTime, recurrenceDelay, recurrenceType, endDateTime, snoozedUntil);
  }
}
