package uz.relay.todoapp.presenter.splash

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.viewmodel.orbitContainer
import uz.relay.todoapp.domain.usecase.GetSettingsUseCase
import uz.relay.todoapp.navigation.NotificationDeepLink
import uz.relay.todoapp.presenter.splash.SplashContract.SideEffect
import uz.relay.todoapp.presenter.splash.SplashContract.UiSplashState
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val directions: SplashContract.Directions,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val deepLink: NotificationDeepLink
) : ViewModel(), SplashContract.ViewModel {

    // MainActivity collects stateFlow, which is what starts this onCreate block.
    override val container: OrbitContainer<UiSplashState, UiSplashState, SideEffect> =
        orbitContainer(UiSplashState()) {
            if (getSettingsUseCase().first().onboarded) {
                directions.navigateToMain()
                when (val target = deepLink.consume()) {
                    is NotificationDeepLink.Target.OpenTask -> directions.openTask(target.id)
                    NotificationDeepLink.Target.QuickAdd -> directions.openNewTask()
                    null -> Unit
                }
            } else {
                deepLink.consume()
                directions.navigateToOnboarding()
            }
            reduce { state.copy(ready = true) }
        }
}
