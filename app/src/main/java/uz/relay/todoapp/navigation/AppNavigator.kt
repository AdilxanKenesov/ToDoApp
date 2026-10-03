package uz.relay.todoapp.navigation

import cafe.adriel.voyager.core.screen.Screen

interface AppNavigator {
    suspend fun navigateTo(screen: Screen)
    suspend fun replaceAll(screen: Screen)
    suspend fun back()
}
