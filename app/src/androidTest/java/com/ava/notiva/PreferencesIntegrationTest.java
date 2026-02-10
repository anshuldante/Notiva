package com.ava.notiva;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

/**
 * Integration tests for SharedPreferences defaults and persistence.
 * Verifies preference key correctness, default values from XML, read/write operations,
 * and resource array consistency for snooze duration options.
 */
@RunWith(AndroidJUnit4.class)
public class PreferencesIntegrationTest {

    private static final String KEY_RINGTONE = "pref_notification_ringtone";
    private static final String KEY_VIBRATION = "pref_notification_vibration";
    private static final String KEY_SNOOZE = "pref_snooze_duration";

    private Context context;
    private SharedPreferences prefs;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().clear().commit();
        // Apply XML defaults to SharedPreferences (simulates what PreferenceFragmentCompat does)
        PreferenceManager.setDefaultValues(context, R.xml.preferences, true);
    }

    // ==================== Default Values ====================

    @Test
    public void defaultRingtone_isNotSet() {
        // Ringtone uses a plain Preference (no android:defaultValue in XML),
        // so setDefaultValues does not create an entry for it.
        assertFalse("Ringtone key should not exist by default",
                prefs.contains(KEY_RINGTONE));
    }

    @Test
    public void defaultVibration_isTrue() {
        assertTrue("Vibration should default to true",
                prefs.getBoolean(KEY_VIBRATION, false));
    }

    @Test
    public void defaultSnoozeDuration_isTen() {
        assertEquals("Snooze duration should default to 10",
                "10", prefs.getString(KEY_SNOOZE, null));
    }

    // ==================== Persistence ====================

    @Test
    public void setRingtoneUri_persistsCorrectly() {
        String testUri = "content://media/internal/audio/media/42";
        prefs.edit().putString(KEY_RINGTONE, testUri).commit();
        assertEquals(testUri, prefs.getString(KEY_RINGTONE, null));
    }

    @Test
    public void clearRingtoneUri_setsEmptyString() {
        prefs.edit().putString(KEY_RINGTONE, "content://some/ringtone").commit();
        prefs.edit().putString(KEY_RINGTONE, "").commit();
        assertEquals("", prefs.getString(KEY_RINGTONE, null));
    }

    @Test
    public void setVibrationFalse_persists() {
        prefs.edit().putBoolean(KEY_VIBRATION, false).commit();
        assertFalse(prefs.getBoolean(KEY_VIBRATION, true));
    }

    @Test
    public void setSnoozeDuration_persists() {
        prefs.edit().putString(KEY_SNOOZE, "30").commit();
        assertEquals("30", prefs.getString(KEY_SNOOZE, null));
    }

    @Test
    public void setRingtoneToNull_removesKey() {
        prefs.edit().putString(KEY_RINGTONE, "content://some/ringtone").commit();
        prefs.edit().remove(KEY_RINGTONE).commit();
        assertNull(prefs.getString(KEY_RINGTONE, null));
    }

    // ==================== Snooze Duration Array Resources ====================

    @Test
    public void snoozeDurationEntries_haveFiveOptions() {
        String[] entries = context.getResources()
                .getStringArray(R.array.pref_snooze_duration_entries);
        assertEquals("Should have 5 snooze duration entries", 5, entries.length);
    }

    @Test
    public void snoozeDurationValues_haveFiveOptions() {
        String[] values = context.getResources()
                .getStringArray(R.array.pref_snooze_duration_values);
        assertEquals("Should have 5 snooze duration values", 5, values.length);
    }

    @Test
    public void snoozeDurationEntries_andValues_haveSameCount() {
        String[] entries = context.getResources()
                .getStringArray(R.array.pref_snooze_duration_entries);
        String[] values = context.getResources()
                .getStringArray(R.array.pref_snooze_duration_values);
        assertEquals("Entries and values arrays must have same length",
                entries.length, values.length);
    }

    @Test
    public void snoozeDurationValues_areAllPositiveIntegers() {
        String[] values = context.getResources()
                .getStringArray(R.array.pref_snooze_duration_values);
        for (String value : values) {
            try {
                int intValue = Integer.parseInt(value);
                assertTrue("Value should be positive: " + value, intValue > 0);
            } catch (NumberFormatException e) {
                fail("Snooze duration value '" + value + "' is not a valid integer");
            }
        }
    }

    @Test
    public void snoozeDurationValues_containExpectedOptions() {
        String[] values = context.getResources()
                .getStringArray(R.array.pref_snooze_duration_values);
        List<String> valueList = Arrays.asList(values);
        assertTrue("Should contain 5", valueList.contains("5"));
        assertTrue("Should contain 10", valueList.contains("10"));
        assertTrue("Should contain 15", valueList.contains("15"));
        assertTrue("Should contain 30", valueList.contains("30"));
        assertTrue("Should contain 60", valueList.contains("60"));
    }

    @Test
    public void defaultSnoozeDuration_existsInValuesArray() {
        String defaultValue = context.getResources()
                .getString(R.string.pref_snooze_duration_default);
        String[] values = context.getResources()
                .getStringArray(R.array.pref_snooze_duration_values);
        assertTrue("Default snooze value '" + defaultValue + "' should be in values array",
                Arrays.asList(values).contains(defaultValue));
    }
}
