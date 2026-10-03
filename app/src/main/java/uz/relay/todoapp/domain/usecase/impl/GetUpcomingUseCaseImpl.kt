package uz.relay.todoapp.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import uz.relay.todoapp.domain.model.Task
import uz.relay.todoapp.domain.repository.SystemRepository
import uz.relay.todoapp.domain.repository.TaskRepository
import uz.relay.todoapp.domain.usecase.GetUpcomingUseCase
import javax.inject.Inject

class GetUpcomingUseCaseImpl @Inject constructor(
    private val taskRepository: TaskRepository,
    private val systemRepository: SystemRepository
) : GetUpcomingUseCase {

    override fun invoke(): Flow<List<Task>> =
        combine(taskRepository.observeTasks(), systemRepository.observeToday()) { tasks, today ->
            tasks.filter { !it.done && it.dueDate != null && it.dueDate >= today }
                .sortedWith(compareBy<Task>({ it.dueDate }, { it.dueTime == null }, { it.dueTime }, { -it.priority.ordinal }))
        }
}
