# Testing Patterns

**Analysis Date:** 2026-02-04

## Test Framework

**Runner:**
- JUnit 4 (version 4.13.2) - standard Android testing framework
- Config: Implicit (no separate config file; uses Android Gradle defaults)

**Instrumentation:**
- AndroidJUnit4 runner for instrumented tests on device/emulator
- Espresso 3.7.0 for UI interaction testing (declared but not used in example tests)
- AndroidX Test runner: `androidx.test.runner.AndroidJUnitRunner`

**Assertion Library:**
- JUnit 4 built-in assertions (`assertEquals`, `assertTrue`, etc.)
- No additional assertion libraries (Mockito, AssertJ, Hamcrest) detected

**Test Dependency Configuration (from `app/build.gradle`):**
```gradle
testImplementation 'junit:junit:4.13.2'
androidTestImplementation 'androidx.test.ext:junit:1.3.0'
androidTestImplementation 'androidx.test.espresso:espresso-core:3.7.0'
```

**Run Commands:**
```bash
# Run all tests (unit + instrumented)
./gradlew test
./gradlew androidTest

# Run specific test class
./gradlew test --tests {TestClassName}
./gradlew androidTest --tests {TestClassName}

# Run with code coverage
./gradlew testDebugUnitTestCoverage
./gradlew testDebugAndroidTestCoverage
```

## Test File Organization

**Location:**
- Local unit tests: `app/src/test/java/com/ava/notiva/` (not present in current structure)
- Instrumented tests: `app/src/androidTest/java/com/ava/notiva/`
- Tests are co-located with main source in separate build variant directory structure

**Naming:**
- Instrumented test classes: `{Feature}Test` (e.g., `ExampleInstrumentedTest.java`, `ExampleUnitTest.java`)
- Test methods: `test{Operation}` or descriptive names (e.g., `useAppContext()`)

**Structure:**
```
app/src/androidTest/java/com/ava/notiva/
├── ExampleInstrumentedTest.java
└── ExampleUnitTest.java
```

## Test Structure

**Suite Organization:**
- Single test class per feature currently (minimal test coverage)
- One test method per test class in example tests
- Flat structure without nested test suites

**Current Example Pattern (from `ExampleInstrumentedTest.java`):**
```java
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
  @Test
  public void useAppContext() {
    Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    assertEquals("com.ava.notiva", appContext.getPackageName());
  }
}
```

**Current Example Pattern (from `ExampleUnitTest.java`):**
```java
public class ExampleUnitTest {
  @Test
  public void addition_isCorrect() {
    assertEquals(4, 2 + 2);
  }
}
```

**Patterns Observed:**
- `@Test` annotation for test methods (JUnit 4)
- `@RunWith(AndroidJUnit4.class)` for instrumented tests requiring Android context
- `InstrumentationRegistry` for accessing application context in instrumented tests
- Test methods are parameterless and void return type

## Mocking

**Framework:**
- No mocking framework currently integrated (Mockito not in dependencies)
- Tests are minimal placeholder examples

**What to Mock (recommendations based on codebase structure):**
- Database operations: DAO, Repository, and ReminderRepository would benefit from mocking in unit tests
- Context: Can be mocked for ViewModel unit tests
- Listeners: Can use spy pattern to verify callback invocations
- WorkManager: Can be mocked for testing ReminderTriggerWorker scheduling

**What NOT to Mock:**
- LiveData transformations - prefer integration testing
- Room entities - test database integration with real database in androidTest
- Android framework components (Intent, PendingIntent) - use instrumented tests
- System services (AlarmManager, ActivityResultLauncher) - mock with Mockito in unit tests

## Fixtures and Factories

**Test Data:**
- No test fixtures or factory patterns currently implemented
- Would benefit from creating test reminder data builders:

```java
// Suggested pattern (not currently in codebase)
public class ReminderTestBuilder {
  public static ReminderModel createTestReminder(String name) {
    ReminderModel model = new ReminderModel(name);
    model.setActive(true);
    model.setRecurrenceType(RecurrenceType.DAY);
    return model;
  }
}
```

**Location:**
- Would place test fixtures in `app/src/androidTest/java/com/ava/notiva/fixtures/`
- Separate builders/factories from actual test classes

## Coverage

**Requirements:**
- No coverage requirements enforced in current configuration
- No coverage reporting configured in build.gradle
- No minimum coverage threshold set

**View Coverage:**
```bash
# Generate coverage report (requires configuration)
./gradlew testDebugUnitTestCoverage
# HTML report available at app/build/reports/coverage/
```

**Current Coverage Gaps:**
- ReminderRepository: No tests covering async execution, callback handling, or database error cases
- ReminderModel: No tests covering `getNextOccurrenceAfter()` logic (complex recurrence calculations)
- Adapters (ReminderItemAdapter): No tests for ViewHolder binding or list diffing
- ViewModels: No tests for LiveData observation or repository interaction
- Workers (ReminderTriggerWorker): No tests for alarm scheduling or error handling
- Activities: No instrumented tests for UI interactions or permission handling
- Listeners: No tests for TextWatcher or OnCheckedChangeListener behavior

## Test Types

**Unit Tests:**
- Scope: Single class behavior in isolation
- Approach: JUnit 4 with minimal dependencies
- Current example: `ExampleUnitTest.java` (trivial arithmetic test)
- Location: `app/src/androidTest/java/` (should be in `app/src/test/java/`)

**Instrumented Tests (Android Integration Tests):**
- Scope: Testing with Android framework and real context
- Approach: AndroidJUnit4 runner with InstrumentationRegistry
- Current example: `ExampleInstrumentedTest.java` (verifies app package name)
- Location: `app/src/androidTest/java/com/ava/notiva/`

**E2E Tests:**
- Framework: Not used
- Would require Espresso (already in dependencies) for UI flow testing
- Not currently implemented for user journeys like "create reminder → observe in list → delete"

## Common Patterns

**Async Testing:**
- CountDownLatch pattern (not currently used):
  ```java
  CountDownLatch latch = new CountDownLatch(1);
  repository.addWithCallback(reminder, id -> latch.countDown());
  assertTrue(latch.await(5, TimeUnit.SECONDS));
  ```

- LiveData test helpers (not in dependencies):
  ```java
  // Would use androidx.arch.core:core-testing for getOrAwaitValue()
  List<ReminderModel> reminders = getAllRemindersViewModel
      .getAllReminders()
      .getOrAwaitValue();
  ```

**Error Testing:**
- No error scenarios currently tested
- Recommended pattern for testing exception handling:
  ```java
  @Test
  public void testDatabaseErrorHandling() {
    try {
      // operation that throws
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertEquals("Expected message", e.getMessage());
    }
  }
  ```

## Missing Test Coverage Analysis

**High Priority Test Gaps:**
- `ReminderModel.getNextOccurrenceAfter()`: Complex logic with multiple recurrence types (NEVER, DAY, WEEK, MONTH, YEAR, FOREVER) needs comprehensive unit tests
- `ReminderRepository` async operations: Callback handling, executor thread safety
- `ReminderTriggerWorker.doWork()`: Alarm scheduling with AlarmManager
- `MainActivity` and `UpsertReminderActivity`: UI interactions, permission requests, data binding
- `ReminderItemAdapter.onBindViewHolder()`: Complex display logic with styling for disabled/expired reminders

**Medium Priority Test Gaps:**
- `DbTypeConverters`: Calendar to/from Long conversion edge cases
- Listeners: TextWatcher and OnCheckedChangeListener implementations
- Permission request flows and callbacks

**Recommended Next Steps:**
1. Add androidx.arch.core:core-testing for LiveData testing utilities
2. Add Mockito for mocking dependencies in unit tests
3. Create test fixtures in `app/src/androidTest/java/com/ava/notiva/fixtures/`
4. Start with unit testing ReminderModel business logic (getNextOccurrenceAfter)
5. Add instrumented tests for database operations with AndroidRoom test helpers
6. Implement Espresso tests for critical UI flows

