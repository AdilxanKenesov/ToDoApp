package uz.relay.todoapp.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import uz.relay.todoapp.domain.repository.SystemRepository
import uz.relay.todoapp.domain.usecase.ObserveTodayDateUseCase
import java.time.LocalDate
import javax.inject.Inject

class ObserveTodayDateUseCaseImpl @Inject constructor(
    private val repository: SystemRepository
) : ObserveTodayDateUseCase {

    override fun invoke(): Flow<LocalDate> = repository.observeToday()
}
