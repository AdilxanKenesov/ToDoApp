package uz.relay.todoapp.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import uz.relay.todoapp.domain.repository.TaskRepository
import uz.relay.todoapp.domain.usecase.ToggleSubtaskUseCase
import javax.inject.Inject

class ToggleSubtaskUseCaseImpl @Inject constructor(
    private val repository: TaskRepository
) : ToggleSubtaskUseCase {

    override fun invoke(subtaskId: Long, done: Boolean): Flow<Result<Unit>> = flow {
        repository.setSubtaskDone(subtaskId, done)
        emit(Result.success(Unit))
    }.catch {
        emit(Result.failure(it))
    }
}
