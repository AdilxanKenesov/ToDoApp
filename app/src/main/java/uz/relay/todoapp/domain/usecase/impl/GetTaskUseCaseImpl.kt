package uz.relay.todoapp.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import uz.relay.todoapp.domain.model.Task
import uz.relay.todoapp.domain.repository.TaskRepository
import uz.relay.todoapp.domain.usecase.GetTaskUseCase
import javax.inject.Inject

class GetTaskUseCaseImpl @Inject constructor(
    private val repository: TaskRepository
) : GetTaskUseCase {

    override fun invoke(id: Long): Flow<Task?> = repository.observeTask(id)
}
