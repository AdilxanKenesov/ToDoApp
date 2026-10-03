package uz.relay.todoapp.domain.usecase

import kotlinx.coroutines.flow.Flow

interface ObserveExactAlarmsUseCase {
    operator fun invoke(): Flow<Boolean>
}
