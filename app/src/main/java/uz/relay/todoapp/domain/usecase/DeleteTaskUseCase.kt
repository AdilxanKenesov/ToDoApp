package uz.relay.todoapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import uz.relay.todoapp.domain.model.Task

interface DeleteTaskUseCase {
    /** Returns the deleted task so it can be restored with Undo. */
    operator fun invoke(id: Long): Flow<Result<Task>>
}
