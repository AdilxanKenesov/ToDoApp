package uz.relay.todoapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import uz.relay.todoapp.domain.model.Task

interface GetTaskUseCase {
    operator fun invoke(id: Long): Flow<Task?>
}
