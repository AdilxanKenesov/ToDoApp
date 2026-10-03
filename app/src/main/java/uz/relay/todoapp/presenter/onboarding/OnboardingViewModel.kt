package uz.relay.todoapp.presenter.onboarding

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.viewmodel.orbitContainer
import uz.relay.todoapp.domain.usecase.UpdateSettingsUseCase
import uz.relay.todoapp.presenter.onboarding.OnboardingContract.Intent
import uz.relay.todoapp.presenter.onboarding.OnboardingContract.SideEffect
import uz.relay.todoapp.presenter.onboarding.OnboardingContract.UiOnboardingState
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val directions: OnboardingContract.Directions,
    private val updateSettingsUseCase: UpdateSettingsUseCase
) : ViewModel(), OnboardingContract.ViewModel {

    override val container: OrbitContainer<UiOnboardingState, UiOnboardingState, SideEffect> =
        orbitContainer(UiOnboardingState)

    override fun onEventDispatcher(intent: Intent) {
        when (intent) {
            Intent.Finish -> intent {
                updateSettingsUseCase { it.copy(onboarded = true) }
                directions.navigateToMain()
            }
        }
    }
}
