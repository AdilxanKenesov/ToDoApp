package uz.relay.todoapp.navigation

import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

object AppNavigationDispatcher : AppNavigator, AppNavigationHandler {

    // A channel keeps commands until the UI collects them, so the splash's first
    // navigation is never lost.
    private val commands = Channel<AppNavigationParam>(Channel.BUFFERED)

    override val backStack: Flow<AppNavigationParam> = commands.receiveAsFlow()

    private suspend fun navigate(param: AppNavigationParam) {
        commands.send(param)
    }

    override suspend fun navigateTo(screen: Screen) = navigate { navigator.push(screen) }

    override suspend fun replaceAll(screen: Screen) = navigate { navigator.replaceAll(screen) }

    override suspend fun back() = navigate { navigator.pop() }
}
