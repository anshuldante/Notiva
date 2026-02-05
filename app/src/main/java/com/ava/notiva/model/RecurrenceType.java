package com.ava.notiva.model;

import java.util.HashMap;
import java.util.Map;

public enum RecurrenceType {
  YEAR("Year(s)"),
  MONTH("Month(s)"),
  DAY("Day(s)"),
  HOUR("Hour(s)"),
  MINUTE("Minute(s)"),
  FOREVER("Forever"),
  NEVER("Never");

  private static final Map<String, RecurrenceType> ENUM_MAP;

  static {
    ENUM_MAP = new HashMap<>();
    ENUM_MAP.put(YEAR.getValue(), YEAR);
    ENUM_MAP.put(MONTH.getValue(), MONTH);
    ENUM_MAP.put(DAY.getValue(), DAY);
    ENUM_MAP.put(HOUR.getValue(), HOUR);
    ENUM_MAP.put(MINUTE.getValue(), MINUTE);
    ENUM_MAP.put(FOREVER.getValue(), FOREVER);
    ENUM_MAP.put(NEVER.getValue(), NEVER);
  }

  private final String value;

  RecurrenceType(String value) {
    this.value = value;
  }

  public static RecurrenceType getRecurrenceTypeByValue(String value) {
    return ENUM_MAP.get(value);
  }

  public String getValue() {
    return value;
  }

  public long getMillis() {
    return switch (this) {
      case MINUTE -> 60_000L;
      case HOUR -> 3_600_000L;
      case DAY -> 86_400_000L;
      case MONTH -> 2_678_400_000L; // 31 days
      case YEAR -> 31_622_400_000L; // 366 days
      default -> 0L;
    };
  }
}
