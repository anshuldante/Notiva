# Testing Guide

![Tests](https://img.shields.io/badge/tests-JUnit_4-green)
![Instrumented](https://img.shields.io/badge/instrumented-Espresso_3.7.0-blue)

## Overview

This guide covers testing practices for Notiva, including how to run existing tests, write new tests, and follow established testing patterns.

**Related Documentation:**
- [README.md](README.md) - Documentation hub
- [ARCHITECTURE.md](ARCHITECTURE.md) - App architecture and layers
- [DATABASE.md](DATABASE.md) - Room database schema

## Testing Philosophy

Notiva follows Android's recommended testing strategy with two main test types:

1. **Unit Tests** - Fast, isolated tests that run on the JVM without Android dependencies
2. **Instrumented Tests** - Tests that run on an Android device or emulator, with access to Android APIs

**Testing Priorities:**
- Business logic and data transformations (unit tests)
- Database operations and queries (instrumented tests)
- UI flows and user interactions (instrumented tests with Espresso)

## Test Directory Structure

```
app/src/
├── main/java/com/ava/notiva/          # Production code
├── test/java/com/ava/notiva/          # Unit tests (JVM)
│   └── [unit test classes]
└── androidTest/java/com/ava/notiva/   # Instrumented tests (device/emulator)
    ├── ExampleInstrumentedTest.java   # Context verification test
    └── ExampleUnitTest.java           # Basic arithmetic test
```

**Directory Purpose:**

| Directory | Type | Runs On | Use For |
|-----------|------|---------|---------|
| `test/` | Unit | JVM (local machine) | Pure logic, utilities, non-Android code |
| `androidTest/` | Instrumented | Device/Emulator | Android APIs, UI, database, context |

## Test Frameworks and Dependencies

The project uses the following test dependencies (from `app/build.gradle`):

```groovy
// Unit testing
testImplementation 'junit:junit:4.13.2'

// Instrumented testing
androidTestImplementation 'androidx.test.ext:junit:1.3.0'
androidTestImplementation 'androidx.test.espresso:espresso-core:3.7.0'
```

| Framework | Version | Purpose |
|-----------|---------|---------|
| JUnit 4 | 4.13.2 | Core testing framework for both test types |
| AndroidX Test JUnit | 1.3.0 | Android-specific JUnit extensions |
| Espresso | 3.7.0 | UI testing framework |

**Test Runner Configuration:**

The instrumented test runner is configured in `app/build.gradle`:

```groovy
android {
    defaultConfig {
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }
}
```

## Running Tests

### Unit Tests

Unit tests run on your local JVM without requiring a device or emulator.

```bash
# Run all unit tests
./gradlew test

# Run tests for debug build variant
./gradlew testDebugUnitTest

# Run tests for release build variant
./gradlew testReleaseUnitTest

# Run with verbose output
./gradlew test --info
```

**Test Reports:**
After running, find HTML reports at:
```
app/build/reports/tests/testDebugUnitTest/index.html
```

### Instrumented Tests

Instrumented tests require a connected device or running emulator.

```bash
# Start an emulator or connect a device first

# Run all instrumented tests
./gradlew connectedAndroidTest

# Run for specific build variant
./gradlew connectedDebugAndroidTest

# Run with verbose output
./gradlew connectedAndroidTest --info
```

**Test Reports:**
After running, find HTML reports at:
```
app/build/reports/androidTests/connected/index.html
```

### Running Specific Tests

```bash
# Run a specific test class (unit)
./gradlew test --tests "com.ava.notiva.ExampleUnitTest"

# Run a specific test method (unit)
./gradlew test --tests "com.ava.notiva.ExampleUnitTest.addition_isCorrect"

# Run tests matching a pattern
./gradlew test --tests "*Reminder*"
```

## Existing Test Examples

### ExampleInstrumentedTest.java

Located at: `app/src/androidTest/java/com/ava/notiva/ExampleInstrumentedTest.java`

```java
package com.ava.notiva;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
  @Test
  public void useAppContext() {
    // Context of the app under test.
    Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    assertEquals("com.ava.notiva", appContext.getPackageName());
  }
}
```

**Key Points:**
- Uses `@RunWith(AndroidJUnit4.class)` annotation for Android test runner
- Accesses Android Context via `InstrumentationRegistry`
- Verifies the app package name matches expected value

### ExampleUnitTest.java

Located at: `app/src/androidTest/java/com/ava/notiva/ExampleUnitTest.java`

```java
package com.ava.notiva;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
  @Test
  public void addition_isCorrect() {
    assertEquals(4, 2 + 2);
  }
}
```

**Key Points:**
- No Android dependencies required
- Uses standard JUnit assertions
- Tests pure Java logic

## Writing New Tests

### Unit Test Template

Create files in `app/src/test/java/com/ava/notiva/`

```java
package com.ava.notiva;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for [ClassName].
 */
public class ClassNameTest {

    private ClassName instance;

    @Before
    public void setUp() {
        // Initialize test fixtures
        instance = new ClassName();
    }

    @Test
    public void methodName_condition_expectedResult() {
        // Arrange
        String input = "test input";

        // Act
        String result = instance.methodName(input);

        // Assert
        assertEquals("expected output", result);
    }

    @Test
    public void methodName_edgeCase_handlesGracefully() {
        // Arrange
        String nullInput = null;

        // Act & Assert
        assertNull(instance.methodName(nullInput));
    }
}
```

### Instrumented Test Template

Create files in `app/src/androidTest/java/com/ava/notiva/`

```java
package com.ava.notiva;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented tests for [ClassName].
 */
@RunWith(AndroidJUnit4.class)
public class ClassNameInstrumentedTest {

    private Context context;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void methodName_withContext_worksCorrectly() {
        // Arrange
        // Use context for Android-specific setup

        // Act
        // Perform action

        // Assert
        assertNotNull(context);
    }
}
```

### Database Test Template

For testing Room database operations:

```java
package com.ava.notiva;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.ava.notiva.database.AppDatabase;
import com.ava.notiva.database.ReminderDao;
import com.ava.notiva.database.ReminderEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * Database tests for ReminderDao.
 */
@RunWith(AndroidJUnit4.class)
public class ReminderDaoTest {

    private ReminderDao reminderDao;
    private AppDatabase db;

    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        // Use in-memory database for tests
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries() // Only for testing
                .build();
        reminderDao = db.reminderDao();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void insertReminder_andReadBack() {
        // Arrange
        ReminderEntity reminder = new ReminderEntity();
        reminder.setTitle("Test Reminder");
        reminder.setEnabled(true);

        // Act
        reminderDao.insert(reminder);
        ReminderEntity result = reminderDao.getById(reminder.getId());

        // Assert
        assertEquals("Test Reminder", result.getTitle());
        assertTrue(result.isEnabled());
    }
}
```

### Espresso UI Test Template

For testing user interface interactions:

```java
package com.ava.notiva;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ava.notiva.activities.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for MainActivity.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void mainActivity_displaysReminderList() {
        // Check that the reminder list is displayed
        onView(withId(R.id.reminderRecyclerView))
                .check(matches(isDisplayed()));
    }

    @Test
    public void fabClick_opensReminderEditor() {
        // Click the FAB
        onView(withId(R.id.fab))
                .perform(click());

        // Verify editor activity opens
        onView(withId(R.id.titleEditText))
                .check(matches(isDisplayed()));
    }
}
```

## Test Coverage Priorities

Based on the app architecture, prioritize testing in this order:

### High Priority (Critical Paths)

1. **Reminder CRUD Operations**
   - Create, read, update, delete reminders
   - Database query correctness
   - Data validation

2. **Notification Scheduling**
   - Correct alarm times
   - Recurrence calculations
   - Worker scheduling

3. **Time/Date Calculations**
   - Recurrence type conversions
   - Next trigger time calculations
   - Timezone handling

### Medium Priority (Important Features)

4. **UI State Management**
   - Adapter data binding
   - List updates after changes
   - Form validation states

5. **Broadcast Receivers**
   - Alarm receiver handling
   - Boot complete receiver
   - Notification actions

### Lower Priority (Edge Cases)

6. **Edge Cases**
   - Empty states
   - Maximum limits
   - Error recovery

## Common Testing Patterns

### Testing Recurrence Calculations

```java
@Test
public void recurrenceType_minutely_returnsCorrectMillis() {
    long expected = 60 * 1000L; // 1 minute in milliseconds
    assertEquals(expected, RecurrenceType.MINUTELY.getMillis());
}

@Test
public void recurrenceType_daily_returnsCorrectMillis() {
    long expected = 24 * 60 * 60 * 1000L; // 24 hours in milliseconds
    assertEquals(expected, RecurrenceType.DAILY.getMillis());
}
```

### Testing with Mocks (using Mockito)

Add to `build.gradle` if needed:
```groovy
testImplementation 'org.mockito:mockito-core:5.11.0'
```

```java
import static org.mockito.Mockito.*;

@Test
public void worker_schedulesNotification_correctlyCallsManager() {
    // Arrange
    NotificationManager mockManager = mock(NotificationManager.class);

    // Act
    worker.scheduleNotification(mockManager, reminder);

    // Assert
    verify(mockManager).notify(anyInt(), any(Notification.class));
}
```

### Testing Async Operations with RxJava

```java
import io.reactivex.observers.TestObserver;

@Test
public void getAllReminders_returnsObservable() {
    // Arrange
    TestObserver<List<ReminderEntity>> testObserver = new TestObserver<>();

    // Act
    reminderDao.getAllAsFlowable().subscribe(testObserver);

    // Assert
    testObserver.assertNoErrors();
    testObserver.assertValueCount(1);
}
```

## Troubleshooting

### Common Issues

**1. "No tests found"**
```
Execution failed for task ':app:testDebugUnitTest'.
> No tests found
```
**Solution:** Ensure test classes are in the correct directory and have `@Test` annotations.

**2. "No connected devices"**
```
> No connected devices!
```
**Solution:** Start an emulator or connect a physical device before running instrumented tests:
```bash
emulator -avd Pixel_8_API_33 &
./gradlew connectedAndroidTest
```

**3. "Test failed: Context is null"**
**Solution:** For instrumented tests, use `@RunWith(AndroidJUnit4.class)` and get context via:
```java
Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
```

**4. "Cannot run queries on main thread"**
**Solution:** For database tests, add `.allowMainThreadQueries()` to Room builder or use RxJava:
```java
db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
        .allowMainThreadQueries()
        .build();
```

**5. "Hilt injection failed in tests"**
**Solution:** For Hilt-enabled apps, use HiltAndroidTest:
```java
@HiltAndroidTest
@RunWith(AndroidJUnit4.class)
public class HiltEnabledTest {
    @Rule
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Before
    public void init() {
        hiltRule.inject();
    }
}
```

**6. "Espresso idle resource timeout"**
**Solution:** Wait for async operations to complete:
```java
// Add IdlingResource for async operations
IdlingRegistry.getInstance().register(idlingResource);
// Run test
IdlingRegistry.getInstance().unregister(idlingResource);
```

### Debugging Tests

```bash
# Run tests with debug logging
./gradlew test --debug

# Run tests and stop at first failure
./gradlew test --fail-fast

# Re-run tests ignoring cache
./gradlew test --rerun-tasks

# Clean build before testing
./gradlew clean test
```

## Next Steps

After reading this guide, you should be able to:

1. Run the existing unit and instrumented tests
2. Write new unit tests for business logic
3. Write new instrumented tests for Android components
4. Write database tests using Room's in-memory database
5. Write UI tests using Espresso

**Recommended reading:**
- [Android Testing Documentation](https://developer.android.com/training/testing)
- [Espresso Testing Guide](https://developer.android.com/training/testing/espresso)
- [Room Testing Guide](https://developer.android.com/training/data-storage/room/testing-db)

---

*Documentation last updated: 2026-02-05*
