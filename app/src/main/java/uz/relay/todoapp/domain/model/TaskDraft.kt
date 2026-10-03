package uz.relay.todoapp.domain.model

import java.time.LocalDate
import java.time.LocalTime

/** What the editor hands to SaveTaskUseCase. id == 0 creates a new task. */
data class TaskDraft(
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val listId: Long,
    val priority: Priority = Priority.NONE,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val reminderAt: Long? = null,
    val alarm: Boolean = true,
    val repeat: RepeatRule = RepeatRule.NONE,
    val subtasks: List<Subtask> = emptyList()
)
