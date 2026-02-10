package com.ava.notiva.data;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.ava.notiva.model.ReminderModel;

@Database(
    entities = {ReminderModel.class},
    version = 3,
    exportSchema = true)
public abstract class RemindersDb extends RoomDatabase {
  public abstract ReminderDao reminderDao();

  // Migration from version 1 to 2: Add snoozed_until column
  public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
      database.execSQL("ALTER TABLE reminders ADD COLUMN snoozed_until INTEGER");
    }
  };

  // Migration from version 2 to 3: Add tracking columns for missed reminders and ringtone
  public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
      database.execSQL("ALTER TABLE reminders ADD COLUMN last_fired_at INTEGER");
      database.execSQL("ALTER TABLE reminders ADD COLUMN last_acknowledged_at INTEGER");
      database.execSQL("ALTER TABLE reminders ADD COLUMN ringtone_uri TEXT");
    }
  };
}
