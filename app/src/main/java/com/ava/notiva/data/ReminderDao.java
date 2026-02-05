package com.ava.notiva.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.ava.notiva.model.ReminderModel;

import java.util.List;

@Dao
public interface ReminderDao {

  @Insert
  long add(ReminderModel model);

  @Query("Delete from reminders")
  void deleteAll();

  @Delete
  void delete(ReminderModel model);

  @Query("update reminders set active = :isActive where id = :id")
  void updateStatus(int id, boolean isActive);

  @Update
  void update(ReminderModel model);

  @Query("SELECT * FROM reminders order by name")
  LiveData<List<ReminderModel>> getAll();

  @Query("SELECT * FROM reminders order by name")
  List<ReminderModel> getAllSync();

  @Query("SELECT * FROM reminders where id = :id")
  LiveData<ReminderModel> get(int id);

  @Query("UPDATE reminders SET snoozed_until = :snoozedUntil WHERE id = :id")
  void updateSnoozedUntil(int id, Long snoozedUntil);
}
