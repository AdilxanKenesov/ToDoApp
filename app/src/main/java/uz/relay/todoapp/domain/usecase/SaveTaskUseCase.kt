package uz.relay.todoapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import uz.relay.todoapp.domain.model.TaskDraft

interface SaveTaskUseCase {
    operator fun invoke(draft: TaskDraft): Flow<Result<Long>>
}
