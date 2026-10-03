package uz.relay.todoapp.screenshots

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import uz.relay.todoapp.domain.model.ListIcon
import uz.relay.todoapp.domain.model.Priority
import uz.relay.todoapp.domain.model.RepeatRule
import uz.relay.todoapp.domain.model.Settings
import uz.relay.todoapp.domain.model.Stats
import uz.relay.todoapp.domain.model.DayCount
import uz.relay.todoapp.domain.model.Subtask
import uz.relay.todoapp.domain.model.Task
import uz.relay.todoapp.domain.model.TaskList
import uz.relay.todoapp.presenter.editor.EditorContract
import uz.relay.todoapp.presenter.editor.EditorScreenContent
import uz.relay.todoapp.presenter.listdetail.ListDetailContract
import uz.relay.todoapp.presenter.listdetail.ListDetailScreenContent
import uz.relay.todoapp.presenter.lists.ListsContract
import uz.relay.todoapp.presenter.lists.ListsScreenContent
import uz.relay.todoapp.presenter.onboarding.OnboardingScreenContent
import uz.relay.todoapp.presenter.settings.SettingsContract
import uz.relay.todoapp.presenter.settings.SettingsScreenContent
import uz.relay.todoapp.presenter.stats.StatsContract
import uz.relay.todoapp.presenter.stats.StatsScreenContent
import uz.relay.todoapp.presenter.today.TodayContract
import uz.relay.todoapp.presenter.today.TodayScreenContent
import uz.relay.todoapp.presenter.upcoming.UpcomingContract
import uz.relay.todoapp.presenter.upcoming.UpcomingScreenContent
import uz.relay.todoapp.ui.theme.TickTheme
import java.time.LocalDate
import java.time.LocalTime

/**
 * Renders the real screens with demo data into docs/screenshots for the README.
 * Regenerate with: ./gradlew recordRoborazziDebug
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
// Plain Application: the screens need no Hilt graph, only their state.
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi", application = Application::class)
class ReadmeScreenshots {

    @get:Rule val composeRule = createComposeRule()

    private val today = LocalDate.now()
    private val work = TaskList(1, "Work", 0, ListIcon.WORK, 8, 5)
    private val home = TaskList(2, "Home", 1, ListIcon.HOME, 5, 2)
    private val shopping = TaskList(3, "Shopping", 3, ListIcon.CART, 5, 1)
    private val travel = TaskList(4, "Travel", 2, ListIcon.TRAVEL, 3, 2)
    private val health = TaskList(5, "Health", 4, ListIcon.HEALTH, 4, 1)

    private fun task(
        id: Long, title: String, list: TaskList, date: LocalDate? = today, time: LocalTime? = null,
        priority: Priority = Priority.NONE, reminder: Boolean = false, repeat: RepeatRule = RepeatRule.NONE,
        subtasks: List<Subtask> = emptyList(), done: Boolean = false
    ) = Task(
        id = id, title = title, listId = list.id, listName = list.name, listColor = list.color,
        priority = priority, dueDate = date, dueTime = time, reminderAt = if (reminder) 1L else null,
        repeat = repeat, subtasks = subtasks, completedAt = if (done) 1L else null
    )

    private val todayState = TodayContract.UiTodayState(
        loading = false,
        date = today,
        overdue = listOf(task(1, "Pay electricity bill", home, today.minusDays(1), priority = Priority.HIGH)),
        today = listOf(
            task(2, "Design review with Lola", work, time = LocalTime.of(11, 0), priority = Priority.MEDIUM, reminder = true),
            task(3, "Gym — legs", health, time = LocalTime.of(18, 30), reminder = true, repeat = RepeatRule.WEEKDAYS,
                subtasks = listOf(Subtask(1, "Warm up", true), Subtask(2, "Squats"), Subtask(3, "Stretch"))),
            task(4, "Call grandma", home, time = LocalTime.of(20, 0))
        ),
        done = listOf(task(5, "Morning run", health, done = true), task(6, "Reply to Anvar", work, done = true),
            task(7, "Water plants", home, done = true), task(8, "Book tickets", travel, done = true))
    )

    @Test fun onboarding() = capture("onboarding") { OnboardingScreenContent(onAllow = {}, onLater = {}) }

    @Test fun todayLight() = capture("today_light") { WithBar(0) { TodayScreenContent(todayState) {} } }

    @Test fun todayDark() = capture("today_dark", dark = true) { WithBar(0) { TodayScreenContent(todayState.copy(doneExpanded = true)) {} } }

    @Test fun upcoming() = capture("upcoming") {
        WithBar(1) {
            UpcomingScreenContent(
                UpcomingContract.UiUpcomingState(
                    loading = false,
                    today = today,
                    groups = listOf(
                        today.plusDays(1) to listOf(
                            task(10, "Train to Samarkand", travel, today.plusDays(1), LocalTime.of(8, 0), Priority.HIGH, reminder = true),
                            task(11, "Buy groceries", shopping, today.plusDays(1), subtasks = List(5) { Subtask(it.toLong(), "Item") })
                        ),
                        today.plusDays(3) to listOf(
                            task(12, "Sprint planning", work, today.plusDays(3), LocalTime.of(10, 0), Priority.MEDIUM, repeat = RepeatRule.WEEKLY)
                        ),
                        today.plusDays(5) to listOf(task(13, "Dentist", health, today.plusDays(5), LocalTime.of(15, 30), reminder = true))
                    )
                )
            ) {}
        }
    }

    @Test fun lists() = capture("lists") {
        WithBar(2) {
            ListsScreenContent(
                ListsContract.UiListsState(loading = false, lists = listOf(work, home, shopping, travel, health)),
                onEventDispatcher = {},
                onNewList = {}
            )
        }
    }

    @Test fun listDetail() = capture("list_dark", dark = true) {
        ListDetailScreenContent(
            state = ListDetailContract.UiListDetailState(
                listId = 3,
                loading = false,
                list = shopping,
                today = today,
                tasks = listOf(
                    task(20, "Oat milk", shopping, null),
                    task(21, "Eggs ×10", shopping, null),
                    task(22, "Birthday cake", shopping, today.plusDays(1), LocalTime.of(12, 0), Priority.HIGH, reminder = true),
                    task(23, "Coffee beans", shopping, null),
                    task(24, "Bread", shopping, null, done = true)
                )
            ),
            snackbarHostState = SnackbarHostState(),
            onEventDispatcher = {},
            onEdit = {}
        )
    }

    @Test fun editor() = capture("editor") {
        EditorScreenContent(
            state = EditorContract.UiEditorState(
                initialized = true,
                id = 3,
                title = "Gym — legs",
                notes = "Squats, lunges, stretch after",
                lists = listOf(work, home, health),
                listId = 5,
                priority = Priority.MEDIUM,
                dueDate = today,
                dueTime = LocalTime.of(18, 30),
                reminderAt = today.atTime(18, 15).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
                repeat = RepeatRule.WEEKDAYS,
                subtasks = listOf(Subtask(1, "Warm up", true), Subtask(2, "4 × 10 squats"), Subtask(3, "Stretch"))
            ),
            onEventDispatcher = {}
        )
    }

    private val statsState = StatsContract.UiStatsState(
        loading = false,
        stats = Stats(
            doneToday = 4,
            openToday = 4,
            overdue = 1,
            doneThisWeek = 19,
            plannedThisWeek = 26,
            streakDays = 6,
            totalDone = 132,
            last7Days = listOf(3, 5, 2, 0, 6, 4, 4).mapIndexed { index, count -> DayCount(today.minusDays(6L - index), count) },
            lists = listOf(work, home, shopping, travel)
        )
    )

    @Test fun stats() = capture("stats") { WithBar(3) { StatsScreenContent(statsState) {} } }

    @Test fun statsDark() = capture("stats_dark", dark = true) { WithBar(3) { StatsScreenContent(statsState) {} } }

    @Test fun settings() = capture("settings") {
        SettingsScreenContent(
            state = SettingsContract.UiSettingsState(settings = Settings(onboarded = true)),
            notificationsOn = true,
            onEventDispatcher = {},
            onOpenNotifications = {},
            onOpenExactAlarms = {}
        )
    }

    /** The real app draws the tabs in MainScreen; this mirrors that bar for still images. */
    @Composable
    private fun WithBar(selected: Int, content: @Composable () -> Unit) {
        val items = listOf("Today" to Icons.Rounded.WbSunny, "Upcoming" to Icons.Rounded.CalendarMonth, "Lists" to Icons.Rounded.GridView, "Stats" to Icons.Rounded.Insights)
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 0.dp) {
                    items.forEachIndexed { index, (title, icon) ->
                        NavigationBarItem(
                            selected = index == selected,
                            onClick = {},
                            icon = { Icon(icon, contentDescription = null) },
                            label = { Text(title, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = TickTheme.colors.faint,
                                unselectedTextColor = TickTheme.colors.faint
                            )
                        )
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) { content() }
        }
    }

    private fun capture(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        composeRule.setContent { TickTheme(darkTheme = dark) { Box(Modifier.background(MaterialTheme.colorScheme.background)) { content() } } }
        composeRule.onRoot().captureRoboImage("../docs/screenshots/$name.png")
    }
}

