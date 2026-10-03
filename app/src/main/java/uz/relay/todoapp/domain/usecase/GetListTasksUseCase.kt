package uz.relay.todoapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import uz.relay.todoapp.domain.model.Task

interface GetListTasksUseCase {
    operator fun invoke(listId: Long): Flow<List<Task>>
}
