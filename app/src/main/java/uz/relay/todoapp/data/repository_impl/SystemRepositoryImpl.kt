package uz.relay.todoapp.data.repository_impl

import kotlinx.coroutines.flow.Flow
import uz.relay.todoapp.data.system.SystemSources
import uz.relay.todoapp.domain.repository.SystemRepository
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemRepositoryImpl @Inject constructor(
    private val sources: SystemSources
) : SystemRepository {

    override fun observeToday(): Flow<LocalDate> = sources.today()

    override fun observeExactAlarmsAllowed(): Flow<Boolean> = sources.exactAlarmsAllowed()
}
