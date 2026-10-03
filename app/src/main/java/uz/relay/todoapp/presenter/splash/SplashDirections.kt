package uz.relay.todoapp.presenter.splash

import uz.relay.todoapp.navigation.AppNavigator
import uz.relay.todoapp.presenter.editor.EditorScreen
import uz.relay.todoapp.presenter.main.MainScreen
import uz.relay.todoapp.presenter.onboarding.OnboardingScreen
import javax.inject.Inject

class SplashDirections @Inject constructor(
    private val navigator: AppNavigator
) : SplashContract.Directions {

    override suspend fun navigateToMain() {
        navigator.replaceAll(MainScreen())
    }

    override suspend fun navigateToOnboarding() {
        navigator.replaceAll(OnboardingScreen())
    }

    override suspend fun openTask(id: Long) {
        navigator.openSheet(EditorScreen(taskId = id))
    }

    override suspend fun openNewTask() {
        navigator.openSheet(EditorScreen())
    }
}
