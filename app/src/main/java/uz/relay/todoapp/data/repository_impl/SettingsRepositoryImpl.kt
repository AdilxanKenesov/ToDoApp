package uz.relay.todoapp.data.repository_impl

import kotlinx.coroutines.flow.Flow
import uz.relay.todoapp.data.local.prefs.SharedManager
import uz.relay.todoapp.domain.model.Settings
import uz.relay.todoapp.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val sharedManager: SharedManager
) : SettingsRepository {

    override fun observeSettings(): Flow<Settings> = sharedManager.observe()

    override fun getSettings(): Settings = sharedManager.read()

    override fun update(settings: Settings) = sharedManager.write(settings)
}
