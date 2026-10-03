package uz.relay.todoapp.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import uz.relay.todoapp.domain.repository.SystemRepository
import uz.relay.todoapp.domain.usecase.ObserveExactAlarmsUseCase
import javax.inject.Inject

class ObserveExactAlarmsUseCaseImpl @Inject constructor(
    private val repository: SystemRepository
) : ObserveExactAlarmsUseCase {

    override fun invoke(): Flow<Boolean> = repository.observeExactAlarmsAllowed()
}
