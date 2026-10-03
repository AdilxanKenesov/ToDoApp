package uz.relay.todoapp.presenter.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.core.screen.Screen
import uz.relay.todoapp.ui.theme.LightPalette
import uz.relay.todoapp.ui.theme.TickTheme

// The system splash shows the logo; this only fills the frame until the first real screen.
class SplashScreen : Screen {

    @Composable
    override fun Content() {
        SplashScreenContent()
    }
}

@Composable
private fun SplashScreenContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightPalette.Primary)
    )
}

@Preview
@Composable
private fun PreviewSplashScreenContent() {
    TickTheme { SplashScreenContent() }
}
