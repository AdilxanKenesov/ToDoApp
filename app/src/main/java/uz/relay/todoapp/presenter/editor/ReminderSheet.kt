package uz.relay.todoapp.presenter.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import uz.relay.todoapp.domain.model.RepeatRule
import uz.relay.todoapp.ui.components.TickChip
import uz.relay.todoapp.ui.theme.TickTheme
import uz.relay.todoapp.utils.atTimeMillis
import uz.relay.todoapp.utils.reminderLabel
import uz.relay.todoapp.utils.toLocalDateTime
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit

private data class Preset(val title: String, val at: Long)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReminderSheet(
    initialAt: Long?,
    initialRepeat: RepeatRule,
    initialAlarm: Boolean,
    dueDate: LocalDate?,
    dueTime: LocalTime?,
    defaultTime: LocalTime,
    onDismiss: () -> Unit,
    onSave: (at: Long?, repeat: RepeatRule, alarm: Boolean) -> Unit
) {
    val now = System.currentTimeMillis()
    val today = LocalDate.now()
    val presets = buildList {
        if (dueDate != null && dueTime != null) {
            val due = dueDate.atTimeMillis(dueTime)
            if (due > now) add(Preset("At due time", due))
        }
        add(Preset("In 1 hour", now + TimeUnit.HOURS.toMillis(1)))
        val evening = today.atTimeMillis(LocalTime.of(18, 0))
        if (evening > now + TimeUnit.MINUTES.toMillis(15)) add(Preset("This evening", evening))
        add(Preset("Tomorrow", today.plusDays(1).atTimeMillis(defaultTime)))
        add(Preset("Next week", today.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).atTimeMillis(defaultTime)))
    }.take(4)

    var at by rememberSaveable { mutableStateOf(initialAt) }
    var repeat by rememberSaveable { mutableStateOf(initialRepeat) }
    var alarm by rememberSaveable { mutableStateOf(initialAlarm) }
    var pickDate by rememberSaveable { mutableStateOf(false) }
    var pickedDate by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    val custom = at != null && presets.none { it.at == at }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Remind me", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                if (at != null) TickChip(text = "Clear", onClick = { at = null; repeat = RepeatRule.NONE })
            }
            Spacer(modifier = Modifier.height(12.dp))

            presets.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                    row.forEach { preset ->
                        PresetCard(
                            title = preset.title,
                            subtitle = preset.at.reminderLabel(today).removePrefix("Today "),
                            selected = at == preset.at,
                            onClick = { at = preset.at },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
            PresetCard(
                title = if (custom) at!!.reminderLabel(today) else "Pick date & time",
                subtitle = null,
                selected = custom,
                icon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = null) },
                onClick = { pickDate = true },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "REPEAT", style = MaterialTheme.typography.labelSmall, color = TickTheme.colors.muted)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                RepeatRule.entries.forEach { rule ->
                    TickChip(text = rule.title, selected = rule == repeat, onClick = { repeat = rule })
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Alarm, contentDescription = null, tint = TickTheme.colors.coral)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Ring like an alarm", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = if (alarm) "Loud until you react" else "A quiet notification",
                        style = MaterialTheme.typography.bodySmall,
                        color = TickTheme.colors.muted
                    )
                }
                Switch(
                    checked = alarm,
                    onCheckedChange = { alarm = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onSave(at, if (at == null) RepeatRule.NONE else repeat, alarm) },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(text = if (at == null) "No reminder" else "Set reminder", style = MaterialTheme.typography.labelLarge)
            }
        }
    }

    if (pickDate) {
        TickDatePicker(
            initial = at?.toLocalDateTime()?.toLocalDate() ?: dueDate ?: today,
            onDismiss = { pickDate = false },
            onPick = { pickDate = false; pickedDate = it }
        )
    }
    pickedDate?.let { date ->
        TickTimePicker(
            initial = at?.toLocalDateTime()?.toLocalTime() ?: dueTime ?: defaultTime,
            onDismiss = { pickedDate = null },
            onPick = { time ->
                pickedDate = null
                at = date.atTimeMillis(time)
            }
        )
    }
}

@Composable
private fun PresetCard(
    title: String,
    subtitle: String?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null
) {
    val primary = MaterialTheme.colorScheme.primary
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest)
            .then(if (selected) Modifier.border(2.dp, primary, MaterialTheme.shapes.medium) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        icon?.invoke()
        Column {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = if (selected) primary else MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = if (selected) primary else TickTheme.colors.muted)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TickDatePicker(initial: LocalDate, onDismiss: () -> Unit, onPick: (LocalDate) -> Unit) {
    val today = LocalDate.now()
    // The picker speaks UTC midnight.
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                !Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate().isBefore(today)
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { onPick(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) } ?: onDismiss()
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    ) {
        DatePicker(state = state, showModeToggle = false)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TickTimePicker(
    initial: LocalTime,
    onDismiss: () -> Unit,
    onPick: (LocalTime) -> Unit,
    onClear: (() -> Unit)? = null
) {
    val state = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        text = { TimePicker(state = state) },
        confirmButton = { TextButton(onClick = { onPick(LocalTime.of(state.hour, state.minute)) }) { Text("OK") } },
        dismissButton = {
            Row {
                if (onClear != null) TextButton(onClick = onClear) { Text("No time", color = TickTheme.colors.coral) }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
