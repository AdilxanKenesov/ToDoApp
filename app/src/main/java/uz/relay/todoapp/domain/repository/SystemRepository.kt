package uz.relay.todoapp.domain.repository

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface SystemRepository {
    /** Today's date; emits again at midnight and when the user changes time or time zone. */
    fun observeToday(): Flow<LocalDate>
    fun observeExactAlarmsAllowed(): Flow<Boolean>
}
