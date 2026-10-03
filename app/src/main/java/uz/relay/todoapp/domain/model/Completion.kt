package uz.relay.todoapp.domain.model

import java.time.LocalDate

/** A single check-off of a task, kept for statistics. */
data class Completion(
    val taskId: Long?,
    val listId: Long,
    val completedAt: Long,
    val date: LocalDate
)
