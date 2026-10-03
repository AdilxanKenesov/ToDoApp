package uz.relay.todoapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import cafe.adriel.voyager.transitions.SlideTransition
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import uz.relay.todoapp.domain.model.ThemeMode
import uz.relay.todoapp.domain.usecase.GetSettingsUseCase
import uz.relay.todoapp.navigation.AppNavHost
import uz.relay.todoapp.navigation.AppNavigationHandler
import uz.relay.todoapp.navigation.AppNavigator
import uz.relay.todoapp.navigation.NotificationDeepLink
import uz.relay.todoapp.presenter.editor.EditorScreen
import uz.relay.todoapp.presenter.splash.SplashScreen
import uz.relay.todoapp.presenter.splash.SplashViewModel
import uz.relay.todoapp.ui.theme.TickTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashViewModel by viewModels()

    @Inject lateinit var navigationHandler: AppNavigationHandler
    @Inject lateinit var appNavigator: AppNavigator
    @Inject lateinit var getSettingsUseCase: GetSettingsUseCase
    @Inject lateinit var deepLink: NotificationDeepLink

    private val themeMode by lazy { getSettingsUseCase().map { it.themeMode }.distinctUntilChanged() }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { !splashViewModel.container.stateFlow.value.ready }
        // Orbit runs onCreate only once the state is collected; reading .value is not enough.
        lifecycleScope.launch { splashViewModel.container.stateFlow.first { it.ready } }

        if (savedInstanceState == null) deepLink.set(intent.target())

        enableEdgeToEdge()
        setContent {
            val theme by themeMode.collectAsStateWithLifecycle(ThemeMode.SYSTEM)

            TickTheme(themeMode = theme) {
                BottomSheetNavigator(
                    sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    sheetBackgroundColor = MaterialTheme.colorScheme.surfaceContainer,
                    sheetContentColor = MaterialTheme.colorScheme.onSurface,
                    scrimColor = Color(0x7314142B)
                ) { sheet ->
                    Navigator(screen = SplashScreen()) { navigator ->
                        LaunchedEffect(navigator) {
                            val host = AppNavHost(navigator, sheet)
                            navigationHandler.backStack.collectLatest { param -> param.invoke(host) }
                        }
                        SlideTransition(navigator)
                    }
                }
            }
        }
    }

    // A notification or the widget opened the app while it was already running.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        when (val target = intent.target()) {
            is NotificationDeepLink.Target.OpenTask -> lifecycleScope.launch { appNavigator.openSheet(EditorScreen(taskId = target.id)) }
            NotificationDeepLink.Target.QuickAdd -> lifecycleScope.launch { appNavigator.openSheet(EditorScreen()) }
            null -> Unit
        }
    }

    private fun Intent.target(): NotificationDeepLink.Target? {
        val taskId = getLongExtra(EXTRA_TASK_ID, -1L)
        return when {
            taskId > 0 -> NotificationDeepLink.Target.OpenTask(taskId)
            getBooleanExtra(QUICK_ADD_KEY, false) -> NotificationDeepLink.Target.QuickAdd
            else -> null
        }
    }

    companion object {
        private const val EXTRA_TASK_ID = "task_id"
        private const val QUICK_ADD_KEY = "quick_add"

        /** Glance passes action parameters as intent extras under their key name. */
        val QUICK_ADD = androidx.glance.action.ActionParameters.Key<Boolean>(QUICK_ADD_KEY)

        fun openTaskIntent(context: Context, taskId: Long): Intent =
            Intent(context, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(EXTRA_TASK_ID, taskId)
    }
}
