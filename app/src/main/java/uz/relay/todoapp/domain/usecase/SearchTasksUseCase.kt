package uz.relay.todoapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import uz.relay.todoapp.domain.model.Task

interface SearchTasksUseCase {
    operator fun invoke(query: String): Flow<List<Task>>
}
