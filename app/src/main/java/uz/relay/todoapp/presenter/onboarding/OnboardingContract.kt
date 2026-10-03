package uz.relay.todoapp.presenter.onboarding

import org.orbitmvi.orbit.OrbitContainerHost

interface OnboardingContract {
    interface ViewModel : OrbitContainerHost<UiOnboardingState, UiOnboardingState, SideEffect> {
        fun onEventDispatcher(intent: Intent)
    }

    sealed interface Intent {
        data object Finish : Intent
    }

    sealed interface SideEffect

    data object UiOnboardingState

    interface Directions {
        suspend fun navigateToMain()
    }
}
