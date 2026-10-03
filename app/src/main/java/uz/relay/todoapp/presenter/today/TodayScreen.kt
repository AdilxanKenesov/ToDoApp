package uz.relay.todoapp.presenter.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import uz.relay.todoapp.domain.model.Priority
import uz.relay.todoapp.domain.model.Task
import uz.relay.todoapp.presenter.today.TodayContract.Intent
import uz.relay.todoapp.ui.components.EmptyState
import uz.relay.todoapp.ui.components.HeroCard
import uz.relay.todoapp.ui.components.LocalSnackbarHostState
import uz.relay.todoapp.ui.components.QuickAddBar
import uz.relay.todoapp.ui.components.SectionHeader
import uz.relay.todoapp.ui.components.SwipeTaskRow
import uz.relay.todoapp.ui.theme.TickTheme
import uz.relay.todoapp.utils.headerLabel
import java.time.LocalDate
import java.time.LocalTime

object TodayScreen : Tab {

    override val options: TabOptions
        @Composable get() {
            val icon = rememberVectorPainter(Icons.Rounded.WbSunny)
            return remember { TabOptions(index = 0u, title = "Today", icon = icon) }
        }

    @Composable
    override fun Content() {
        val viewModel: TodayContract.ViewModel = getViewModel<TodayViewModel>()
        val state = viewModel.collectAsState().value
        val snackbarHostState = LocalSnackbarHostState.current
        val scope = rememberCoroutineScope()

        viewModel.collectSideEffect { sideEffect ->
            when (sideEffect) {
                is TodayContract.SideEffect.ShowMessage -> snackbarHostState.showSnackbar(sideEffect.message)
                // Launched so a second delete replaces this snackbar instead of waiting behind it.
                is TodayContract.SideEffect.ShowUndo -> scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    val result = snackbarHostState.showSnackbar(sideEffect.message, actionLabel = "Undo", duration = SnackbarDuration.Short)
                    if (result == SnackbarResult.ActionPerformed) viewModel.onEventDispatcher(Intent.Undo)
                }
            }
        }

        TodayScreenContent(state = state, onEventDispatcher = viewModel::onEventDispatcher)
    }
}

@Composable
internal fun TodayScreenContent(
    state: TodayContract.UiTodayState,
    onEventDispatcher: (Intent) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "header") {
                Column(modifier = Modifier.statusBarsPadding()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = state.date.headerLabel().uppercase(), style = MaterialTheme.typography.labelSmall, color = TickTheme.colors.muted)
                            Text(text = "Today", style = MaterialTheme.typography.headlineMedium)
                        }
                        FilledIconButton(
                            onClick = { onEventDispatcher(Intent.OpenSearch) },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Icon(Icons.Rounded.Search, contentDescription = "Search")
                        }
                    }
                    if (state.total > 0) HeroCard(done = state.done.size, total = state.total, modifier = Modifier.padding(top = 14.dp))
                }
            }

            if (state.overdue.isNotEmpty()) {
                item(key = "overdue") { SectionHeader("Overdue", state.overdue.size, color = TickTheme.colors.coral) }
                items(state.overdue, key = { it.id }) { task -> Row(task, state.date, onEventDispatcher) }
            }
            if (state.today.isNotEmpty()) {
                item(key = "today") { SectionHeader("Today", state.today.size) }
                items(state.today, key = { it.id }) { task -> Row(task, state.date, onEventDispatcher) }
            }

            if (!state.loading && state.openCount == 0) {
                item(key = "empty") {
                    EmptyState(
                        title = if (state.done.isEmpty()) "Fresh start" else "All done",
                        subtitle = if (state.done.isEmpty()) "Add a task below" else "${state.done.size} completed today",
                        modifier = Modifier.fillMaxWidth().padding(top = 56.dp, bottom = 24.dp).animateItem()
                    )
                }
            }

            if (state.done.isNotEmpty()) {
                item(key = "done") {
                    SectionHeader(
                        title = "Done",
                        count = state.done.size,
                        modifier = Modifier.clickable { onEventDispatcher(Intent.ToggleDoneSection) },
                        trailing = {
                            Icon(
                                imageVector = if (state.doneExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = if (state.doneExpanded) "Hide done" else "Show done",
                                tint = TickTheme.colors.faint,
                                modifier = Modifier.padding(start = 4.dp).size(18.dp)
                            )
                        }
                    )
                }
                if (state.doneExpanded) {
                    items(state.done, key = { "done-${it.id}" }) { task -> Row(task, state.date, onEventDispatcher) }
                }
            }
        }

        if (state.loading) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

        QuickAddBar(
            placeholder = "Add a task…",
            onSubmit = { onEventDispatcher(Intent.QuickAdd(it)) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .imePadding()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun androidx.compose.foundation.lazy.LazyItemScope.Row(
    task: Task,
    today: LocalDate,
    onEventDispatcher: (Intent) -> Unit
) {
    SwipeTaskRow(
        task = task,
        today = today,
        onToggle = { onEventDispatcher(Intent.Toggle(task.id, !task.done)) },
        onDelete = { onEventDispatcher(Intent.Delete(task.id)) },
        onClick = { onEventDispatcher(Intent.OpenTask(task.id)) },
        showDate = false,
        modifier = Modifier.animateItem()
    )
}

@Preview
@Composable
private fun PreviewTodayScreenContent() {
    TickTheme {
        TodayScreenContent(
            state = TodayContract.UiTodayState(
                loading = false,
                today = listOf(
                    Task(id = 1, title = "Design review", listId = 1, listName = "Work", priority = Priority.MEDIUM, dueDate = LocalDate.now(), dueTime = LocalTime.of(11, 0)),
                    Task(id = 2, title = "Gym — legs", listId = 1, listName = "Health", listColor = 4, dueDate = LocalDate.now(), dueTime = LocalTime.of(18, 30))
                ),
                done = listOf(Task(id = 3, title = "Morning run", listId = 1, completedAt = 1))
            ),
            onEventDispatcher = {}
        )
    }
}
