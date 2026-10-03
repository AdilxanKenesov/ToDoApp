package uz.relay.todoapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface QuickAddUseCase {
    operator fun invoke(text: String, listId: Long? = null, date: LocalDate? = null): Flow<Result<Long>>
}
