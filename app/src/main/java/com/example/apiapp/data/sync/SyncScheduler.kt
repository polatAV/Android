package com.example.apiapp.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val WORK_NAME = "character_sync_work"
    }

    fun scheduleSync(intervalMinutes: Int) {
        val workManager = WorkManager.getInstance(context)

        if (intervalMinutes <= 0) {
            // отменяем фоновую задачу, если пользователь отключил синхронизацию в настройках
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }

        // синхронизация требует наличия интернет-соединения, чтобы опрашивать api
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            intervalMinutes.toLong(), TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            syncRequest
        )
    }
}
