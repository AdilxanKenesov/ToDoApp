package uz.relay.todoapp.presenter.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import uz.relay.todoapp.domain.model.ListIcon
import uz.relay.todoapp.domain.model.TaskList
import uz.relay.todoapp.presenter.lists.ListsContract.Intent
import uz.relay.todoapp.ui.components.ListBadge
import uz.relay.todoapp.ui.components.LocalSnackbarHostState
import uz.relay.todoapp.ui.theme.TickTheme

object ListsScreen : Tab {

    override val options: TabOptions
        @Composable get() {
            val icon = rememberVectorPainter(Icons.Rounded.GridView)
            return remember { TabOptions(index = 2u, title = "Lists", icon = icon) }
        }

    @Composable
    override fun Content() {
        val viewModel: ListsContract.ViewModel = getViewModel<ListsViewModel>()
        val state = viewModel.collectAsState().value
        val snackbarHostState = LocalSnackbarHostState.current
        var creating by rememberSaveable { mutableStateOf(false) }

        viewModel.collectSideEffect { sideEffect ->
            when (sideEffect) {
                is ListsContract.SideEffect.ShowMessage -> snackbarHostState.showSnackbar(sideEffect.message)
                ListsContract.SideEffect.ListCreated -> creating = false
            }
        }

        ListsScreenContent(
            state = state,
            onEventDispatcher = viewModel::onEventDispatcher,
            onNewList = { creating = true }
        )

        if (creating) {
            ListEditorSheet(
                initial = null,
                onDismiss = { creating = false },
                onSave = { viewModel.onEventDispatcher(Intent.CreateList(it)) }
            )
        }
    }
}

@Composable
internal fun ListsScreenContent(
    state: ListsContract.UiListsState,
    onEventDispatcher: (Intent) -> Unit,
    onNewList: () -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(span = { GridItemSpan(2) }, key = "header") {
            Row(
                modifier = Modifier.statusBarsPadding().padding(top = 14.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${state.lists.size} LISTS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TickTheme.colors.muted
                    )
                    Text(text = "Lists", style = MaterialTheme.typography.headlineMedium)
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
        }

        items(state.lists, key = { it.id }) { list ->
            ListCard(list = list, onClick = { onEventDispatcher(Intent.OpenList(list.id)) })
        }

        item(key = "new") {
            Column(
                modifier = Modifier
                    .height(136.dp)
                    .clip(MaterialTheme.shapes.large)
                    .border(2.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.large)
                    .clickable(onClick = onNewList),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, tint = TickTheme.colors.muted)
                Text(text = "New list", style = MaterialTheme.typography.labelMedium, color = TickTheme.colors.muted)
            }
        }
    }
}

@Composable
private fun ListCard(list: TaskList, onClick: () -> Unit) {
    val color = TickTheme.colors.listColor(list.color)
    val open = list.taskCount - list.doneCount
    Column(
        modifier = Modifier
            .height(136.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        ListBadge(icon = list.icon, color = color)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = list.name, style = MaterialTheme.typography.titleLarge, maxLines = 1)
        Text(
            text = when {
                list.taskCount == 0 -> "Empty"
                open == 0 -> "All done"
                else -> "$open open · ${list.doneCount} done"
            },
            style = MaterialTheme.typography.labelSmall,
            color = TickTheme.colors.muted
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { list.progress },
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            strokeCap = StrokeCap.Round,
            drawStopIndicator = {},
            gapSize = 0.dp,
            modifier = Modifier.fillMaxWidth().height(5.dp)
        )
    }
}

@Preview
@Composable
private fun PreviewListsScreenContent() {
    TickTheme {
        ListsScreenContent(
            state = ListsContract.UiListsState(
                loading = false,
                lists = listOf(
                    TaskList(1, "Work", 0, ListIcon.WORK, 8, 5),
                    TaskList(2, "Home", 1, ListIcon.HOME, 5, 2)
                )
            ),
            onEventDispatcher = {},
            onNewList = {}
        )
    }
}
