package com.ava.notiva.data;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.ava.notiva.model.ReminderModel;

@Database(
    entities = {ReminderModel.class},
    version = 2,
    exportSchema = false)
public abstract class RemindersDb extends RoomDatabase {
  public abstract ReminderDao reminderDao();

  // Migration from version 1 to 2: Add snoozed_until column
  public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
      database.execSQL("ALTER TABLE reminders ADD COLUMN snoozed_until INTEGER");
    }
  };
}
