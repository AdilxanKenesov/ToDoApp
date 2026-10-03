package uz.relay.todoapp.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import uz.relay.todoapp.domain.model.Task
import uz.relay.todoapp.domain.repository.ReminderRepository
import uz.relay.todoapp.domain.repository.TaskRepository
import uz.relay.todoapp.domain.usecase.RestoreTaskUseCase
import javax.inject.Inject

class RestoreTaskUseCaseImpl @Inject constructor(
    private val taskRepository: TaskRepository,
    private val reminderRepository: ReminderRepository
) : RestoreTaskUseCase {

    override fun invoke(task: Task): Flow<Result<Unit>> = flow {
        taskRepository.restore(task)
        reminderRepository.sync(task)
        emit(Result.success(Unit))
    }.catch {
        emit(Result.failure(it))
    }
}
