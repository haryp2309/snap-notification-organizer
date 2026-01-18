package com.haryp.snapnotificationorganizer

import android.app.Notification
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationOrganizerService : NotificationListenerService() {

    override fun onCreate() {
        super.onCreate()
        AppFilterRepository.init(this)
        NotificationRepository.init(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        
        if (sbn == null || sbn.packageName == packageName) return

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: "Unknown"
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        
        val largeIcon = sbn.notification.getLargeIcon()
        
        if (!AppFilterRepository.isAllowed(sbn.packageName, title, text)) return

        val contentIntent = sbn.notification.contentIntent

        if (AppFilterRepository.dismissOriginal.value) {
            cancelNotification(sbn.key)
        }

        NotificationRepository.addNotification(
            OrganizedNotification(
                packageName = sbn.packageName,
                sender = title,
                message = text,
                originalId = sbn.id
            )
        )
        
        NotificationHelper.showConversationNotification(
            context = this,
            senderApp = sbn.packageName,
            sender = title,
            message = text,
            icon = largeIcon,
            originalIntent = contentIntent,
            originalId = sbn.id
        )
    }
}
