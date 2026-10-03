package uz.relay.todoapp.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import uz.relay.todoapp.domain.model.TaskDraft
import uz.relay.todoapp.domain.repository.ListRepository
import uz.relay.todoapp.domain.repository.SettingsRepository
import uz.relay.todoapp.domain.usecase.QuickAddUseCase
import uz.relay.todoapp.domain.usecase.SaveTaskUseCase
import uz.relay.todoapp.utils.QuickAddParser
import uz.relay.todoapp.utils.atTimeMillis
import java.time.LocalDate
import javax.inject.Inject

class QuickAddUseCaseImpl @Inject constructor(
    private val listRepository: ListRepository,
    private val settingsRepository: SettingsRepository,
    private val saveTaskUseCase: SaveTaskUseCase
) : QuickAddUseCase {

    // "Call mom tomorrow 18:00 !high #Home": a typed time also sets the reminder.
    override fun invoke(text: String, listId: Long?, date: LocalDate?): Flow<Result<Long>> = flow {
        val parsed = QuickAddParser.parse(text)
        val targetList = parsed.listName?.let { listRepository.findByName(it)?.id }
            ?: listId
            ?: listRepository.firstListId()
        val dueDate = parsed.date ?: date
        val reminder = if (dueDate != null && parsed.time != null) dueDate.atTimeMillis(parsed.time) else null

        val draft = TaskDraft(
            title = parsed.title,
            listId = targetList,
            priority = parsed.priority,
            dueDate = dueDate,
            dueTime = parsed.time,
            reminderAt = reminder?.takeIf { it > System.currentTimeMillis() },
            alarm = settingsRepository.getSettings().alarmByDefault
        )
        emit(saveTaskUseCase(draft).first())
    }.catch {
        emit(Result.failure(it))
    }
}
