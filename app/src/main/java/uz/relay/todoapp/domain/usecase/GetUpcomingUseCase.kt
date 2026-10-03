package uz.relay.todoapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import uz.relay.todoapp.domain.model.Task

interface GetUpcomingUseCase {
    operator fun invoke(): Flow<List<Task>>
}
