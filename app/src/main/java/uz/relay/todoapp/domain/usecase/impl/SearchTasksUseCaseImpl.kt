package uz.relay.todoapp.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import uz.relay.todoapp.domain.model.Task
import uz.relay.todoapp.domain.repository.TaskRepository
import uz.relay.todoapp.domain.usecase.SearchTasksUseCase
import javax.inject.Inject

class SearchTasksUseCaseImpl @Inject constructor(
    private val repository: TaskRepository
) : SearchTasksUseCase {

    override fun invoke(query: String): Flow<List<Task>> {
        val clean = query.trim()
        return if (clean.isEmpty()) flowOf(emptyList()) else repository.search(clean)
    }
}
