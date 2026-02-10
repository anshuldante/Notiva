package com.ava.notiva;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;

/**
 * Instrumented tests for {@link SettingsActivity} and {@link SettingsFragment}.
 * Verifies toolbar, fragment loading, preference display, defaults, and toggle behavior.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4.class)
public class SettingsActivityTest {

    @Rule
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit();
    }

    // ==================== Activity Launch ====================

    @Test
    public void settingsActivity_launchesSuccessfully() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    // ==================== Toolbar ====================

    @Test
    public void settingsActivity_displaysToolbar() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            onView(withId(R.id.settings_toolbar)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void settingsActivity_toolbarShowsSettingsTitle() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            onView(withText(R.string.title_activity_settings)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void settingsActivity_toolbarHasNavigationIcon() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            scenario.onActivity(activity -> {
                Toolbar toolbar = activity.findViewById(R.id.settings_toolbar);
                assertNotNull("Navigation icon (back arrow) should be set",
                        toolbar.getNavigationIcon());
            });
        }
    }

    @Test
    public void settingsActivity_hasActionBar() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            scenario.onActivity(activity ->
                    assertNotNull("ActionBar should be set",
                            activity.getSupportActionBar()));
        }
    }

    // ==================== Fragment Loading ====================

    @Test
    public void settingsActivity_loadsSettingsFragment() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            scenario.onActivity(activity -> {
                assertNotNull("Fragment container should have a fragment",
                        activity.getSupportFragmentManager()
                                .findFragmentById(R.id.settings_container));
                assertTrue("Fragment should be SettingsFragment",
                        activity.getSupportFragmentManager()
                                .findFragmentById(R.id.settings_container)
                                instanceof SettingsFragment);
            });
        }
    }

    // ==================== Preference Display ====================

    @Test
    public void settingsActivity_displaysNotificationsCategory() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            onView(withText(R.string.pref_category_notifications))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void settingsActivity_displaysRingtonePreference() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            onView(withText(R.string.pref_ringtone_title))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void settingsActivity_displaysVibrationPreference() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            onView(withText(R.string.pref_vibration_title))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void settingsActivity_displaysSnoozeDurationPreference() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            onView(withText(R.string.pref_snooze_duration_title))
                    .check(matches(isDisplayed()));
        }
    }

    // ==================== Default Summaries ====================

    @Test
    public void settingsActivity_ringtoneDefaultSummaryIsSilent() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            onView(withText(R.string.pref_ringtone_summary_silent))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void settingsActivity_vibrationDefaultSummaryIsOn() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            onView(withText(R.string.pref_vibration_summary_on))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void settingsActivity_snoozeDurationShowsDefaultSummary() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            onView(withText("10 minutes")).check(matches(isDisplayed()));
        }
    }

    // ==================== Vibration Toggle ====================

    @Test
    public void settingsActivity_toggleVibrationOff_showsOffSummary() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            onView(withText(R.string.pref_vibration_title)).perform(click());
            onView(withText(R.string.pref_vibration_summary_off))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void settingsActivity_toggleVibrationOffThenOn_restoresOnSummary() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            onView(withText(R.string.pref_vibration_title)).perform(click());
            onView(withText(R.string.pref_vibration_summary_off))
                    .check(matches(isDisplayed()));
            onView(withText(R.string.pref_vibration_title)).perform(click());
            onView(withText(R.string.pref_vibration_summary_on))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void settingsActivity_toggleVibration_persistsToSharedPreferences() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            onView(withText(R.string.pref_vibration_title)).perform(click());
            scenario.onActivity(activity -> {
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(activity);
                assertFalse("Vibration should be false after toggling off",
                        prefs.getBoolean("pref_notification_vibration", true));
            });
        }
    }

    // ==================== Snooze Duration Dialog ====================

    @Test
    public void settingsActivity_clickSnoozeDuration_opensDialogWithAllOptions() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            onView(withText(R.string.pref_snooze_duration_title)).perform(click());
            onView(withText("5 minutes")).check(matches(isDisplayed()));
            onView(withText("10 minutes")).check(matches(isDisplayed()));
            onView(withText("15 minutes")).check(matches(isDisplayed()));
            onView(withText("30 minutes")).check(matches(isDisplayed()));
            onView(withText("60 minutes")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void settingsActivity_selectSnoozeDuration_updatesSummaryAndPersists() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            onView(withText(R.string.pref_snooze_duration_title)).perform(click());
            onView(withText("30 minutes")).perform(click());
            // After dialog closes, summary should show selected value
            onView(withText("30 minutes")).check(matches(isDisplayed()));
            // Verify persistence
            scenario.onActivity(activity -> {
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(activity);
                assertEquals("30", prefs.getString("pref_snooze_duration", null));
            });
        }
    }
}
