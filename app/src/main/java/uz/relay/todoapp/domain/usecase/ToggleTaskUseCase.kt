package uz.relay.todoapp.domain.usecase

import kotlinx.coroutines.flow.Flow

interface ToggleTaskUseCase {
    operator fun invoke(id: Long, done: Boolean): Flow<Result<Unit>>
}
