package com.example.apiapp.data.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.apiapp.MainActivity
import com.example.apiapp.data.preferences.SettingsDataStore
import com.example.apiapp.domain.repository.RickAndMortyRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val rickAndMortyRepository: RickAndMortyRepository,
    private val favoriteRepository: com.example.apiapp.domain.repository.FavoriteRepository,
    private val settingsDataStore: SettingsDataStore
) : CoroutineWorker(context, params) {

    companion object {
        private const val CHANNEL_ID = "sync_channel"
        private const val CHANNEL_NAME = "Sync Notifications"
    }

    override suspend fun doWork(): Result = coroutineScope {
        val userId = settingsDataStore.activeUserId.first() ?: return@coroutineScope Result.success()

        val favorites = favoriteRepository.getAllFavourites(userId).first()
        if (favorites.isEmpty()) return@coroutineScope Result.success()

        favorites.map { character ->
            async {
                rickAndMortyRepository.getCharacter(character.id, userId)
                    .onSuccess { freshChar ->
                        if (freshChar.status != character.status) {
                            // обновляем статус персонажа в нашей локальной бд, чтобы синхронизировать состояние
                            favoriteRepository.updateFavoriteCharacter(freshChar, userId)
                            
                            sendNotification(freshChar.name, freshChar.status, freshChar.id)
                        }
                    }
            }
        }.awaitAll()
        Result.success()
    }

    private fun sendNotification(name: String, status: String, characterId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // создаем диплинк для перехода на экран деталей персонажа
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("apiapp://detail/$characterId"),
            context,
            MainActivity::class.java
        )

        // flag_immutable необходим для соблюдения требований сетевой безопасности android
        val pendingIntent = PendingIntent.getActivity(
            context,
            characterId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Character Status Changed")
            .setContentText("Character $name is now $status")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(characterId, notification)
    }
}
