package com.haryp.snapnotificationorganizer

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

data class OrganizedNotification(
    val id: Long = System.currentTimeMillis(),
    val packageName: String,
    val sender: String,
    val message: String,
    val originalId: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toJson(): String {
        return JSONObject().apply {
            put("id", id)
            put("packageName", packageName)
            put("sender", sender)
            put("message", message)
            put("originalId", originalId)
            put("timestamp", timestamp)
        }.toString()
    }

    companion object {
        fun fromJson(json: String): OrganizedNotification {
            val obj = JSONObject(json)
            return OrganizedNotification(
                id = obj.getLong("id"),
                packageName = obj.getString("packageName"),
                sender = obj.getString("sender"),
                message = obj.getString("message"),
                originalId = obj.optInt("originalId", 0),
                timestamp = obj.getLong("timestamp")
            )
        }
    }
}

object NotificationRepository {
    private const val PREFS_NAME = "notification_logs"
    private const val KEY_NOTIFICATIONS = "notifications_list"
    private lateinit var prefs: SharedPreferences

    private val _notifications = MutableStateFlow<List<OrganizedNotification>>(emptyList())
    val notifications: StateFlow<List<OrganizedNotification>> = _notifications.asStateFlow()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getStringSet(KEY_NOTIFICATIONS, emptySet()) ?: emptySet()
        _notifications.value = saved.map { OrganizedNotification.fromJson(it) }
            .sortedByDescending { it.timestamp }
    }

    fun addNotification(notification: OrganizedNotification) {
        val current = _notifications.value.toMutableList()
        current.add(0, notification)
        _notifications.value = current
        save()
    }

    private fun save() {
        val set = _notifications.value.map { it.toJson() }.toSet()
        prefs.edit().putStringSet(KEY_NOTIFICATIONS, set).apply()
    }
}
