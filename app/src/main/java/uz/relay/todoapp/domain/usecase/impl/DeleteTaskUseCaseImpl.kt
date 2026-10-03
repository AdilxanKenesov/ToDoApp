package uz.relay.todoapp.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import uz.relay.todoapp.domain.model.Task
import uz.relay.todoapp.domain.repository.ReminderRepository
import uz.relay.todoapp.domain.repository.TaskRepository
import uz.relay.todoapp.domain.usecase.DeleteTaskUseCase
import javax.inject.Inject

class DeleteTaskUseCaseImpl @Inject constructor(
    private val taskRepository: TaskRepository,
    private val reminderRepository: ReminderRepository
) : DeleteTaskUseCase {

    override fun invoke(id: Long): Flow<Result<Task>> = flow {
        val task = taskRepository.getTask(id) ?: error("Task not found")
        reminderRepository.cancel(id)
        taskRepository.delete(id)
        emit(Result.success(task))
    }.catch {
        emit(Result.failure(it))
    }
}
