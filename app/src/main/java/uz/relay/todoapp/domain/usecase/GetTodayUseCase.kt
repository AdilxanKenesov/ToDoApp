package uz.relay.todoapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import uz.relay.todoapp.domain.model.TodayData

interface GetTodayUseCase {
    operator fun invoke(): Flow<TodayData>
}
