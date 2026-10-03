package uz.relay.todoapp.domain.model

import java.time.LocalDate

data class TodayData(
    val date: LocalDate,
    val overdue: List<Task>,
    val today: List<Task>,
    val done: List<Task>
) {
    val total: Int get() = today.size + done.size + overdue.size
    val doneCount: Int get() = done.size
}
