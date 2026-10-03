package uz.relay.todoapp.presenter.stats

import uz.relay.todoapp.navigation.AppNavigator
import uz.relay.todoapp.presenter.settings.SettingsScreen
import javax.inject.Inject

class StatsDirections @Inject constructor(
    private val navigator: AppNavigator
) : StatsContract.Directions {

    override suspend fun openSettings() {
        navigator.navigateTo(SettingsScreen())
    }
}
