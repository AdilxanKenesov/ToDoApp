package uz.relay.todoapp.domain.usecase.impl

import uz.relay.todoapp.domain.model.Settings
import uz.relay.todoapp.domain.repository.SettingsRepository
import uz.relay.todoapp.domain.usecase.UpdateSettingsUseCase
import javax.inject.Inject

class UpdateSettingsUseCaseImpl @Inject constructor(
    private val repository: SettingsRepository
) : UpdateSettingsUseCase {

    override fun invoke(change: (Settings) -> Settings) {
        repository.update(change(repository.getSettings()))
    }
}
