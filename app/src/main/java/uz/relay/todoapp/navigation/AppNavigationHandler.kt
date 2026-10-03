package uz.relay.todoapp.navigation

import kotlinx.coroutines.flow.Flow

interface AppNavigationHandler {
    val backStack: Flow<AppNavigationParam>
}
