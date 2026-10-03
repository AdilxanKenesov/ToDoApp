package uz.relay.todoapp.presenter.settings

import org.orbitmvi.orbit.OrbitContainerHost
import uz.relay.todoapp.domain.model.Settings

interface SettingsContract {
    interface ViewModel : OrbitContainerHost<UiSettingsState, UiSettingsState, SideEffect> {
        fun onEventDispatcher(intent: Intent)
    }

    sealed interface Intent {
        data class SetAlarmByDefault(val enabled: Boolean) : Intent
        data class SetVibration(val enabled: Boolean) : Intent
        data object Back : Intent
    }

    sealed interface SideEffect

    data class UiSettingsState(
        val settings: Settings = Settings(),
        val exactAlarmsAllowed: Boolean = true
    )

    interface Directions {
        suspend fun back()
    }
}
