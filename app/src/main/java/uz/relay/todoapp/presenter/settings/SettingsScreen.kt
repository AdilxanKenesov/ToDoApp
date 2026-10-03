package uz.relay.todoapp.presenter.settings

import android.content.Intent as AndroidIntent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.AvTimer
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getViewModel
import org.orbitmvi.orbit.compose.collectAsState
import uz.relay.todoapp.presenter.settings.SettingsContract.Intent
import uz.relay.todoapp.ui.theme.TickTheme

class SettingsScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel: SettingsContract.ViewModel = getViewModel<SettingsViewModel>()
        val state = viewModel.collectAsState().value
        val context = LocalContext.current
        var notificationsOn by remember { mutableStateOf(true) }

        // Re-read on resume: the user may have just changed it in system settings.
        LifecycleResumeEffect(Unit) {
            notificationsOn = NotificationManagerCompat.from(context).areNotificationsEnabled()
            onPauseOrDispose { }
        }

        SettingsScreenContent(
            state = state,
            notificationsOn = notificationsOn,
            onEventDispatcher = viewModel::onEventDispatcher,
            onOpenNotifications = {
                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    AndroidIntent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                } else {
                    AndroidIntent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.fromParts("package", context.packageName, null))
                }
                context.startActivity(intent)
            },
            onOpenExactAlarms = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.startActivity(AndroidIntent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}")))
                }
            }
        )
    }
}

@Composable
internal fun SettingsScreenContent(
    state: SettingsContract.UiSettingsState,
    notificationsOn: Boolean,
    onEventDispatcher: (Intent) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenExactAlarms: () -> Unit
) {
    val settings = state.settings
    val colors = TickTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp)
            .padding(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
            FilledIconButton(
                onClick = { onEventDispatcher(Intent.Back) },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = MaterialTheme.shapes.small
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "Settings", style = MaterialTheme.typography.headlineSmall)
        }

        GroupTitle("Reminders")
        Group {
            SettingRow(
                icon = Icons.Rounded.Alarm,
                title = "Loud alarm",
                subtitle = if (settings.alarmByDefault) "New reminders ring until you react" else "New reminders arrive as a quiet notification",
                tint = colors.coral
            ) {
                TickSwitch(checked = settings.alarmByDefault) { onEventDispatcher(Intent.SetAlarmByDefault(it)) }
            }
            Divider()
            SettingRow(icon = Icons.Rounded.Vibration, title = "Vibration") {
                TickSwitch(checked = settings.vibration) { onEventDispatcher(Intent.SetVibration(it)) }
            }
        }

        GroupTitle("Permissions")
        Group {
            SettingRow(
                icon = Icons.Rounded.NotificationsActive,
                title = "Notifications",
                subtitle = if (notificationsOn) "Reminders can show up" else "Reminders are hidden",
                value = if (notificationsOn) "On" else "Turn on",
                valueColor = if (notificationsOn) colors.green else colors.coral,
                onClick = onOpenNotifications
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Divider()
                SettingRow(
                    icon = Icons.Rounded.AvTimer,
                    title = "Exact alarms",
                    subtitle = if (state.exactAlarmsAllowed) "Ring at the exact minute" else "Reminders may come a few minutes late",
                    value = if (state.exactAlarmsAllowed) "On" else "Allow",
                    valueColor = if (state.exactAlarmsAllowed) colors.green else colors.coral,
                    onClick = onOpenExactAlarms
                )
            }
        }

        Group {
            SettingRow(icon = Icons.Rounded.Widgets, title = "Home screen widget", subtitle = "Long-press your home screen → Widgets → Tick")
        }
    }
}

@Composable
private fun GroupTitle(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = TickTheme.colors.muted,
        modifier = Modifier.padding(start = 6.dp, top = 6.dp)
    )
}

@Composable
private fun Group(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) { content() }
}

@Composable
private fun Divider() = HorizontalDivider(color = MaterialTheme.colorScheme.outline)

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    value: String? = null,
    valueColor: Color? = null,
    tint: Color? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val accent = tint ?: MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            if (subtitle != null) Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TickTheme.colors.muted)
        }
        if (value != null) Text(text = value, style = MaterialTheme.typography.labelMedium, color = valueColor ?: TickTheme.colors.muted)
        trailing?.invoke()
    }
}

@Composable
private fun TickSwitch(checked: Boolean, onChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onChange,
        colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
    )
}

@Preview
@Composable
private fun PreviewSettingsScreenContent() {
    TickTheme {
        SettingsScreenContent(
            state = SettingsContract.UiSettingsState(),
            notificationsOn = true,
            onEventDispatcher = {},
            onOpenNotifications = {},
            onOpenExactAlarms = {}
        )
    }
}
