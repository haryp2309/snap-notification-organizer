package com.haryp.snapnotificationorganizer

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppFilterRepository {
    private const val PREFS_NAME = "app_filter_prefs"
    private const val KEY_ALLOWED_PACKAGES = "allowed_packages"
    private const val KEY_BLOCKED_KEYWORDS = "blocked_keywords"
    private const val KEY_DISMISS_ORIGINAL = "dismiss_original"
    
    private lateinit var prefs: SharedPreferences
    
    private val _allowedPackages = MutableStateFlow<Set<String>>(emptySet())
    val allowedPackages: StateFlow<Set<String>> = _allowedPackages.asStateFlow()

    private val _blockedKeywords = MutableStateFlow<Set<String>>(emptySet())
    val blockedKeywords: StateFlow<Set<String>> = _blockedKeywords.asStateFlow()

    private val _dismissOriginal = MutableStateFlow(false)
    val dismissOriginal: StateFlow<Boolean> = _dismissOriginal.asStateFlow()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _allowedPackages.value = prefs.getStringSet(KEY_ALLOWED_PACKAGES, emptySet()) ?: emptySet()
        _blockedKeywords.value = prefs.getStringSet(KEY_BLOCKED_KEYWORDS, emptySet()) ?: emptySet()
        _dismissOriginal.value = prefs.getBoolean(KEY_DISMISS_ORIGINAL, false)
    }

    fun toggleApp(packageName: String) {
        val current = _allowedPackages.value.toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        _allowedPackages.value = current
        prefs.edit().putStringSet(KEY_ALLOWED_PACKAGES, current).apply()
    }

    fun addKeyword(keyword: String) {
        if (keyword.isBlank()) return
        val current = _blockedKeywords.value.toMutableSet()
        current.add(keyword.lowercase())
        _blockedKeywords.value = current
        prefs.edit().putStringSet(KEY_BLOCKED_KEYWORDS, current).apply()
    }

    fun removeKeyword(keyword: String) {
        val current = _blockedKeywords.value.toMutableSet()
        current.remove(keyword)
        _blockedKeywords.value = current
        prefs.edit().putStringSet(KEY_BLOCKED_KEYWORDS, current).apply()
    }

    fun setDismissOriginal(dismiss: Boolean) {
        _dismissOriginal.value = dismiss
        prefs.edit().putBoolean(KEY_DISMISS_ORIGINAL, dismiss).apply()
    }

    fun isAllowed(packageName: String, title: String, content: String): Boolean {
        // App Filter
        val appAllowed = _allowedPackages.value.isEmpty() || _allowedPackages.value.contains(packageName)
        if (!appAllowed) return false

        // Keyword Filter
        val blocked = _blockedKeywords.value.any { 
            title.lowercase().contains(it) || content.lowercase().contains(it) 
        }
        
        return !blocked
    }
}
