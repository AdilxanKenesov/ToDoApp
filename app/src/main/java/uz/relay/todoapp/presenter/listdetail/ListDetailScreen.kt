package uz.relay.todoapp.presenter.listdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getViewModel
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import uz.relay.todoapp.domain.model.ListIcon
import uz.relay.todoapp.domain.model.Task
import uz.relay.todoapp.domain.model.TaskList
import uz.relay.todoapp.presenter.listdetail.ListDetailContract.Intent
import uz.relay.todoapp.presenter.lists.ListEditorSheet
import uz.relay.todoapp.ui.components.EmptyState
import uz.relay.todoapp.ui.components.ListBadge
import uz.relay.todoapp.ui.components.QuickAddBar
import uz.relay.todoapp.ui.components.SwipeTaskRow
import uz.relay.todoapp.ui.theme.TickTheme

data class ListDetailScreen(private val listId: Long) : Screen {

    @Composable
    override fun Content() {
        val viewModel: ListDetailContract.ViewModel = getViewModel<ListDetailViewModel>()
        val state = viewModel.collectAsState().value
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        var editing by rememberSaveable { mutableStateOf(false) }

        LaunchedEffect(listId) { viewModel.onEventDispatcher(Intent.Init(listId)) }

        viewModel.collectSideEffect { sideEffect ->
            when (sideEffect) {
                is ListDetailContract.SideEffect.ShowMessage -> snackbarHostState.showSnackbar(sideEffect.message)
                is ListDetailContract.SideEffect.ShowUndo -> scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    val result = snackbarHostState.showSnackbar(sideEffect.message, actionLabel = "Undo", duration = SnackbarDuration.Short)
                    if (result == SnackbarResult.ActionPerformed) viewModel.onEventDispatcher(Intent.Undo)
                }
                ListDetailContract.SideEffect.ListSaved -> editing = false
            }
        }

        ListDetailScreenContent(
            state = state,
            snackbarHostState = snackbarHostState,
            onEventDispatcher = viewModel::onEventDispatcher,
            onEdit = { editing = true }
        )

        if (editing && state.list != null) {
            ListEditorSheet(
                initial = state.list,
                onDismiss = { editing = false },
                onSave = { viewModel.onEventDispatcher(Intent.SaveList(it)) }
            )
        }
    }
}

@Composable
internal fun ListDetailScreenContent(
    state: ListDetailContract.UiListDetailState,
    snackbarHostState: SnackbarHostState,
    onEventDispatcher: (Intent) -> Unit,
    onEdit: () -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val list = state.list
    val color = TickTheme.colors.listColor(list?.color ?: 0)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = 64.dp)) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item(key = "top") {
                    Column(modifier = Modifier.statusBarsPadding().padding(top = 8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            RoundButton(onClick = { onEventDispatcher(Intent.Back) }) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                            }
                            Box(modifier = Modifier.weight(1f))
                            Box {
                                RoundButton(onClick = { menu = true }) {
                                    Icon(Icons.Rounded.MoreVert, contentDescription = "More")
                                }
                                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                    DropdownMenuItem(
                                        text = { Text("Edit list") },
                                        leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                                        onClick = { menu = false; onEdit() }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete list", color = TickTheme.colors.coral) },
                                        leadingIcon = { Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = TickTheme.colors.coral) },
                                        onClick = { menu = false; confirmDelete = true }
                                    )
                                }
                            }
                        }
                        if (list != null) {
                            Row(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                ListBadge(icon = list.icon, color = color, size = 52.dp)
                                Column(modifier = Modifier.padding(start = 14.dp)) {
                                    Text(text = list.name, style = MaterialTheme.typography.headlineSmall)
                                    Text(
                                        text = "${list.doneCount} of ${list.taskCount} done",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TickTheme.colors.muted
                                    )
                                }
                            }
                        }
                    }
                }

                items(state.tasks, key = { it.id }) { task ->
                    SwipeTaskRow(
                        task = task,
                        today = state.today,
                        onToggle = { onEventDispatcher(Intent.Toggle(task.id, !task.done)) },
                        onDelete = { onEventDispatcher(Intent.Delete(task.id)) },
                        onClick = { onEventDispatcher(Intent.OpenTask(task.id)) },
                        showList = false,
                        modifier = Modifier.animateItem()
                    )
                }

                if (!state.loading && state.tasks.isEmpty()) {
                    item(key = "empty") {
                        EmptyState(
                            title = "Nothing here yet",
                            subtitle = "Add the first task below",
                            icon = Icons.Rounded.Inbox,
                            modifier = Modifier.fillMaxWidth().padding(top = 56.dp)
                        )
                    }
                }
            }

            QuickAddBar(
                placeholder = "Add to ${list?.name ?: "list"}…",
                onSubmit = { onEventDispatcher(Intent.QuickAdd(it)) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            )
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete “${list?.name.orEmpty()}”?") },
            text = { Text("Its ${list?.taskCount ?: 0} tasks will be deleted too.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onEventDispatcher(Intent.DeleteList)
                }) { Text("Delete", color = TickTheme.colors.coral) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun RoundButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    FilledIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = MaterialTheme.shapes.small,
        content = content
    )
}

@Preview
@Composable
private fun PreviewListDetailScreenContent() {
    TickTheme {
        ListDetailScreenContent(
            state = ListDetailContract.UiListDetailState(
                listId = 1,
                loading = false,
                list = TaskList(1, "Shopping", 3, ListIcon.CART, 5, 1),
                tasks = listOf(Task(id = 1, title = "Oat milk", listId = 1), Task(id = 2, title = "Bread", listId = 1, completedAt = 1))
            ),
            snackbarHostState = SnackbarHostState(),
            onEventDispatcher = {},
            onEdit = {}
        )
    }
}
