package uz.relay.todoapp.presenter.upcoming

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import uz.relay.todoapp.domain.model.Task
import uz.relay.todoapp.presenter.upcoming.UpcomingContract.Intent
import uz.relay.todoapp.ui.components.EmptyState
import uz.relay.todoapp.ui.components.LocalSnackbarHostState
import uz.relay.todoapp.ui.components.QuickAddBar
import uz.relay.todoapp.ui.components.SectionHeader
import uz.relay.todoapp.ui.components.SwipeTaskRow
import uz.relay.todoapp.ui.theme.TickTheme
import uz.relay.todoapp.utils.label
import uz.relay.todoapp.utils.monthLabel
import uz.relay.todoapp.utils.shortDay
import java.time.LocalDate
import java.time.LocalTime

object UpcomingScreen : Tab {

    override val options: TabOptions
        @Composable get() {
            val icon = rememberVectorPainter(Icons.Rounded.CalendarMonth)
            return remember { TabOptions(index = 1u, title = "Upcoming", icon = icon) }
        }

    @Composable
    override fun Content() {
        val viewModel: UpcomingContract.ViewModel = getViewModel<UpcomingViewModel>()
        val state = viewModel.collectAsState().value
        val snackbarHostState = LocalSnackbarHostState.current
        val scope = rememberCoroutineScope()

        viewModel.collectSideEffect { sideEffect ->
            when (sideEffect) {
                is UpcomingContract.SideEffect.ShowMessage -> snackbarHostState.showSnackbar(sideEffect.message)
                is UpcomingContract.SideEffect.ShowUndo -> scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    val result = snackbarHostState.showSnackbar(sideEffect.message, actionLabel = "Undo", duration = SnackbarDuration.Short)
                    if (result == SnackbarResult.ActionPerformed) viewModel.onEventDispatcher(Intent.Undo)
                }
            }
        }

        UpcomingScreenContent(state = state, onEventDispatcher = viewModel::onEventDispatcher)
    }
}

@Composable
internal fun UpcomingScreenContent(
    state: UpcomingContract.UiUpcomingState,
    onEventDispatcher: (Intent) -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Index of a day's header in the LazyColumn: 1 for the strip + rows of earlier groups.
    fun indexOf(date: LocalDate): Int? {
        var index = 1
        state.groups.forEach { (day, tasks) ->
            if (day >= date) return index
            index += tasks.size + 1
        }
        return null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "header") {
                Column(modifier = Modifier.statusBarsPadding()) {
                    Text(
                        text = state.today.monthLabel().uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TickTheme.colors.muted,
                        modifier = Modifier.padding(top = 14.dp)
                    )
                    Text(text = "Upcoming", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(14.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(state.days, key = { it.toEpochDay() }) { day ->
                            DayPill(
                                date = day,
                                selected = day == state.selected,
                                busy = day in state.busyDays,
                                onClick = {
                                    onEventDispatcher(Intent.SelectDate(day))
                                    indexOf(day)?.let { scope.launch { listState.animateScrollToItem(it) } }
                                }
                            )
                        }
                    }
                }
            }

            state.groups.forEach { (date, tasks) ->
                item(key = "h-${date.toEpochDay()}") {
                    SectionHeader(
                        title = date.label(state.today),
                        count = tasks.size,
                        color = if (date == state.selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                }
                items(tasks, key = { it.id }) { task ->
                    SwipeTaskRow(
                        task = task,
                        today = state.today,
                        onToggle = { onEventDispatcher(Intent.Toggle(task.id, !task.done)) },
                        onDelete = { onEventDispatcher(Intent.Delete(task.id)) },
                        onClick = { onEventDispatcher(Intent.OpenTask(task.id)) },
                        showDate = false,
                        modifier = Modifier.animateItem()
                    )
                }
            }

            if (!state.loading && state.groups.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        title = "Clear horizon",
                        subtitle = "Nothing planned yet",
                        icon = Icons.Rounded.EventAvailable,
                        modifier = Modifier.fillMaxWidth().padding(top = 64.dp)
                    )
                }
            }
        }

        if (state.loading) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

        QuickAddBar(
            placeholder = "Add to ${state.selected.label(state.today)}…",
            onSubmit = { onEventDispatcher(Intent.QuickAdd(it)) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .imePadding()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun DayPill(date: LocalDate, selected: Boolean, busy: Boolean, onClick: () -> Unit) {
    val background by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "dayBackground"
    )
    val content = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .width(46.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(background)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = date.shortDay(), style = MaterialTheme.typography.labelSmall, color = if (selected) content.copy(alpha = 0.75f) else TickTheme.colors.muted)
        Text(text = date.dayOfMonth.toString(), style = MaterialTheme.typography.titleLarge, color = content)
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(5.dp)
                .clip(CircleShape)
                .background(if (busy) TickTheme.colors.coral else Color.Transparent)
        )
    }
}

@Preview
@Composable
private fun PreviewUpcomingScreenContent() {
    val today = LocalDate.now()
    TickTheme {
        UpcomingScreenContent(
            state = UpcomingContract.UiUpcomingState(
                loading = false,
                today = today,
                groups = listOf(
                    today.plusDays(1) to listOf(Task(id = 1, title = "Train to Samarkand", listId = 1, dueDate = today.plusDays(1), dueTime = LocalTime.of(8, 0), reminderAt = 1))
                )
            ),
            onEventDispatcher = {}
        )
    }
}
