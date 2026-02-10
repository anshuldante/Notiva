package com.ava.notiva;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.Manifest;
import android.content.Context;

import androidx.preference.PreferenceManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;

/**
 * Instrumented tests for MainActivity toolbar and settings navigation.
 * Verifies toolbar display, app name, gear icon visibility, and navigation to SettingsActivity.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4.class)
public class MainActivityToolbarTest {

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule(order = 1)
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.POST_NOTIFICATIONS);

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit();
    }

    @Test
    public void mainActivity_displaysToolbar() {
        try (ActivityScenario<MainActivity> scenario =
                     ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void mainActivity_toolbarShowsAppName() {
        try (ActivityScenario<MainActivity> scenario =
                     ActivityScenario.launch(MainActivity.class)) {
            onView(withText(R.string.app_name)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void mainActivity_settingsMenuItemIsVisible() {
        try (ActivityScenario<MainActivity> scenario =
                     ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.action_settings)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void mainActivity_clickSettingsIcon_launchesSettingsActivity() {
        try (ActivityScenario<MainActivity> scenario =
                     ActivityScenario.launch(MainActivity.class)) {
            Intents.init();
            try {
                onView(withId(R.id.action_settings)).perform(click());
                intended(hasComponent(SettingsActivity.class.getName()));
            } finally {
                Intents.release();
            }
        }
    }
}
