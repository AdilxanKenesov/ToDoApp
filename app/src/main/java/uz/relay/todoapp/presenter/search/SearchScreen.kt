package uz.relay.todoapp.presenter.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import uz.relay.todoapp.domain.model.Task
import uz.relay.todoapp.presenter.search.SearchContract.Intent
import uz.relay.todoapp.ui.components.EmptyState
import uz.relay.todoapp.ui.components.SectionHeader
import uz.relay.todoapp.ui.components.TaskRow
import uz.relay.todoapp.ui.theme.TickTheme

class SearchScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel: SearchContract.ViewModel = getViewModel<SearchViewModel>()
        val state = viewModel.collectAsState().value
        val snackbarHostState = remember { SnackbarHostState() }

        viewModel.collectSideEffect { sideEffect ->
            when (sideEffect) {
                is SearchContract.SideEffect.ShowMessage -> snackbarHostState.showSnackbar(sideEffect.message)
            }
        }

        SearchScreenContent(state = state, snackbarHostState = snackbarHostState, onEventDispatcher = viewModel::onEventDispatcher)
    }
}

@Composable
internal fun SearchScreenContent(
    state: SearchContract.UiSearchState,
    snackbarHostState: SnackbarHostState,
    onEventDispatcher: (Intent) -> Unit
) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()).imePadding(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "bar") {
                Row(
                    modifier = Modifier.statusBarsPadding().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = { onEventDispatcher(Intent.Back) },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(start = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Search, contentDescription = null, tint = TickTheme.colors.faint)
                        Box(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                            if (state.query.isEmpty()) Text("Search tasks", style = MaterialTheme.typography.bodyMedium, color = TickTheme.colors.faint)
                            BasicTextField(
                                value = state.query,
                                onValueChange = { onEventDispatcher(Intent.Query(it)) },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                modifier = Modifier.fillMaxWidth().focusRequester(focus)
                            )
                        }
                        if (state.query.isNotEmpty()) {
                            IconButton(onClick = { onEventDispatcher(Intent.Query("")) }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = TickTheme.colors.faint)
                            }
                        }
                    }
                }
            }

            if (state.query.isNotBlank()) {
                if (state.results.isEmpty()) {
                    item(key = "none") {
                        EmptyState(
                            title = "No matches",
                            subtitle = "Try another word",
                            icon = Icons.Rounded.SearchOff,
                            modifier = Modifier.fillMaxWidth().padding(top = 64.dp)
                        )
                    }
                } else {
                    item(key = "count") { SectionHeader(title = "${state.results.size} results", count = null) }
                    items(state.results, key = { it.id }) { task ->
                        TaskRow(
                            task = task,
                            today = state.today,
                            onToggle = { onEventDispatcher(Intent.Toggle(task.id, !task.done)) },
                            onClick = { onEventDispatcher(Intent.OpenTask(task.id)) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewSearchScreenContent() {
    TickTheme {
        SearchScreenContent(
            state = SearchContract.UiSearchState(query = "gym", results = listOf(Task(id = 1, title = "Gym — legs", listId = 1))),
            snackbarHostState = SnackbarHostState(),
            onEventDispatcher = {}
        )
    }
}
