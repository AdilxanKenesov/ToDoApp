package uz.relay.todoapp.domain.usecase

import uz.relay.todoapp.domain.model.Settings

interface UpdateSettingsUseCase {
    operator fun invoke(change: (Settings) -> Settings)
}
