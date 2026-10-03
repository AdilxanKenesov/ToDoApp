package uz.relay.todoapp.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import uz.relay.todoapp.domain.model.Settings
import uz.relay.todoapp.domain.repository.SettingsRepository
import uz.relay.todoapp.domain.usecase.GetSettingsUseCase
import javax.inject.Inject

class GetSettingsUseCaseImpl @Inject constructor(
    private val repository: SettingsRepository
) : GetSettingsUseCase {

    override fun invoke(): Flow<Settings> = repository.observeSettings()
}
