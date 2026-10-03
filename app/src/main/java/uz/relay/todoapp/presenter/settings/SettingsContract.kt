package uz.relay.todoapp.presenter.settings

import org.orbitmvi.orbit.OrbitContainerHost
import uz.relay.todoapp.domain.model.Settings
import uz.relay.todoapp.domain.model.ThemeMode
import java.time.LocalTime

interface SettingsContract {
    interface ViewModel : OrbitContainerHost<UiSettingsState, UiSettingsState, SideEffect> {
        fun onEventDispatcher(intent: Intent)
    }

    sealed interface Intent {
        data class SetTheme(val mode: ThemeMode) : Intent
        data class SetDefaultTime(val time: LocalTime) : Intent
        data class SetAlarmByDefault(val enabled: Boolean) : Intent
        data class SetVibration(val enabled: Boolean) : Intent
    }

    sealed interface SideEffect

    data class UiSettingsState(
        val settings: Settings = Settings(),
        val exactAlarmsAllowed: Boolean = true
    )
}
