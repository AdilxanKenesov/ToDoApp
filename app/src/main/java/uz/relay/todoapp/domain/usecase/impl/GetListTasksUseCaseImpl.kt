package uz.relay.todoapp.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uz.relay.todoapp.domain.model.Task
import uz.relay.todoapp.domain.repository.TaskRepository
import uz.relay.todoapp.domain.usecase.GetListTasksUseCase
import javax.inject.Inject

class GetListTasksUseCaseImpl @Inject constructor(
    private val repository: TaskRepository
) : GetListTasksUseCase {

    // Open tasks first (by date), finished ones at the bottom.
    override fun invoke(listId: Long): Flow<List<Task>> =
        repository.observeListTasks(listId).map { tasks ->
            tasks.sortedWith(
                compareBy<Task>({ it.done }, { it.dueDate == null }, { it.dueDate }, { it.dueTime }, { -it.priority.ordinal }, { it.createdAt })
            )
        }
}
