# Code Examples

Quick reference for common implementation patterns used in Notiva. Each example links to the actual source file for full context.

For architectural understanding, see [Architecture Overview](ARCHITECTURE.md). For feature-specific details, see [Reminders](features/REMINDERS.md), [Notifications](features/NOTIFICATIONS.md), and [Recurrence](features/RECURRENCE.md).

---

## Table of Contents

1. [Dependency Injection (Hilt)](#dependency-injection-hilt)
2. [Room Database Operations](#room-database-operations)
3. [Async Operations](#async-operations)
4. [WorkManager](#workmanager)
5. [Notifications](#notifications)
6. [RecyclerView](#recyclerview)

---

## Dependency Injection (Hilt)

### Module Providing Singletons

**Source:** [`DbModule.java`](../app/src/main/java/com/ava/notiva/module/DbModule.java)

Hilt modules define how dependencies are created and provided throughout the app.

```java
@Module
@InstallIn(SingletonComponent.class)
public class DbModule {

  @Provides
  @Singleton
  public RemindersDb getRemindersDb(@ApplicationContext Context context) {
    return Room.databaseBuilder(context, RemindersDb.class, "Reminders-DB").build();
  }

  @Provides
  @Singleton
  public ReminderDao getReminderDao(RemindersDb remindersDb) {
    return remindersDb.reminderDao();
  }

  @Provides
  @Singleton
  public ReminderRepository getReminderRepository(
      ReminderDao reminderDao, @Named("reminderDaoExecutor") ExecutorService reminderDaoExecutor) {
    return new ReminderRepository(reminderDao, reminderDaoExecutor);
  }
}
```

**Key annotations:**
- `@Module` - Marks class as a Hilt module
- `@InstallIn(SingletonComponent.class)` - Scoped to application lifecycle
- `@Provides` - Method provides a dependency
- `@Singleton` - Only one instance created
- `@Named("...")` - Disambiguates multiple providers of same type

**When to use:** Create a module when you need to provide third-party library instances (like Room database) or when constructor injection isn't possible.

---

### Named ExecutorService

**Source:** [`DbModule.java`](../app/src/main/java/com/ava/notiva/module/DbModule.java)

Use `@Named` to provide multiple instances of the same type with different configurations.

```java
@Provides
@Singleton
@Named("reminderDaoExecutor")
public ExecutorService getReminderDaoExecutorService() {
  return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
}
```

**When to use:** When you need multiple ExecutorService instances for different purposes (e.g., database operations vs. network calls).

---

### ViewModel Injection

**Source:** [`ReminderDmlViewModel.java`](../app/src/main/java/com/ava/notiva/data/ReminderDmlViewModel.java)

ViewModels receive their dependencies through constructor injection.

```java
public class ReminderDmlViewModel extends ViewModel {

  private final ReminderRepository reminderRepository;

  public ReminderDmlViewModel(ReminderRepository reminderRepository) {
    this.reminderRepository = reminderRepository;
  }

  public void updateReminder(ReminderModel model) {
    reminderRepository.update(model);
  }

  public void addReminderWithCallback(ReminderModel model, Consumer<Long> callback) {
    reminderRepository.addWithCallback(model, callback);
  }
}
```

**Note:** The ViewModel is provided by `DbModule` rather than using `@HiltViewModel`. This approach gives explicit control over ViewModel creation and allows for custom instantiation logic.

---

## Room Database Operations

### Entity with TypeConverters

**Source:** [`ReminderModel.java`](../app/src/main/java/com/ava/notiva/model/ReminderModel.java)

Room entities map directly to database tables. TypeConverters handle non-primitive types.

```java
@Entity(tableName = "reminders")
@TypeConverters(DbTypeConverters.class)
public class ReminderModel {

  @PrimaryKey(autoGenerate = true)
  private int id;

  private boolean active;
  private String name;

  @ColumnInfo(name = "start_date")
  private Calendar startDateTime;

  @ColumnInfo(name = "recurrence_delay")
  private int recurrenceDelay;

  @ColumnInfo(name = "recurrence_type")
  private RecurrenceType recurrenceType;

  @ColumnInfo(name = "end_date")
  private Calendar endDateTime;

  // Constructors, getters, setters...
}
```

**Key annotations:**
- `@Entity(tableName = "...")` - Maps class to database table
- `@PrimaryKey(autoGenerate = true)` - Auto-incrementing primary key
- `@ColumnInfo(name = "...")` - Custom column name (different from field name)
- `@TypeConverters` - Class-level converter for complex types

---

### TypeConverter for Calendar

**Source:** [`DbTypeConverters.java`](../app/src/main/java/com/ava/notiva/converter/DbTypeConverters.java)

TypeConverters transform complex types to/from database-compatible types.

```java
public class DbTypeConverters {

  @TypeConverter
  public static Calendar toCalendar(Long timeInMillis) {
    Calendar calendar = null;
    if (timeInMillis != null) {
      calendar = Calendar.getInstance();
      calendar.setTimeInMillis(timeInMillis);
    }
    return calendar;
  }

  @TypeConverter
  public static Long fromCalendar(Calendar calendar) {
    if (calendar != null) {
      return calendar.getTimeInMillis();
    }
    return null;
  }
}
```

**When to use:** Any time you need to store a non-primitive type (Calendar, Date, Enum, List) in Room. The converter transforms it to a storable type (Long, String, etc.).

---

### DAO with LiveData

**Source:** [`ReminderDao.java`](../app/src/main/java/com/ava/notiva/data/ReminderDao.java)

DAOs define database operations. Return `LiveData` for observable queries.

```java
@Dao
public interface ReminderDao {

  @Insert
  long add(ReminderModel model);

  @Delete
  void delete(ReminderModel model);

  @Update
  void update(ReminderModel model);

  @Query("update reminders set active = :isActive where id = :id")
  void updateStatus(int id, boolean isActive);

  @Query("SELECT * FROM reminders order by name")
  LiveData<List<ReminderModel>> getAll();

  @Query("SELECT * FROM reminders order by name")
  List<ReminderModel> getAllSync();

  @Query("SELECT * FROM reminders where id = :id")
  LiveData<ReminderModel> get(int id);
}
```

**Key patterns:**
- `@Insert` / `@Delete` / `@Update` - Standard CRUD operations
- `@Query` - Custom SQL for complex operations
- `LiveData<T>` return - Automatic UI updates when data changes
- `List<T>` return (sync) - For background operations that need immediate data

---

## Async Operations

### Repository with ExecutorService

**Source:** [`ReminderRepository.java`](../app/src/main/java/com/ava/notiva/data/ReminderRepository.java)

The Repository pattern isolates data operations and handles threading.

```java
public class ReminderRepository {

  private static final String TAG = "Notiva.ReminderRepository: ";
  private final ReminderDao reminderDao;
  private final ExecutorService reminderDaoExecutor;
  private final LiveData<List<ReminderModel>> getAllObservable;

  public ReminderRepository(ReminderDao reminderDao, ExecutorService reminderDaoExecutor) {
    this.reminderDao = reminderDao;
    this.reminderDaoExecutor = reminderDaoExecutor;
    this.getAllObservable = reminderDao.getAll();
  }

  public LiveData<List<ReminderModel>> getAll() {
    return getAllObservable;
  }

  public void delete(ReminderModel reminder) {
    reminderDaoExecutor.submit(() -> {
      try {
        reminderDao.delete(reminder);
        Log.i(TAG, "Deleted reminder: " + Optional.ofNullable(reminder.getName()).orElse(""));
      } catch (Exception e) {
        Log.e(TAG, "Exception while deleting reminder: " + reminder.getName(), e);
      }
    });
  }
}
```

**Threading model:**
- All write operations use `ExecutorService.submit()` to run off main thread
- Read operations returning `LiveData` are handled by Room automatically
- Synchronous reads (`getAllSync()`) are for background workers only

---

### Callback Pattern for Async Results

**Source:** [`ReminderRepository.java`](../app/src/main/java/com/ava/notiva/data/ReminderRepository.java)

Use callbacks to return results from async operations (e.g., generated IDs).

```java
public void addWithCallback(ReminderModel model, Consumer<Long> callback) {
  reminderDaoExecutor.submit(() -> {
    long id = -1;
    try {
      id = reminderDao.add(model);
      Log.i(TAG, "Added reminder (async): " + model + ", id: " + id);
    } catch (Exception e) {
      Log.e(TAG, "Error while adding the reminder (async): " + model, e);
    }
    if (callback != null) {
      callback.accept(id);
    }
  });
}
```

**Usage in ViewModel:**
```java
public void addReminderWithCallback(ReminderModel model, Consumer<Long> callback) {
  reminderRepository.addWithCallback(model, callback);
}
```

**When to use:** When the caller needs the result of an async operation (like the auto-generated ID after insert).

---

## WorkManager

### Worker Implementation with Hilt

**Source:** [`ReminderTriggerWorker.java`](../app/src/main/java/com/ava/notiva/service/ReminderTriggerWorker.java)

Workers perform background tasks. Use `@HiltWorker` for dependency injection.

```java
@HiltWorker
public class ReminderTriggerWorker extends Worker {
  public static final String TAG = "ReminderTriggerWorker";
  private final ReminderRepository reminderRepository;

  @AssistedInject
  public ReminderTriggerWorker(@Assisted @NonNull Context context,
                               @Assisted @NonNull WorkerParameters params,
                               ReminderRepository reminderRepository) {
    super(context, params);
    this.reminderRepository = reminderRepository;
  }

  @NonNull
  @Override
  public Result doWork() {
    try {
      List<ReminderModel> reminders = reminderRepository.getAllSync();
      Calendar now = Calendar.getInstance();
      AlarmManager alarmMgr = (AlarmManager) getApplicationContext()
          .getSystemService(Context.ALARM_SERVICE);

      for (ReminderModel reminder : reminders) {
        if (!reminder.isActive()) continue;
        Calendar next = reminder.getNextOccurrenceAfter(now);
        if (next != null && next.after(now)) {
          // Schedule exact alarm for this reminder
          Intent alarmIntent = new Intent(getApplicationContext(),
              NotificationStarterService.class);
          alarmIntent.putExtra(REMINDER_ID, reminder.getId());
          alarmIntent.putExtra(REMINDER_NAME, reminder.getName());
          // ... schedule with AlarmManager
        }
      }
      return Result.success();
    } catch (Exception e) {
      Log.e(TAG, "Error scheduling reminders", e);
      return Result.failure();
    }
  }
}
```

**Key annotations:**
- `@HiltWorker` - Enables Hilt injection in Worker
- `@AssistedInject` - Constructor injection with assisted parameters
- `@Assisted` - Parameters provided by WorkManager (Context, WorkerParameters)

**Result types:**
- `Result.success()` - Work completed successfully
- `Result.failure()` - Work failed, don't retry
- `Result.retry()` - Work failed, retry later

---

### Enqueueing Work

**Source:** [`ReminderWorkerUtils.java`](../app/src/main/java/com/ava/notiva/util/ReminderWorkerUtils.java)

Utility class for scheduling work requests.

```java
public class ReminderWorkerUtils {
  public static void enqueueReminderWorker(Context context) {
    WorkManager.getInstance(context).enqueue(
        new OneTimeWorkRequest.Builder(ReminderTriggerWorker.class).build()
    );
  }
}
```

**Work request types:**
- `OneTimeWorkRequest` - Runs once
- `PeriodicWorkRequest` - Runs on a schedule (minimum 15 minutes)

**When to use:** Call `enqueueReminderWorker()` after any reminder CRUD operation to reschedule all alarms.

---

## Notifications

### Foreground Service with Notification

**Source:** [`NotificationStarterService.java`](../app/src/main/java/com/ava/notiva/service/NotificationStarterService.java)

Foreground services must display a notification while running.

```java
public class NotificationStarterService extends Service {

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    notificationId = intent.getIntExtra(REMINDER_ID, -1);
    notificationName = intent.getStringExtra(REMINDER_NAME);

    startForeground(notificationId, buildNotification());

    mediaPlayer.start();
    vibrateWithPattern();
    return START_STICKY;
  }

  private Notification buildNotification() {
    createNotificationChannel();
    NotificationCompat.Builder builder = createNotification();
    attachSnoozeAction(builder);
    attachDismissActions(builder);
    return builder.build();
  }

  private void createNotificationChannel() {
    NotificationChannel channel = new NotificationChannel(
        CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
    channel.setDescription(CHANNEL_DESCRIPTION);
    channel.setLightColor(Color.BLUE);
    channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
    notificationManager.createNotificationChannel(channel);
  }
}
```

**Key points:**
- `startForeground()` must be called within 5 seconds of service start
- Notification channel is required for Android 8.0+
- `START_STICKY` - System restarts service if killed

---

### Notification Actions (Snooze/Dismiss)

**Source:** [`NotificationStarterService.java`](../app/src/main/java/com/ava/notiva/service/NotificationStarterService.java)

Add action buttons to notifications using PendingIntents.

```java
private void attachSnoozeAction(NotificationCompat.Builder builder) {
  Intent snoozeIntent = new Intent(this, NotificationStopperService.class);
  snoozeIntent.setAction(ACTION_SNOOZE);
  snoozeIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
  snoozeIntent.putExtra(REMINDER_ID, notificationId);
  snoozeIntent.putExtra(REMINDER_NAME, notificationName);
  PendingIntent snoozePendingIntent = PendingIntent.getService(
      this, 0, snoozeIntent, FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT);
  builder.addAction(
      R.drawable.ic_baseline_snooze_24, getString(R.string.snooze), snoozePendingIntent);
}

private void attachDismissActions(NotificationCompat.Builder builder) {
  Intent dismissIntent = new Intent(this, NotificationStopperService.class);
  dismissIntent.setAction(ACTION_DISMISS);
  dismissIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
  dismissIntent.putExtra(REMINDER_ID, notificationId);
  PendingIntent dismissPendingIntent = PendingIntent.getService(
      this, 0, dismissIntent, FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT);
  builder.addAction(
      R.drawable.ic_baseline_cancel_24, getString(R.string.dismiss), dismissPendingIntent);
  builder.setContentIntent(dismissPendingIntent);
}
```

**PendingIntent flags:**
- `FLAG_IMMUTABLE` - Required for Android 12+ security
- `FLAG_UPDATE_CURRENT` - Update existing intent with new extras

---

### Vibration Pattern

**Source:** [`NotificationStarterService.java`](../app/src/main/java/com/ava/notiva/service/NotificationStarterService.java)

```java
private void vibrateWithPattern() {
  if (vibrator != null && vibrator.hasVibrator()) {
    long[] pattern = {0, 500, 300, 500};  // delay, vibrate, pause, vibrate
    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
  }
}
```

**Pattern format:** `{delay, vibrate, pause, vibrate, ...}` in milliseconds. The `-1` means don't repeat.

---

## RecyclerView

### ListAdapter with DiffUtil

**Source:** [`ReminderItemAdapter.java`](../app/src/main/java/com/ava/notiva/adapter/ReminderItemAdapter.java)

`ListAdapter` automatically handles list updates efficiently using DiffUtil.

```java
public class ReminderItemAdapter
    extends ListAdapter<ReminderModel, ReminderItemAdapter.ReminderItemViewHolder> {

  private static final ReminderDiffCallback DIFF_CALLBACK = new ReminderDiffCallback();
  private final Context context;
  private final ReminderDmlViewModel dmlViewModel;
  private final ReminderItemClickListener itemClickListener;

  public ReminderItemAdapter(
      Context context,
      ReminderDmlViewModel dmlViewModel,
      ReminderItemClickListener itemClickListener) {
    super(DIFF_CALLBACK);
    this.context = context;
    this.dmlViewModel = dmlViewModel;
    this.itemClickListener = itemClickListener;
  }

  @NonNull
  @Override
  public ReminderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ReminderItemViewHolder(
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.rv_item_reminder, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ReminderItemViewHolder holder, int position) {
    ReminderModel reminder = getItem(position);
    holder.alarmName.setText(reminder.getName());
    holder.activeSwitch.setChecked(reminder.isActive());
    // ... more binding
  }

  public interface ReminderItemClickListener {
    void onItemClick(ReminderModel reminderAt);
  }
}
```

**Benefits of ListAdapter:**
- Automatic diffing on background thread
- Smooth animations for insertions/removals
- No need to call `notifyDataSetChanged()`

---

### DiffUtil.ItemCallback

**Source:** [`ReminderDiffCallback.java`](../app/src/main/java/com/ava/notiva/adapter/ReminderDiffCallback.java)

DiffUtil compares items to determine what changed.

```java
public class ReminderDiffCallback extends DiffUtil.ItemCallback<ReminderModel> {

  @Override
  public boolean areItemsTheSame(@NonNull ReminderModel oldItem, @NonNull ReminderModel newItem) {
    return oldItem.getId() == newItem.getId();
  }

  @Override
  public boolean areContentsTheSame(@NonNull ReminderModel oldItem, @NonNull ReminderModel newItem) {
    return oldItem.equals(newItem);
  }
}
```

**Methods:**
- `areItemsTheSame()` - Compare identity (usually by ID)
- `areContentsTheSame()` - Compare content (uses `equals()`)

**When to use:** Always use DiffUtil with RecyclerView for efficient updates. Implement `equals()` and `hashCode()` on your model class.

---

### ViewHolder with Click Listeners

**Source:** [`ReminderItemAdapter.java`](../app/src/main/java/com/ava/notiva/adapter/ReminderItemAdapter.java)

```java
public class ReminderItemViewHolder extends RecyclerView.ViewHolder {

  private final TextView alarmName;
  private final SwitchCompat activeSwitch;
  private ReminderModel reminder;

  public ReminderItemViewHolder(View itemView) {
    super(itemView);
    alarmName = itemView.findViewById(R.id.rir_tv_alarm_name);
    activeSwitch = itemView.findViewById(R.id.rir_sw_active);

    activeSwitch.setOnCheckedChangeListener(this::toggleReminderStatus);
    itemView.setOnClickListener(this::openReminderEditor);
  }

  private void openReminderEditor(View view) {
    int position = getBindingAdapterPosition();
    if (itemClickListener != null && position != RecyclerView.NO_POSITION) {
      itemClickListener.onItemClick(getReminderAt(position));
    }
  }

  private void toggleReminderStatus(CompoundButton buttonView, boolean isChecked) {
    if (reminder != null && isChecked != reminder.isActive()) {
      dmlViewModel.updateReminderStatus(reminder, isChecked);
    }
  }
}
```

**Best practices:**
- Use `getBindingAdapterPosition()` not `getAdapterPosition()` (deprecated)
- Check for `NO_POSITION` to handle edge cases during animations
- Store model reference in ViewHolder for click handlers

---

## Related Documentation

- [Architecture Overview](ARCHITECTURE.md) - System design and patterns
- [Database Reference](DATABASE.md) - Schema and migrations
- [Reminders Feature](features/REMINDERS.md) - Reminder CRUD flows
- [Notifications Feature](features/NOTIFICATIONS.md) - Notification system
- [Recurrence Feature](features/RECURRENCE.md) - Scheduling logic
- [Testing Guide](TESTING.md) - Testing strategies

---

*Last updated: 2026-02-05*
