# Coding Conventions

**Analysis Date:** 2026-02-04

## Naming Patterns

**Files:**
- Java class files: PascalCase (e.g., `MainActivity.java`, `ReminderModel.java`)
- Inner classes: PascalCase within outer class file (e.g., `ReminderItemViewHolder` inside `ReminderItemAdapter.java`)
- Resource files: snake_case (e.g., `activity_main.xml`, `rv_item_reminder.xml`)
- Package naming: reverse domain notation starting with `com.ava.notiva` followed by domain (e.g., `com.ava.notiva.data`, `com.ava.notiva.adapter`)

**Functions/Methods:**
- camelCase with lowercase first letter (e.g., `onCreate()`, `onBindViewHolder()`, `saveReminder()`)
- Getter methods: `get{FieldName}()` (e.g., `getName()`, `getStartDateTime()`)
- Setter methods: `set{FieldName}()` (e.g., `setName()`, `setActive()`)
- Listener callbacks: `on{Action}()` (e.g., `onCheckedChanged()`, `onTimeChanged()`)
- Initialization methods: `init{Component}()` (e.g., `initStartTimeComponents()`, `initRecurrenceTypeSpinner()`)
- Check/verification methods: `check{Thing}()` or `is{Thing}()` (e.g., `checkPermissions()`, `isReminderDisabledOrExpired()`)

**Variables:**
- camelCase (e.g., `reminderModel`, `recurrenceDelay`, `startDateTime`)
- Boolean: `is{Property}` or `{verb}{Noun}` (e.g., `isActive`, `isGranted`, `recurrenceEnabled`)
- Static final constants: UPPER_SNAKE_CASE (e.g., `REMINDER_ID`, `ACTION_SNOOZE`, `TAG`)
- Local variables: camelCase (e.g., `now`, `countStr`, `newId`)
- Member variables: camelCase prefixed with `this.` (e.g., `this.reminderDao`, `this.itemClickListener`)
- Resource IDs in findViewById: descriptive camelCase (e.g., `reminderRecyclerView`, `emptyReminderList`)

**Types/Enums:**
- Enum types: PascalCase (e.g., `RecurrenceType`)
- Enum constants: UPPER_SNAKE_CASE (e.g., `RecurrenceType.DAY`, `RecurrenceType.FOREVER`)

**Constants:**
- Intent extras: fully qualified namespace constants (e.g., `com.ava.notiva.REMINDER_ID`)
- Channel names/IDs: SCREAMING_SNAKE_CASE (e.g., `CHANNEL_ID`, `NOTIVA_CHANNEL`)
- Logging tags: Class name with package prefix for clarity (e.g., `"Notiva.MainActivity"`, `"Notiva.ReminderRepository"`)

## Code Style

**Formatting:**
- Java 17 language level (set in `app/build.gradle` with `JavaVersion.VERSION_17`)
- 4-space indentation (Android standard)
- Line breaks: Max line length not explicitly enforced, but code stays within reasonable bounds
- Brace style: Allman/Java style (opening brace on same line, closing brace on new line)

**Linting:**
- Not explicitly configured; using Android Studio defaults
- Remote WorkManager initializer disabled in lint config: `disable 'RemoveWorkManagerInitializer'` in `build.gradle`

## Import Organization

**Order (observed pattern):**
1. Android framework imports (`android.*`)
2. AndroidX imports (`androidx.*`)
3. Material Design imports (`com.google.android.material.*`)
4. Dagger/Hilt imports (`dagger.*`)
5. Custom/local imports (`com.ava.notiva.*`)
6. Java standard library imports (`java.*`, `javax.*`)

**Path Aliases:**
- No path aliases detected; full package paths used throughout
- Static imports used for constants: `import static com.ava.notiva.util.ReminderConstants.*` (see `MainActivity.java`, `UpsertReminderActivity.java`)

## Error Handling

**Patterns:**
- Try-catch blocks with specific exception handling and logging:
  ```java
  try {
    // operation
    Log.i(TAG, "Success message");
  } catch (Exception e) {
    Log.e(TAG, "Error message", e);
    // Optional: user-facing Toast notification
    Toast.makeText(context, "Error message", Toast.LENGTH_SHORT).show();
  }
  ```
- Async callback errors handled via Consumer pattern: see `ReminderRepository.addWithCallback()` where callback receives `-1` on failure
- Null checks using conditional statements: `if (reminder != null)`, `if (model != null)`
- Optional usage for null-safe chaining: `Optional.ofNullable(reminder.getName()).orElse("")`
- Guard clauses for early returns: `if (!isGranted) { return; }` (in `checkNotificationPermission()`)

**Exception Types:**
- Generic `Exception` catch-all in most handlers (broad exception catching)
- No custom exception types defined
- Silent catch with `catch (Exception ignored)` for intentionally ignored exceptions (see `UpsertReminderActivity.recalculateAndSetEndDate()`)

## Logging

**Framework:** `android.util.Log`

**Patterns:**
- Log tags as static final String in each class: `public static final String TAG = "Notiva.{ClassName}"`
- Log levels used:
  - `Log.i(TAG, message)`: Information about normal operations (e.g., "Added reminder", "Updated reminder")
  - `Log.e(TAG, message, exception)`: Error conditions with throwable
  - `Log.w(TAG, message)`: Warnings for unexpected but recoverable conditions
- Logging context: Important state changes, database operations, user actions, UI updates
- Log messages include IDs and names for tracking: `"Deleted reminder: ID=" + reminder.getId() + ", Name=" + reminder.getName()`

## Comments

**When to Comment:**
- Few inline comments in codebase; code is generally self-documenting
- Comments used sparingly for non-obvious logic
- TODO markers used for known improvements: `// TODO: Use this instead of the logic in UpsertReminderActivity` (RecurrenceSwitchListener.java)

**JSDoc/JavaDoc:**
- Not consistently used in this codebase
- No class-level documentation
- No method-level documentation for public APIs

## Function Design

**Size:**
- Functions range from very small (3-5 lines) to large (100+ lines for complex UI initialization)
- Small focused functions preferred for utility operations (`getReminderAt()`, `isReminderDisabledOrExpired()`)
- Large Activity methods contain multiple related operations (e.g., `initRecurrenceComponents()`)

**Parameters:**
- Constructor injection for dependencies (Hilt/Dagger pattern)
- Callback patterns using functional interfaces: `Consumer<Long>` for async results
- Listener interfaces for UI callbacks: `ReminderItemClickListener`
- Context passed explicitly to utility methods requiring resources

**Return Values:**
- void for side-effect operations (database updates, UI changes)
- LiveData for observable data streams: `LiveData<List<ReminderModel>>`
- Callback functions via Consumer for async results
- Plain objects for data transfer (ReminderModel)
- Worker.Result for WorkManager operations (SUCCESS/FAILURE)

## Module Design

**Exports:**
- DAO interfaces expose data access methods (insert, update, delete, query)
- Repository pattern wraps DAO with business logic and async handling
- ViewModel pattern exposes LiveData for UI observation
- Module pattern uses Hilt for dependency provision and injection

**Barrel Files:**
- No barrel files (index.ts equivalent) used in this Java/Android codebase
- Direct imports of specific classes required

**Package Structure by Responsibility:**
- `adapter/`: RecyclerView adapters and diff callbacks
- `data/`: Data access layer (DAO, Repository, ViewModels)
- `listener/`: Event listeners and text watchers
- `model/`: Entity/data classes (Room entities, enums)
- `module/`: Dependency injection configuration (Hilt)
- `service/`: Background work and system services
- `util/`: Utility/helper classes and constants
- Root package: Application and Activity entry points

## Annotation Usage

**Common Patterns:**
- AndroidX annotations: `@NonNull`, `@Nullable` for null-safety
- Hilt annotations: `@AndroidEntryPoint`, `@HiltAndroidApp`, `@Module`, `@Provides`
- Room annotations: `@Entity`, `@Dao`, `@Query`, `@Insert`, `@Update`, `@Delete`
- Lifecycle annotations: `@Override` for inherited methods
- Named qualifiers: `@Named("reminderDaoExecutor")` for multiple dependencies of same type

## Testing Conventions

**Test File Locations:**
- `app/src/androidTest/java/` for instrumented tests
- Test classes follow naming: `{ClassName}Test` or example format `Example{Type}Test`
- Both local unit tests and instrumented tests present but minimal content

