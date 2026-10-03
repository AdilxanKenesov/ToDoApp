package uz.relay.todoapp.navigation

import cafe.adriel.voyager.navigator.Navigator

/** What a navigation command acts on: the screen stack. */
class AppNavHost(val navigator: Navigator)

typealias AppNavigationParam = AppNavHost.() -> Unit
