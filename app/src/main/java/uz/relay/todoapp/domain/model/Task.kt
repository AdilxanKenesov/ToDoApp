package uz.relay.todoapp.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class Task(
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val listId: Long,
    val listName: String = "",
    val listColor: Int = 0,
    val priority: Priority = Priority.NONE,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val reminderAt: Long? = null,
    val alarm: Boolean = true,
    val repeat: RepeatRule = RepeatRule.NONE,
    val completedAt: Long? = null,
    val createdAt: Long = 0,
    val lastRungAt: Long? = null,
    val subtasks: List<Subtask> = emptyList()
) {
    val done: Boolean get() = completedAt != null
    val subtasksDone: Int get() = subtasks.count { it.done }

    fun isOverdue(today: LocalDate): Boolean = !done && dueDate != null && dueDate < today
}
