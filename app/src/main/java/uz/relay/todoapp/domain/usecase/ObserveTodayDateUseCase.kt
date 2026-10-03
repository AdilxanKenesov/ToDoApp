package uz.relay.todoapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ObserveTodayDateUseCase {
    operator fun invoke(): Flow<LocalDate>
}
