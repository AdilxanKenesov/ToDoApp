package uz.relay.todoapp.domain.model

import java.time.LocalDate

data class Stats(
    val doneToday: Int = 0,
    /** Still open and due today or earlier. */
    val openToday: Int = 0,
    val overdue: Int = 0,
    val doneThisWeek: Int = 0,
    /** Done this week plus what is still open and due this week. */
    val plannedThisWeek: Int = 0,
    /** Days in a row with at least one check-off, ending today or yesterday. */
    val streakDays: Int = 0,
    val totalDone: Int = 0,
    val last7Days: List<DayCount> = emptyList(),
    val lists: List<TaskList> = emptyList()
) {
    val weekProgress: Float get() = if (plannedThisWeek == 0) 0f else doneThisWeek / plannedThisWeek.toFloat()
}

data class DayCount(val date: LocalDate, val count: Int)
