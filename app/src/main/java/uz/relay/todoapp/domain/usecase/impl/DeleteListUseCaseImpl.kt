package uz.relay.todoapp.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import uz.relay.todoapp.domain.repository.ListRepository
import uz.relay.todoapp.domain.repository.ReminderRepository
import uz.relay.todoapp.domain.repository.TaskRepository
import uz.relay.todoapp.domain.usecase.DeleteListUseCase
import javax.inject.Inject

class DeleteListUseCaseImpl @Inject constructor(
    private val listRepository: ListRepository,
    private val taskRepository: TaskRepository,
    private val reminderRepository: ReminderRepository
) : DeleteListUseCase {

    // Tasks go with the list (cascade), so their alarms are cancelled first.
    override fun invoke(id: Long): Flow<Result<Unit>> = flow {
        taskRepository.getListTasks(id).forEach { reminderRepository.cancel(it.id) }
        listRepository.delete(id)
        emit(Result.success(Unit))
    }.catch {
        emit(Result.failure(it))
    }
}
