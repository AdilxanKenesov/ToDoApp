package uz.relay.todoapp.presenter.settings

import uz.relay.todoapp.navigation.AppNavigator
import javax.inject.Inject

class SettingsDirections @Inject constructor(
    private val navigator: AppNavigator
) : SettingsContract.Directions {

    override suspend fun back() {
        navigator.back()
    }
}
