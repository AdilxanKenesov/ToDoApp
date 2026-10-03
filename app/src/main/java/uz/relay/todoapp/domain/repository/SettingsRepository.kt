package uz.relay.todoapp.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.relay.todoapp.domain.model.Settings

interface SettingsRepository {
    fun observeSettings(): Flow<Settings>
    fun getSettings(): Settings
    fun update(settings: Settings)
}
