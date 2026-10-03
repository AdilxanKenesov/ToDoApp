package uz.relay.todoapp.data.reminder

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import uz.relay.todoapp.domain.repository.ReminderRepository

@HiltWorker
class RescheduleWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val reminderRepository: ReminderRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        reminderRepository.rescheduleAll()
        return Result.success()
    }

    companion object {
        private const val NAME = "reschedule_alarms"

        fun enqueue(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                NAME,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<RescheduleWorker>().build()
            )
        }
    }
}
