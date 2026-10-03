package uz.relay.todoapp.presenter.onboarding

import uz.relay.todoapp.navigation.AppNavigator
import uz.relay.todoapp.presenter.main.MainScreen
import javax.inject.Inject

class OnboardingDirections @Inject constructor(
    private val navigator: AppNavigator
) : OnboardingContract.Directions {

    override suspend fun navigateToMain() {
        navigator.replaceAll(MainScreen())
    }
}
