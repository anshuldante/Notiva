package com.ava.notiva;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String KEY_RINGTONE = "pref_notification_ringtone";
    private static final String KEY_SNOOZE = "pref_snooze_duration";

    private Preference ringtonePreference;
    private ActivityResultLauncher<Intent> ringtonePickerLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Register the launcher before super.onCreate to satisfy lifecycle requirements
        ringtonePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getData() != null) {
                        Uri ringtoneUri = result.getData().getParcelableExtra(
                                RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri.class);
                        SharedPreferences prefs = PreferenceManager
                                .getDefaultSharedPreferences(requireContext());
                        if (ringtoneUri != null) {
                            prefs.edit().putString(KEY_RINGTONE, ringtoneUri.toString()).apply();
                        } else {
                            prefs.edit().putString(KEY_RINGTONE, "").apply();
                        }
                        updateRingtoneSummary();
                    }
                });
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // Ringtone preference: manual click handler to launch system picker
        ringtonePreference = findPreference(KEY_RINGTONE);
        if (ringtonePreference != null) {
            ringtonePreference.setOnPreferenceClickListener(pref -> {
                launchRingtonePicker();
                return true;
            });
            updateRingtoneSummary();
        }

        // Snooze duration: use SimpleSummaryProvider for automatic summary
        ListPreference snoozePreference = findPreference(KEY_SNOOZE);
        if (snoozePreference != null) {
            snoozePreference.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
        }
    }

    private void launchRingtonePicker() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                Settings.System.DEFAULT_NOTIFICATION_URI);

        // Pass existing URI so picker pre-selects current choice
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(requireContext());
        String existingUri = prefs.getString(KEY_RINGTONE, null);
        if (existingUri != null && !existingUri.isEmpty()) {
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                    Uri.parse(existingUri));
        } else {
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
        }

        ringtonePickerLauncher.launch(intent);
    }

    private void updateRingtoneSummary() {
        if (ringtonePreference == null) return;

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(requireContext());
        String uriString = prefs.getString(KEY_RINGTONE, null);

        if (uriString == null || uriString.isEmpty()) {
            ringtonePreference.setSummary(R.string.pref_ringtone_summary_silent);
        } else {
            Ringtone ringtone = RingtoneManager.getRingtone(requireContext(),
                    Uri.parse(uriString));
            if (ringtone != null) {
                ringtonePreference.setSummary(ringtone.getTitle(requireContext()));
            } else {
                ringtonePreference.setSummary(R.string.pref_ringtone_summary_silent);
            }
        }
    }
}
