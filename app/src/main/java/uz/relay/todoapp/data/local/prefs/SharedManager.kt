package uz.relay.todoapp.data.local.prefs

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import uz.relay.todoapp.domain.model.Settings
import uz.relay.todoapp.domain.model.ThemeMode
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedManager @Inject constructor(
    private val prefs: SharedPreferences
) {

    fun read(): Settings = Settings(
        themeMode = ThemeMode.from(prefs.getString(KEY_THEME, null)),
        defaultTime = LocalTime.ofSecondOfDay(prefs.getInt(KEY_DEFAULT_TIME, 9 * 60) * 60L),
        alarmByDefault = prefs.getBoolean(KEY_ALARM, true),
        vibration = prefs.getBoolean(KEY_VIBRATION, true),
        onboarded = prefs.getBoolean(KEY_ONBOARDED, false)
    )

    fun write(settings: Settings) {
        prefs.edit {
            putString(KEY_THEME, settings.themeMode.name)
            putInt(KEY_DEFAULT_TIME, settings.defaultTime.hour * 60 + settings.defaultTime.minute)
            putBoolean(KEY_ALARM, settings.alarmByDefault)
            putBoolean(KEY_VIBRATION, settings.vibration)
            putBoolean(KEY_ONBOARDED, settings.onboarded)
        }
    }

    /** Live settings: SharedPreferences' own listener wrapped in a callbackFlow. */
    fun observe(): Flow<Settings> = callbackFlow {
        trySend(read())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> trySend(read()) }
        prefs.registerOnSharedPreferenceChangeListener(listener)

        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    private companion object {
        const val KEY_THEME = "theme_mode"
        const val KEY_DEFAULT_TIME = "default_time"
        const val KEY_ALARM = "alarm_by_default"
        const val KEY_VIBRATION = "vibration"
        const val KEY_ONBOARDED = "onboarded"
    }
}
