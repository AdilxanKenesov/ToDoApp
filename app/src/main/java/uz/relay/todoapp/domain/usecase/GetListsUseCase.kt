package uz.relay.todoapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import uz.relay.todoapp.domain.model.TaskList

interface GetListsUseCase {
    operator fun invoke(): Flow<List<TaskList>>
}
