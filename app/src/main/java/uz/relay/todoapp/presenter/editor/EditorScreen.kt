package uz.relay.todoapp.presenter.editor

import android.Manifest
import android.content.Intent as AndroidIntent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.hilt.getViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import uz.relay.todoapp.domain.model.Priority
import uz.relay.todoapp.domain.model.RepeatRule
import uz.relay.todoapp.domain.model.Subtask
import uz.relay.todoapp.domain.model.TaskList
import uz.relay.todoapp.presenter.editor.EditorContract.Intent
import uz.relay.todoapp.ui.components.ListTag
import uz.relay.todoapp.ui.components.TaskCheckbox
import uz.relay.todoapp.ui.components.TickChip
import uz.relay.todoapp.ui.theme.TickTheme
import uz.relay.todoapp.utils.label
import uz.relay.todoapp.utils.reminderLabel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

/** Shown in the app's bottom sheet. taskId == 0 creates a task. */
data class EditorScreen(
    private val taskId: Long = 0,
    private val listId: Long? = null,
    private val date: LocalDate? = null
) : Screen {

    // A fresh key per sheet, so opening the editor twice never reuses an old ViewModel.
    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val viewModel: EditorContract.ViewModel = getViewModel<EditorViewModel>()
        val state = viewModel.collectAsState().value
        val context = LocalContext.current
        var askExact by remember { mutableStateOf(false) }
        val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

        LaunchedEffect(taskId) { viewModel.onEventDispatcher(Intent.Init(taskId, listId, date)) }

        viewModel.collectSideEffect { sideEffect ->
            when (sideEffect) {
                // The sheet sits above the snackbar host, so a toast is the visible option here.
                is EditorContract.SideEffect.ShowMessage -> Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
                EditorContract.SideEffect.CheckAlarmPermissions -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        !context.getSystemService(android.app.AlarmManager::class.java).canScheduleExactAlarms()
                    ) {
                        askExact = true
                    }
                }
            }
        }

        EditorScreenContent(state = state, onEventDispatcher = viewModel::onEventDispatcher)

        if (askExact) {
            AlertDialog(
                onDismissRequest = { askExact = false },
                icon = { Icon(Icons.Rounded.Alarm, contentDescription = null, tint = TickTheme.colors.coral) },
                title = { Text("Ring on time") },
                text = { Text("Allow alarms so Tick can ring at the exact minute.") },
                confirmButton = {
                    TextButton(onClick = {
                        askExact = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            context.startActivity(
                                AndroidIntent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))
                            )
                        }
                    }) { Text("Allow") }
                },
                dismissButton = { TextButton(onClick = { askExact = false }) { Text("Not now") } }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun EditorScreenContent(
    state: EditorContract.UiEditorState,
    onEventDispatcher: (Intent) -> Unit
) {
    val colors = TickTheme.colors
    val today = LocalDate.now()
    val titleFocus = remember { FocusRequester() }
    var dateMenu by remember { mutableStateOf(false) }
    var priorityMenu by remember { mutableStateOf(false) }
    var listMenu by remember { mutableStateOf(false) }
    var pickDate by rememberSaveable { mutableStateOf(false) }
    var pickTime by rememberSaveable { mutableStateOf(false) }
    var reminderSheet by rememberSaveable { mutableStateOf(false) }
    var newSubtask by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state.initialized, state.isNew) {
        if (state.initialized && state.isNew) runCatching { titleFocus.requestFocus() }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 10.dp, bottom = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 38.dp, height = 4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outline)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (state.isNew) "New task" else "Edit task",
                style = MaterialTheme.typography.labelSmall,
                color = colors.muted,
                modifier = Modifier.weight(1f)
            )
            if (!state.isNew) {
                IconButton(onClick = { onEventDispatcher(Intent.Delete) }) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete task", tint = colors.coral)
                }
            }
            IconButton(onClick = { onEventDispatcher(Intent.Close) }) {
                Icon(Icons.Rounded.Close, contentDescription = "Close", tint = colors.muted)
            }
        }

        if (state.loading) {
            Box(modifier = Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        Box {
            if (state.title.isEmpty()) {
                Text(
                    text = "What needs doing?",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (state.titleError) colors.coral else colors.faint
                )
            }
            BasicTextField(
                value = state.title,
                onValueChange = { onEventDispatcher(Intent.ChangeTitle(it)) },
                textStyle = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth().focusRequester(titleFocus)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box {
            if (state.notes.isEmpty()) Text("Notes", style = MaterialTheme.typography.bodyMedium, color = colors.faint)
            BasicTextField(
                value = state.notes,
                onValueChange = { onEventDispatcher(Intent.ChangeNotes(it)) },
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.muted),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth().heightIn(min = 22.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box {
                TickChip(
                    text = state.dueDate?.label(today) ?: "Date",
                    icon = Icons.Rounded.CalendarToday,
                    selected = state.dueDate != null,
                    onClick = { dateMenu = true }
                )
                DropdownMenu(expanded = dateMenu, onDismissRequest = { dateMenu = false }) {
                    listOf(
                        "Today" to today,
                        "Tomorrow" to today.plusDays(1),
                        "Next week" to today.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                    ).forEach { (name, value) ->
                        DropdownMenuItem(text = { Text(name) }, onClick = { dateMenu = false; onEventDispatcher(Intent.SetDate(value)) })
                    }
                    DropdownMenuItem(text = { Text("Pick a date…") }, onClick = { dateMenu = false; pickDate = true })
                    if (state.dueDate != null) {
                        DropdownMenuItem(text = { Text("No date", color = colors.coral) }, onClick = { dateMenu = false; onEventDispatcher(Intent.SetDate(null)) })
                    }
                }
            }

            TickChip(
                text = state.dueTime?.label() ?: "Time",
                icon = Icons.Rounded.Schedule,
                selected = state.dueTime != null,
                onClick = { pickTime = true }
            )

            TickChip(
                text = state.reminderAt?.reminderLabel(today) ?: "Remind",
                icon = if (state.alarm) Icons.Rounded.Alarm else Icons.Rounded.NotificationsNone,
                accent = if (state.reminderAt != null) colors.coral else null,
                onClick = { reminderSheet = true }
            )

            if (state.repeat != RepeatRule.NONE) {
                TickChip(text = state.repeat.title, icon = Icons.Rounded.Repeat, onClick = { reminderSheet = true })
            }

            Box {
                TickChip(
                    text = if (state.priority == Priority.NONE) "Priority" else state.priority.title,
                    icon = Icons.Rounded.Flag,
                    accent = if (state.priority == Priority.NONE) null else colors.priorityColor(state.priority),
                    onClick = { priorityMenu = true }
                )
                DropdownMenu(expanded = priorityMenu, onDismissRequest = { priorityMenu = false }) {
                    Priority.entries.reversed().forEach { priority ->
                        DropdownMenuItem(
                            text = { Text(priority.title) },
                            leadingIcon = { Icon(Icons.Rounded.Flag, contentDescription = null, tint = colors.priorityColor(priority)) },
                            trailingIcon = if (priority == state.priority) ({ Icon(Icons.Rounded.Check, contentDescription = null) }) else null,
                            onClick = { priorityMenu = false; onEventDispatcher(Intent.SetPriority(priority)) }
                        )
                    }
                }
            }

            Box {
                Row(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .clickable { listMenu = true }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ListTag(name = state.list?.name ?: "List", color = colors.listColor(state.list?.color ?: 0))
                }
                DropdownMenu(expanded = listMenu, onDismissRequest = { listMenu = false }) {
                    state.lists.forEach { list ->
                        DropdownMenuItem(
                            text = { ListTag(name = list.name, color = colors.listColor(list.color)) },
                            trailingIcon = if (list.id == state.listId) ({ Icon(Icons.Rounded.Check, contentDescription = null) }) else null,
                            onClick = { listMenu = false; onEventDispatcher(Intent.SetList(list.id)) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = if (state.subtasks.isEmpty()) "SUBTASKS" else "SUBTASKS · ${state.subtasks.count { it.done }}/${state.subtasks.size}",
            style = MaterialTheme.typography.labelSmall,
            color = colors.muted
        )
        state.subtasks.forEachIndexed { index, subtask ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                TaskCheckbox(checked = subtask.done, priority = Priority.NONE, onToggle = { onEventDispatcher(Intent.ToggleSubtask(index)) }, size = 18.dp)
                Text(
                    text = subtask.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (subtask.done) colors.faint else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (subtask.done) TextDecoration.LineThrough else null,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onEventDispatcher(Intent.RemoveSubtask(index)) }) {
                    Icon(Icons.Rounded.Close, contentDescription = "Remove subtask", tint = colors.faint, modifier = Modifier.size(18.dp))
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
            Icon(Icons.Rounded.Add, contentDescription = null, tint = colors.faint, modifier = Modifier.padding(horizontal = 9.dp).size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (newSubtask.isEmpty()) Text("Add subtask", style = MaterialTheme.typography.bodyMedium, color = colors.faint)
                BasicTextField(
                    value = newSubtask,
                    onValueChange = { newSubtask = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        onEventDispatcher(Intent.AddSubtask(newSubtask))
                        newSubtask = ""
                    }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (newSubtask.isNotBlank()) {
                    onEventDispatcher(Intent.AddSubtask(newSubtask))
                    newSubtask = ""
                }
                onEventDispatcher(Intent.Save)
            },
            enabled = !state.saving,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text(text = "Save", style = MaterialTheme.typography.labelLarge)
        }
    }

    if (pickDate) {
        TickDatePicker(
            initial = state.dueDate ?: today,
            onDismiss = { pickDate = false },
            onPick = { pickDate = false; onEventDispatcher(Intent.SetDate(it)) }
        )
    }
    if (pickTime) {
        TickTimePicker(
            initial = state.dueTime ?: state.defaultTime,
            onDismiss = { pickTime = false },
            onPick = { pickTime = false; onEventDispatcher(Intent.SetTime(it)) },
            onClear = if (state.dueTime != null) ({ pickTime = false; onEventDispatcher(Intent.SetTime(null)) }) else null
        )
    }
    if (reminderSheet) {
        ReminderSheet(
            initialAt = state.reminderAt,
            initialRepeat = state.repeat,
            initialAlarm = state.alarm,
            dueDate = state.dueDate,
            dueTime = state.dueTime,
            defaultTime = state.defaultTime,
            onDismiss = { reminderSheet = false },
            onSave = { at, repeat, alarm ->
                reminderSheet = false
                onEventDispatcher(Intent.SetReminder(at, repeat, alarm))
            }
        )
    }
}

@Preview
@Composable
private fun PreviewEditorScreenContent() {
    TickTheme {
        EditorScreenContent(
            state = EditorContract.UiEditorState(
                initialized = true,
                id = 1,
                title = "Gym — legs",
                notes = "Squats, lunges, stretch after",
                lists = listOf(TaskList(1, "Health", 4)),
                listId = 1,
                priority = Priority.MEDIUM,
                dueDate = LocalDate.now(),
                dueTime = LocalTime.of(18, 30),
                subtasks = listOf(Subtask(1, "Warm up", true), Subtask(2, "Stretch"))
            ),
            onEventDispatcher = {}
        )
    }
}
