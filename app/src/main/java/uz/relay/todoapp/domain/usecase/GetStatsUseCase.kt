package uz.relay.todoapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import uz.relay.todoapp.domain.model.Stats

interface GetStatsUseCase {
    operator fun invoke(): Flow<Stats>
}
