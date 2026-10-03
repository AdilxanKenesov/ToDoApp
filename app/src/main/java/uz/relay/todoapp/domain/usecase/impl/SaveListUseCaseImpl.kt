package uz.relay.todoapp.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import uz.relay.todoapp.domain.model.TaskList
import uz.relay.todoapp.domain.repository.ListRepository
import uz.relay.todoapp.domain.usecase.SaveListUseCase
import javax.inject.Inject

class SaveListUseCaseImpl @Inject constructor(
    private val repository: ListRepository
) : SaveListUseCase {

    override fun invoke(list: TaskList): Flow<Result<Long>> = flow {
        val name = list.name.trim()
        require(name.isNotEmpty()) { "Name the list" }
        emit(Result.success(repository.save(list.copy(name = name))))
    }.catch {
        emit(Result.failure(it))
    }
}
