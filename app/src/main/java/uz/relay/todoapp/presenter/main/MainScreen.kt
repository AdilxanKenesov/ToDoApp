package uz.relay.todoapp.presenter.main

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import uz.relay.todoapp.presenter.lists.ListsScreen
import uz.relay.todoapp.presenter.settings.SettingsScreen
import uz.relay.todoapp.presenter.today.TodayScreen
import uz.relay.todoapp.presenter.upcoming.UpcomingScreen
import uz.relay.todoapp.ui.components.LocalSnackbarHostState
import uz.relay.todoapp.ui.components.rememberSnackbarHost
import uz.relay.todoapp.ui.theme.TickTheme

/** The four tabs. Each tab is a Screen with its own Contract / ViewModel. */
class MainScreen : Screen {

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Content() {
        val snackbarHostState = rememberSnackbarHost()

        CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
            TabNavigator(TodayScreen) {
                // While typing in a quick add bar the keyboard takes the bar's place.
                val imeVisible = WindowInsets.isImeVisible
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = 64.dp)) },
                    bottomBar = { if (!imeVisible) TickNavigationBar() },
                    containerColor = MaterialTheme.colorScheme.background
                ) { padding ->
                    Box(modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
                        CurrentTab()
                    }
                }
            }
        }
    }
}

@Composable
private fun TickNavigationBar() {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 0.dp) {
        listOf(TodayScreen, UpcomingScreen, ListsScreen, SettingsScreen).forEach { tab -> TabItem(tab) }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.TabItem(tab: Tab) {
    val navigator = LocalTabNavigator.current
    val options = tab.options
    NavigationBarItem(
        selected = navigator.current.key == tab.key,
        onClick = { navigator.current = tab },
        icon = { options.icon?.let { Icon(painter = it, contentDescription = null) } },
        label = { Text(text = options.title, style = MaterialTheme.typography.labelSmall) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedIconColor = TickTheme.colors.faint,
            unselectedTextColor = TickTheme.colors.faint
        )
    )
}
