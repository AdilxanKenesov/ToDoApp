package uz.relay.todoapp.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import uz.relay.todoapp.domain.model.TaskList
import uz.relay.todoapp.domain.repository.ListRepository
import uz.relay.todoapp.domain.usecase.GetListsUseCase
import javax.inject.Inject

class GetListsUseCaseImpl @Inject constructor(
    private val repository: ListRepository
) : GetListsUseCase {

    override fun invoke(): Flow<List<TaskList>> = repository.observeLists()
}
