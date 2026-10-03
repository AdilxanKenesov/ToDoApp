package uz.relay.todoapp.domain.usecase

import kotlinx.coroutines.flow.Flow

interface ToggleSubtaskUseCase {
    operator fun invoke(subtaskId: Long, done: Boolean): Flow<Result<Unit>>
}
