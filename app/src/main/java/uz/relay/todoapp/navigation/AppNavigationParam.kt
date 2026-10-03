package uz.relay.todoapp.navigation

import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator

/** The two places a command can act on: the screen stack and the bottom sheet. */
class AppNavHost(val navigator: Navigator, val sheet: BottomSheetNavigator)

typealias AppNavigationParam = AppNavHost.() -> Unit
