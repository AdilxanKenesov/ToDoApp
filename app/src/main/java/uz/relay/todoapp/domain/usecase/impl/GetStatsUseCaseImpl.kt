package uz.relay.todoapp.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import uz.relay.todoapp.domain.model.Completion
import uz.relay.todoapp.domain.model.DayCount
import uz.relay.todoapp.domain.model.Stats
import uz.relay.todoapp.domain.model.Task
import uz.relay.todoapp.domain.model.TaskList
import uz.relay.todoapp.domain.repository.ListRepository
import uz.relay.todoapp.domain.repository.SystemRepository
import uz.relay.todoapp.domain.repository.TaskRepository
import uz.relay.todoapp.domain.usecase.GetStatsUseCase
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

class GetStatsUseCaseImpl @Inject constructor(
    private val taskRepository: TaskRepository,
    private val listRepository: ListRepository,
    private val systemRepository: SystemRepository
) : GetStatsUseCase {

    override fun invoke(): Flow<Stats> = combine(
        taskRepository.observeTasks(),
        taskRepository.observeCompletions(),
        listRepository.observeLists(),
        systemRepository.observeToday()
    ) { tasks, completions, lists, today -> stats(tasks, completions, lists, today) }

    private fun stats(tasks: List<Task>, completions: List<Completion>, lists: List<TaskList>, today: LocalDate): Stats {
        val perDay = completions.groupingBy { it.date }.eachCount()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEnd = weekStart.plusDays(6)
        val open = tasks.filter { !it.done }

        val doneThisWeek = completions.count { it.date in weekStart..weekEnd }
        val openThisWeek = open.count { it.dueDate != null && it.dueDate <= weekEnd }

        return Stats(
            doneToday = perDay[today] ?: 0,
            openToday = open.count { it.dueDate != null && it.dueDate <= today },
            overdue = open.count { it.isOverdue(today) },
            doneThisWeek = doneThisWeek,
            plannedThisWeek = doneThisWeek + openThisWeek,
            streakDays = streak(perDay, today),
            totalDone = completions.size,
            last7Days = (6L downTo 0L).map { today.minusDays(it) }.map { DayCount(it, perDay[it] ?: 0) },
            lists = lists.filter { it.taskCount > 0 }
        )
    }

    /** A streak still counts while today has no check-off yet. */
    private fun streak(perDay: Map<LocalDate, Int>, today: LocalDate): Int {
        var day = if ((perDay[today] ?: 0) > 0) today else today.minusDays(1)
        var count = 0
        while ((perDay[day] ?: 0) > 0) {
            count++
            day = day.minusDays(1)
        }
        return count
    }
}
