package com.ava.notiva;

import android.app.Application;
import android.content.Context;

import androidx.test.runner.AndroidJUnitRunner;

import dagger.hilt.android.testing.HiltTestApplication;

/**
 * Custom test runner that uses {@link HiltTestApplication} for Hilt dependency injection in tests.
 * Referenced in build.gradle as testInstrumentationRunner.
 */
public class HiltTestRunner extends AndroidJUnitRunner {
    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        return super.newApplication(cl, HiltTestApplication.class.getName(), context);
    }
}
