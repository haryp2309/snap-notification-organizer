package com.haryp.snapnotificationorganizer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat

object NotificationHelper {
    private const val GROUP_KEY = "com.haryp.snapnotificationorganizer.MESSAGE_GROUP"
    private const val CHANNEL_ID = "organized_notifications"

    fun showConversationNotification(
        context: Context,
        senderApp: String,
        sender: String,
        message: String,
        icon: Icon? = null,
        originalIntent: PendingIntent? = null,
        originalId: Int = 0
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        createNotificationChannel(context)

        val personBuilder = Person.Builder()
            .setName(sender)
            .setImportant(true)
            
        if (icon != null) {
            personBuilder.setIcon(IconCompat.createFromIcon(context, icon))
        }

        val person = personBuilder.build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            createShortcut(context, sender, icon)
        }

        val messagingStyle = NotificationCompat.MessagingStyle(person)
            .addMessage(message, System.currentTimeMillis(), person)
            .setConversationTitle("Organized: $senderApp")

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setStyle(messagingStyle)
            .setGroup(GROUP_KEY)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setShortcutId(sender)
            .setLocusId(androidx.core.content.LocusIdCompat(sender))
            .setContentIntent(originalIntent)
            .setAutoCancel(true)

        if (icon != null) {
            notificationBuilder.setLargeIcon(icon)
        }

        val summaryNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Organized Notifications")
            .setSmallIcon(R.drawable.ic_notification_small)
            .setStyle(NotificationCompat.InboxStyle()
                .setSummaryText("New messages"))
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .build()

        val notificationId = (originalId.toString() + senderApp + sender).hashCode()
        notificationManager.notify(notificationId, notificationBuilder.build())
        notificationManager.notify(0, summaryNotification)
    }

    private fun createShortcut(context: Context, sender: String, icon: Icon?) {
        val shortcutManager = context.getSystemService(ShortcutManager::class.java)
        val shortcutIntent = Intent(context, MainActivity::class.java).apply { action = Intent.ACTION_VIEW }

        val shortcutBuilder = ShortcutInfo.Builder(context, sender)
            .setShortLabel(sender)
            .setLongLabel(sender)
            .setIntent(shortcutIntent)
            .setLongLived(true)

        if (icon != null) {
            shortcutBuilder.setIcon(icon)
        } else {
            shortcutBuilder.setIcon(Icon.createWithResource(context, R.drawable.ic_notification_small))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            shortcutManager.pushDynamicShortcut(shortcutBuilder.build())
        }
    }

    private fun createNotificationChannel(context: Context) {
        val name = "Organized Notifications"
        val descriptionText = "Notifications reorganized by the app"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
