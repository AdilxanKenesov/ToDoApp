package uz.relay.todoapp.data.reminder

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Vibration for a ringing task; kept outside the channel so the in-app switch works live. */
@Singleton
class AlarmFeedback @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val vibrator: Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

    fun vibrate(alarm: Boolean) {
        val vibrator = vibrator?.takeIf { it.hasVibrator() } ?: return
        val pattern = if (alarm) longArrayOf(0, 600, 400, 600, 400, 600) else longArrayOf(0, 250, 150, 250)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    fun stop() {
        vibrator?.cancel()
    }
}
