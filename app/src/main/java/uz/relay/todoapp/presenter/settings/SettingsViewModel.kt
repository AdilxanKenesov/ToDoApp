package uz.relay.todoapp.presenter.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.viewmodel.orbitContainer
import uz.relay.todoapp.domain.usecase.GetSettingsUseCase
import uz.relay.todoapp.domain.usecase.ObserveExactAlarmsUseCase
import uz.relay.todoapp.domain.usecase.UpdateSettingsUseCase
import uz.relay.todoapp.presenter.settings.SettingsContract.Intent
import uz.relay.todoapp.presenter.settings.SettingsContract.SideEffect
import uz.relay.todoapp.presenter.settings.SettingsContract.UiSettingsState
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val directions: SettingsContract.Directions,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val observeExactAlarmsUseCase: ObserveExactAlarmsUseCase
) : ViewModel(), SettingsContract.ViewModel {

    override val container: OrbitContainer<UiSettingsState, UiSettingsState, SideEffect> =
        orbitContainer(UiSettingsState()) {
            // Re-subscribes each time the screen shows, so a permission granted in system
            // settings is picked up on return.
            repeatOnSubscription {
                combine(getSettingsUseCase(), observeExactAlarmsUseCase()) { settings, exact -> settings to exact }
                    .collect { (settings, exact) -> reduce { state.copy(settings = settings, exactAlarmsAllowed = exact) } }
            }
        }

    override fun onEventDispatcher(intent: Intent) {
        when (intent) {
            is Intent.SetAlarmByDefault -> updateSettingsUseCase { it.copy(alarmByDefault = intent.enabled) }
            is Intent.SetVibration -> updateSettingsUseCase { it.copy(vibration = intent.enabled) }
            Intent.Back -> intent { directions.back() }
        }
    }
}
