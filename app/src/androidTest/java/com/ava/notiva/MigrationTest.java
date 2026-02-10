package com.ava.notiva;

import static org.junit.Assert.*;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.room.testing.MigrationTestHelper;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.ava.notiva.data.RemindersDb;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * Instrumented tests for Room database migration from version 2 to 3.
 * Verifies that MIGRATION_2_3 correctly adds tracking columns.
 */
@RunWith(AndroidJUnit4.class)
public class MigrationTest {

    private static final String DB_NAME = "migration-test";

    @Rule
    public MigrationTestHelper helper = new MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            RemindersDb.class
    );

    @Test
    public void migrate2To3_addsTrackingColumns() throws IOException {
        // Create v2 database
        SupportSQLiteDatabase db = helper.createDatabase(DB_NAME, 2);

        // Insert a row with only v2 columns
        ContentValues values = new ContentValues();
        values.put("active", 1);
        values.put("name", "Migration Test");
        values.put("start_date", System.currentTimeMillis());
        values.put("recurrence_delay", 1);
        values.put("recurrence_type", "DAY");
        values.put("end_date", System.currentTimeMillis() + 86400000L);
        values.put("snoozed_until", (Long) null);
        db.insert("reminders", SQLiteDatabase.CONFLICT_REPLACE, values);
        db.close();

        // Run migration 2 -> 3
        db = helper.runMigrationsAndValidate(DB_NAME, 3, true, RemindersDb.MIGRATION_2_3);

        // Verify new columns accept values by inserting a row using them
        ContentValues newRow = new ContentValues();
        newRow.put("active", 1);
        newRow.put("name", "Post-Migration");
        newRow.put("start_date", System.currentTimeMillis());
        newRow.put("recurrence_delay", 2);
        newRow.put("recurrence_type", "HOUR");
        newRow.put("last_fired_at", 123456789L);
        newRow.put("last_acknowledged_at", 987654321L);
        newRow.put("ringtone_uri", "content://media/audio/ringtone/42");
        db.insert("reminders", SQLiteDatabase.CONFLICT_REPLACE, newRow);

        // Query back and verify
        Cursor cursor = db.query("SELECT last_fired_at, last_acknowledged_at, ringtone_uri FROM reminders WHERE name = 'Post-Migration'");
        assertTrue("Should have a result row", cursor.moveToFirst());
        assertEquals(123456789L, cursor.getLong(0));
        assertEquals(987654321L, cursor.getLong(1));
        assertEquals("content://media/audio/ringtone/42", cursor.getString(2));
        cursor.close();

        db.close();
    }

    @Test
    public void migrate2To3_preservesExistingData() throws IOException {
        // Create v2 database and insert a row
        SupportSQLiteDatabase db = helper.createDatabase(DB_NAME, 2);

        long startDate = 1700000000000L;
        long endDate = 1700086400000L;

        ContentValues values = new ContentValues();
        values.put("active", 1);
        values.put("name", "Existing Reminder");
        values.put("start_date", startDate);
        values.put("recurrence_delay", 3);
        values.put("recurrence_type", "MONTH");
        values.put("end_date", endDate);
        values.put("snoozed_until", 1700050000000L);
        db.insert("reminders", SQLiteDatabase.CONFLICT_REPLACE, values);
        db.close();

        // Run migration
        db = helper.runMigrationsAndValidate(DB_NAME, 3, true, RemindersDb.MIGRATION_2_3);

        // Verify old data is intact and new columns are null
        Cursor cursor = db.query("SELECT name, active, start_date, recurrence_delay, recurrence_type, end_date, snoozed_until, last_fired_at, last_acknowledged_at, ringtone_uri FROM reminders");
        assertTrue("Should have the existing row", cursor.moveToFirst());

        assertEquals("Existing Reminder", cursor.getString(0));
        assertEquals(1, cursor.getInt(1));
        assertEquals(startDate, cursor.getLong(2));
        assertEquals(3, cursor.getInt(3));
        assertEquals("MONTH", cursor.getString(4));
        assertEquals(endDate, cursor.getLong(5));
        assertEquals(1700050000000L, cursor.getLong(6));
        assertTrue("last_fired_at should be null", cursor.isNull(7));
        assertTrue("last_acknowledged_at should be null", cursor.isNull(8));
        assertTrue("ringtone_uri should be null", cursor.isNull(9));

        cursor.close();
        db.close();
    }
}
