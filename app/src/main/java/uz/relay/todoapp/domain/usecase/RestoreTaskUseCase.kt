package uz.relay.todoapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import uz.relay.todoapp.domain.model.Task

interface RestoreTaskUseCase {
    operator fun invoke(task: Task): Flow<Result<Unit>>
}
