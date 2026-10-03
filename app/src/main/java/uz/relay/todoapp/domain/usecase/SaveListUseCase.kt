package uz.relay.todoapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import uz.relay.todoapp.domain.model.TaskList

interface SaveListUseCase {
    operator fun invoke(list: TaskList): Flow<Result<Long>>
}
