package uz.relay.todoapp.domain.usecase

import kotlinx.coroutines.flow.Flow

interface DeleteListUseCase {
    operator fun invoke(id: Long): Flow<Result<Unit>>
}
