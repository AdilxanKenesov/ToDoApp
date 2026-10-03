package uz.relay.todoapp.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import uz.relay.todoapp.domain.model.Task
import uz.relay.todoapp.domain.model.TodayData
import uz.relay.todoapp.domain.repository.SystemRepository
import uz.relay.todoapp.domain.repository.TaskRepository
import uz.relay.todoapp.domain.usecase.GetTodayUseCase
import uz.relay.todoapp.utils.toLocalDateTime
import javax.inject.Inject

class GetTodayUseCaseImpl @Inject constructor(
    private val taskRepository: TaskRepository,
    private val systemRepository: SystemRepository
) : GetTodayUseCase {

    // Re-groups when tasks change and when the date rolls over at midnight.
    override fun invoke(): Flow<TodayData> =
        combine(taskRepository.observeTasks(), systemRepository.observeToday()) { tasks, today ->
            val open = tasks.filter { !it.done }
            TodayData(
                date = today,
                overdue = open.filter { it.isOverdue(today) }.sortedWith(order),
                today = open.filter { it.dueDate == today }.sortedWith(order),
                done = tasks.filter { task ->
                    val completed = task.completedAt?.toLocalDateTime()?.toLocalDate()
                    task.done && completed == today
                }.sortedByDescending { it.completedAt }
            )
        }

    private val order = compareBy<Task>({ it.dueTime == null }, { it.dueTime }, { -it.priority.ordinal }, { it.createdAt })
}
