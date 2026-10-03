package uz.relay.todoapp.presenter.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getViewModel
import uz.relay.todoapp.presenter.onboarding.OnboardingContract.Intent
import uz.relay.todoapp.ui.theme.TickTheme

class OnboardingScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel: OnboardingContract.ViewModel = getViewModel<OnboardingViewModel>()
        val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            viewModel.onEventDispatcher(Intent.Finish)
        }

        OnboardingScreenContent(
            onAllow = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    viewModel.onEventDispatcher(Intent.Finish)
                }
            },
            onLater = { viewModel.onEventDispatcher(Intent.Finish) }
        )
    }
}

@Composable
internal fun OnboardingScreenContent(onAllow: () -> Unit, onLater: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.weight(0.6f))
        Box(modifier = Modifier.align(Alignment.CenterHorizontally).size(240.dp), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(240.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer))
            Box(
                modifier = Modifier.size(164.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(84.dp))
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-6).dp, y = 22.dp)
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(TickTheme.colors.coral),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
            }
        }
        Spacer(modifier = Modifier.weight(0.5f))
        Text(text = "Plan the day.\nNever miss it.", style = MaterialTheme.typography.displaySmall)
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "Alarms ring right on time.", style = MaterialTheme.typography.bodyLarge, color = TickTheme.colors.muted)
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onAllow,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) {
            Text(text = "Allow notifications", style = MaterialTheme.typography.labelLarge)
        }
        TextButton(onClick = onLater, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Text(text = "Later", style = MaterialTheme.typography.labelLarge, color = TickTheme.colors.muted)
        }
    }
}

@Preview
@Composable
private fun PreviewOnboardingScreenContent() {
    TickTheme { OnboardingScreenContent(onAllow = {}, onLater = {}) }
}
