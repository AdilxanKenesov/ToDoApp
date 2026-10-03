package uz.relay.todoapp.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import uz.relay.todoapp.domain.model.Completion
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
        if (done == task.done) return@flow emit(Result.success(Unit))

        val now = System.currentTimeMillis()
        val updated = when {
            !done -> task.copy(completedAt = null)
            // A repeating task moves on to its next date and stays open.
            else -> task.nextOccurrence(LocalDate.now()) ?: task.copy(completedAt = now)
        }
        taskRepository.update(updated)
        if (done) {
            taskRepository.addCompletion(Completion(taskId = id, listId = task.listId, completedAt = now, date = LocalDate.now()))
        } else {
            taskRepository.removeLatestCompletion(id)
        }
        reminderRepository.sync(updated)
        emit(Result.success(Unit))
    }.catch {
        emit(Result.failure(it))
    }
}
