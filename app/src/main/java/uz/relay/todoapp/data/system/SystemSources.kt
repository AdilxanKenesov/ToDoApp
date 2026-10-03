package uz.relay.todoapp.data.system

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemSources @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    /**
     * Today's date. TIME_TICK arrives every minute while someone listens, so "Today"
     * rolls over at midnight; manual clock and time zone changes are caught too.
     */
    fun today(): Flow<LocalDate> = broadcasts(
        Intent.ACTION_TIME_TICK,
        Intent.ACTION_DATE_CHANGED,
        Intent.ACTION_TIME_CHANGED,
        Intent.ACTION_TIMEZONE_CHANGED
    ) { LocalDate.now() }

    fun exactAlarmsAllowed(): Flow<Boolean> =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            broadcasts { true }
        } else {
            broadcasts(AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED) {
                alarmManager.canScheduleExactAlarms()
            }
        }

    fun canScheduleExact(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    private fun <T> broadcasts(vararg actions: String, read: () -> T): Flow<T> = callbackFlow {
        trySend(read())
        if (actions.isEmpty()) {
            awaitClose { }
            return@callbackFlow
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(read())
            }
        }
        val filter = IntentFilter().apply { actions.forEach(::addAction) }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)

        awaitClose { context.unregisterReceiver(receiver) }
    }.distinctUntilChanged()
}
