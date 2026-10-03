package uz.relay.todoapp.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import uz.relay.todoapp.domain.model.TaskList
import uz.relay.todoapp.domain.repository.ListRepository
import uz.relay.todoapp.domain.usecase.GetListUseCase
import javax.inject.Inject

class GetListUseCaseImpl @Inject constructor(
    private val repository: ListRepository
) : GetListUseCase {

    override fun invoke(id: Long): Flow<TaskList?> = repository.observeList(id)
}
