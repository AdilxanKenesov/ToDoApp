package uz.relay.todoapp.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import uz.relay.todoapp.domain.model.RepeatRule
import uz.relay.todoapp.domain.model.TaskDraft
import uz.relay.todoapp.domain.repository.ReminderRepository
import uz.relay.todoapp.domain.repository.TaskRepository
import uz.relay.todoapp.domain.usecase.SaveTaskUseCase
import uz.relay.todoapp.utils.toLocalDateTime
import javax.inject.Inject

class SaveTaskUseCaseImpl @Inject constructor(
    private val taskRepository: TaskRepository,
    private val reminderRepository: ReminderRepository
) : SaveTaskUseCase {

    override fun invoke(draft: TaskDraft): Flow<Result<Long>> = flow {
        require(draft.title.isNotBlank()) { "Add a title" }

        // A repeating task needs an anchor date; take it from the reminder if needed.
        val fixed = if (draft.repeat != RepeatRule.NONE && draft.dueDate == null) {
            draft.copy(dueDate = draft.reminderAt?.toLocalDateTime()?.toLocalDate() ?: java.time.LocalDate.now())
        } else draft

        val id = taskRepository.save(fixed.copy(title = fixed.title.trim(), notes = fixed.notes.trim()))
        taskRepository.getTask(id)?.let(reminderRepository::sync)
        emit(Result.success(id))
    }.catch {
        emit(Result.failure(it))
    }
}
