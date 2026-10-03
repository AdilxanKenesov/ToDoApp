package uz.relay.todoapp.data.local.room

import uz.relay.todoapp.domain.model.Completion
import uz.relay.todoapp.domain.model.ListIcon
import uz.relay.todoapp.domain.model.Priority
import uz.relay.todoapp.domain.model.RepeatRule
import uz.relay.todoapp.domain.model.Subtask
import uz.relay.todoapp.domain.model.Task
import uz.relay.todoapp.domain.model.TaskDraft
import uz.relay.todoapp.domain.model.TaskList
import java.time.LocalDate
import java.time.LocalTime

fun TaskWithDetails.toDomain(): Task = Task(
    id = task.id,
    title = task.title,
    notes = task.notes,
    listId = task.listId,
    listName = list?.name.orEmpty(),
    listColor = list?.color ?: 0,
    priority = Priority.from(task.priority),
    dueDate = task.dueDate?.let(LocalDate::ofEpochDay),
    dueTime = task.dueTime?.let { LocalTime.of(it / 60, it % 60) },
    reminderAt = task.reminderAt,
    alarm = task.alarm,
    repeat = RepeatRule.from(task.repeat),
    completedAt = task.completedAt,
    createdAt = task.createdAt,
    lastRungAt = task.lastRungAt,
    subtasks = subtasks.sortedBy { it.position }.map { Subtask(it.id, it.title, it.done) }
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    notes = notes,
    listId = listId,
    priority = priority.ordinal,
    dueDate = dueDate?.toEpochDay(),
    dueTime = dueTime?.let { it.hour * 60 + it.minute },
    reminderAt = reminderAt,
    alarm = alarm,
    repeat = repeat.name,
    completedAt = completedAt,
    createdAt = createdAt,
    lastRungAt = lastRungAt
)

fun Task.subtaskEntities(): List<SubtaskEntity> =
    subtasks.mapIndexed { index, subtask -> SubtaskEntity(subtask.id, id, subtask.title, subtask.done, index) }

fun TaskDraft.toEntity(existing: TaskEntity?): TaskEntity = TaskEntity(
    id = id,
    title = title,
    notes = notes,
    listId = listId,
    priority = priority.ordinal,
    dueDate = dueDate?.toEpochDay(),
    dueTime = dueTime?.let { it.hour * 60 + it.minute },
    reminderAt = reminderAt,
    alarm = alarm,
    repeat = repeat.name,
    completedAt = existing?.completedAt,
    createdAt = existing?.createdAt ?: System.currentTimeMillis(),
    // A new reminder time has not rung yet.
    lastRungAt = existing?.lastRungAt?.takeIf { existing.reminderAt == reminderAt }
)

fun TaskDraft.subtaskEntities(): List<SubtaskEntity> =
    subtasks.filter { it.title.isNotBlank() }
        .mapIndexed { index, subtask -> SubtaskEntity(subtask.id, id, subtask.title.trim(), subtask.done, index) }

fun TaskListWithCounts.toDomain(): TaskList =
    TaskList(id = id, name = name, color = color, icon = ListIcon.from(icon), taskCount = taskCount, doneCount = doneCount)

fun CompletionEntity.toDomain(): Completion = Completion(
    taskId = taskId,
    listId = listId,
    completedAt = completedAt,
    date = LocalDate.ofEpochDay(date)
)

fun Completion.toEntity(): CompletionEntity = CompletionEntity(
    taskId = taskId,
    listId = listId,
    completedAt = completedAt,
    date = date.toEpochDay()
)
