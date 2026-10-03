package uz.relay.todoapp.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import uz.relay.todoapp.domain.repository.ReminderRepository
import uz.relay.todoapp.domain.repository.TaskRepository
import uz.relay.todoapp.domain.usecase.ToggleTaskUseCase
import uz.relay.todoapp.utils.nextOccurrence
import java.time.LocalDate
import javax.inject.Inject

class ToggleTaskUseCaseImpl @Inject constructor(
    private val taskRepository: TaskRepository,
    private val reminderRepository: ReminderRepository
) : ToggleTaskUseCase {

    override fun invoke(id: Long, done: Boolean): Flow<Result<Unit>> = flow {
        val task = taskRepository.getTask(id) ?: error("Task not found")
        val updated = when {
            !done -> task.copy(completedAt = null)
            // A repeating task moves on to its next date and stays open.
            else -> task.nextOccurrence(LocalDate.now()) ?: task.copy(completedAt = System.currentTimeMillis())
        }
        taskRepository.update(updated)
        reminderRepository.sync(updated)
        emit(Result.success(Unit))
    }.catch {
        emit(Result.failure(it))
    }
}
