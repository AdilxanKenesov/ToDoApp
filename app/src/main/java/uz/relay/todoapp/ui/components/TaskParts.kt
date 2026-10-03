package uz.relay.todoapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SubdirectoryArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import uz.relay.todoapp.domain.model.Priority
import uz.relay.todoapp.domain.model.RepeatRule
import uz.relay.todoapp.domain.model.Task
import uz.relay.todoapp.ui.theme.TickTheme
import uz.relay.todoapp.utils.label
import java.time.LocalDate

/** Round checkbox: priority-colored ring, springs into a filled check. */
@Composable
fun TaskCheckbox(
    checked: Boolean,
    priority: Priority,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    val haptics = LocalHapticFeedback.current
    val ring = TickTheme.colors.priorityColor(priority)
    val fill by animateColorAsState(
        targetValue = when {
            checked -> MaterialTheme.colorScheme.primary
            priority == Priority.HIGH -> TickTheme.colors.coralSoft
            else -> Color.Transparent
        },
        label = "checkFill"
    )
    val scale by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "checkScale"
    )

    Box(
        modifier = modifier
            .size(size + 16.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (!checked) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggle()
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(fill)
                .border(2.dp, if (checked) MaterialTheme.colorScheme.primary else ring, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = if (checked) "Mark as not done" else "Mark as done",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(size * 0.62f)
                    .scale(scale)
            )
        }
    }
}

@Composable
fun TaskRow(
    task: Task,
    today: LocalDate,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showList: Boolean = true,
    showDate: Boolean = true
) {
    val titleColor by animateColorAsState(
        targetValue = if (task.done) TickTheme.colors.faint else MaterialTheme.colorScheme.onSurface,
        label = "titleColor"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(start = 4.dp, end = 14.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TaskCheckbox(checked = task.done, priority = task.priority, onToggle = onToggle)
        Column(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                color = titleColor,
                textDecoration = if (task.done) TextDecoration.LineThrough else null,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!task.done) TaskMeta(task = task, today = today, showList = showList, showDate = showDate)
        }
    }
}

@Composable
private fun TaskMeta(task: Task, today: LocalDate, showList: Boolean, showDate: Boolean) {
    val late = task.isOverdue(today)
    val items = buildList<@Composable () -> Unit> {
        val dateText = when {
            late || (showDate && task.dueDate != null && task.dueDate != today) -> {
                val date = task.dueDate!!.label(today)
                task.dueTime?.let { "$date ${it.label()}" } ?: date
            }
            task.dueTime != null -> task.dueTime.label()
            else -> null
        }
        if (dateText != null) add { MetaItem(Icons.Rounded.Schedule, dateText, if (late) TickTheme.colors.coral else null) }
        if (task.reminderAt != null) add { MetaItem(if (task.alarm) Icons.Rounded.Alarm else Icons.Rounded.NotificationsNone, null) }
        if (task.repeat != RepeatRule.NONE) add { MetaItem(Icons.Rounded.Repeat, if (task.repeat == RepeatRule.DAILY) null else task.repeat.title) }
        if (task.subtasks.isNotEmpty()) add { MetaItem(Icons.Rounded.SubdirectoryArrowRight, "${task.subtasksDone}/${task.subtasks.size}") }
        if (showList && task.listName.isNotBlank()) add { ListTag(task.listName, TickTheme.colors.listColor(task.listColor)) }
    }
    if (items.isEmpty()) return

    Spacer(modifier = Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        items.forEach { it() }
    }
}

@Composable
private fun MetaItem(icon: ImageVector, text: String?, tint: Color? = null) {
    val color = tint ?: TickTheme.colors.muted
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        if (text != null) Text(text = text, style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1)
    }
}

@Composable
fun ListTag(name: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(color))
        Text(text = name, style = MaterialTheme.typography.labelSmall, color = TickTheme.colors.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/**
 * Swipe right to complete, left to delete. The state is a plain remember keyed on the
 * task's identity and status: a lazy list keeps saveable state per key, so a reused key
 * would otherwise come back already swiped away.
 */
@Composable
fun SwipeTaskRow(
    task: Task,
    today: LocalDate,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showList: Boolean = true,
    showDate: Boolean = true
) {
    val positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold
    val state = remember(task.id, task.done, task.dueDate) {
        SwipeToDismissBoxState(SwipeToDismissBoxValue.Settled, positionalThreshold)
    }

    // SwipeToDismissBox re-runs onDismiss whenever the lambda changes while it is settled,
    // so the callback is stable and fires once per swipe.
    val toggle by rememberUpdatedState(onToggle)
    val delete by rememberUpdatedState(onDelete)
    val onDismiss = remember(state) {
        var handled = false
        { direction: SwipeToDismissBoxValue ->
            if (!handled) {
                handled = true
                if (direction == SwipeToDismissBoxValue.StartToEnd) toggle() else delete()
            }
        }
    }

    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        onDismiss = onDismiss,
        backgroundContent = {
            val toEnd = state.dismissDirection == SwipeToDismissBoxValue.StartToEnd
            val background = if (toEnd) MaterialTheme.colorScheme.primary else TickTheme.colors.coral
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium)
                    .background(background)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (toEnd) Arrangement.Start else Arrangement.End
            ) {
                val content = if (toEnd) MaterialTheme.colorScheme.onPrimary else Color.White
                if (toEnd) {
                    Icon(Icons.Rounded.Check, contentDescription = null, tint = content)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (task.done) "Undo" else "Done", style = MaterialTheme.typography.labelMedium, color = content)
                } else {
                    Text(text = "Delete", style = MaterialTheme.typography.labelMedium, color = content)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = content)
                }
            }
        }
    ) {
        TaskRow(task = task, today = today, onToggle = onToggle, onClick = onClick, showList = showList, showDate = showDate)
    }
}

@Composable
fun SectionHeader(
    title: String,
    count: Int?,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 6.dp, end = 6.dp, top = 18.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleSmall, color = color, modifier = Modifier.weight(1f))
        if (count != null) Text(text = count.toString(), style = MaterialTheme.typography.labelSmall, color = TickTheme.colors.faint)
        trailing?.invoke()
    }
}
