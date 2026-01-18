package com.haryp.snapnotificationorganizer

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.haryp.snapnotificationorganizer.ui.theme.SnapNotificationOrganizerTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppFilterRepository.init(this)
        NotificationRepository.init(this)
        enableEdgeToEdge()
        setContent {
            SnapNotificationOrganizerTheme {
                var currentTab by remember { mutableStateOf(0) }
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { currentTab = 0 },
                                icon = { Icon(Icons.Default.Notifications, contentDescription = "Logs") },
                                label = { Text("Logs") }
                            )
                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 },
                                icon = { Icon(Icons.Default.FilterList, contentDescription = "Filter") },
                                label = { Text("Filter") }
                            )
                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { currentTab = 2 },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                label = { Text("Settings") }
                            )
                        }
                    }
                ) { innerPadding ->
                    val notifications by NotificationRepository.notifications.collectAsState()
                    val allowedPackages by AppFilterRepository.allowedPackages.collectAsState()
                    val blockedKeywords by AppFilterRepository.blockedKeywords.collectAsState()
                    val dismissOriginal by AppFilterRepository.dismissOriginal.collectAsState()
                    
                    var isPostNotificationPermissionGranted by remember {
                        mutableStateOf(
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                ContextCompat.checkSelfPermission(
                                    this,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                            } else {
                                true
                            }
                        )
                    }

                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        isPostNotificationPermissionGranted = isGranted
                    }

                    LaunchedEffect(Unit) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isPostNotificationPermissionGranted) {
                            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

                    val isListenerEnabled = isNotificationServiceEnabled()

                    if (!isListenerEnabled || !isPostNotificationPermissionGranted) {
                        PermissionScreen(
                            isListenerEnabled = isListenerEnabled,
                            isPostPermissionGranted = isPostNotificationPermissionGranted,
                            onOpenSettings = { openNotificationSettings() },
                            onRequestPostPermission = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        when (currentTab) {
                            0 -> NotificationLogsScreen(
                                notifications = notifications,
                                modifier = Modifier.padding(innerPadding)
                            )
                            1 -> FilterScreen(
                                allowedPackages = allowedPackages,
                                blockedKeywords = blockedKeywords,
                                onToggleApp = { AppFilterRepository.toggleApp(it) },
                                onAddKeyword = { AppFilterRepository.addKeyword(it) },
                                onRemoveKeyword = { AppFilterRepository.removeKeyword(it) },
                                modifier = Modifier.padding(innerPadding)
                            )
                            2 -> SettingsScreen(
                                dismissOriginal = dismissOriginal,
                                onDismissToggle = { AppFilterRepository.setDismissOriginal(it) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in names.indices) {
                val cn = ComponentName.unflattenFromString(names[i])
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.packageName)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun openNotificationSettings() {
        startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
    }
}

@Composable
fun PermissionScreen(
    isListenerEnabled: Boolean,
    isPostPermissionGranted: Boolean,
    onOpenSettings: () -> Unit,
    onRequestPostPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isListenerEnabled) {
            Text(text = "Step 1: Grant Notification Listener Access")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onOpenSettings) {
                Text(text = "Grant Listener Access")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        if (!isPostPermissionGranted) {
            Text(text = "Step 2: Grant permission to show notifications")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRequestPostPermission) {
                Text(text = "Grant Post Permission")
            }
        }
    }
}

@Composable
fun NotificationLogsScreen(
    notifications: List<OrganizedNotification>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Received Notifications",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (notifications.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(text = "No notifications intercepted yet.")
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(notifications) { notification ->
                    NotificationItem(
                        notification = notification,
                        onResend = {
                            NotificationHelper.showConversationNotification(
                                context = context,
                                senderApp = notification.packageName,
                                sender = notification.sender,
                                message = notification.message,
                                originalId = notification.originalId
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    dismissOriginal: Boolean,
    onDismissToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "General Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Dismiss original notification", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "Automatically clear the notification from the source app",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(checked = dismissOriginal, onCheckedChange = onDismissToggle)
            }
        }
    }
}

@Composable
fun FilterScreen(
    allowedPackages: Set<String>,
    blockedKeywords: Set<String>,
    onToggleApp: (String) -> Unit,
    onAddKeyword: (String) -> Unit,
    onRemoveKeyword: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text(text = "Apps", modifier = Modifier.padding(16.dp))
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text(text = "Keywords", modifier = Modifier.padding(16.dp))
            }
        }
        
        if (selectedTab == 0) {
            AppFilterList(allowedPackages, onToggleApp)
        } else {
            KeywordFilterList(blockedKeywords, onAddKeyword, onRemoveKeyword)
        }
    }
}

@Composable
fun AppFilterList(allowedPackages: Set<String>, onToggleApp: (String) -> Unit) {
    val context = LocalContext.current
    val installedApps = remember {
        context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
            .sortedBy { context.packageManager.getApplicationLabel(it).toString() }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Filter Apps",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Select apps to organize. If none selected, all user apps are organized.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(installedApps) { app ->
                val label = context.packageManager.getApplicationLabel(app).toString()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = allowedPackages.contains(app.packageName),
                        onCheckedChange = { onToggleApp(app.packageName) }
                    )
                    Column {
                        Text(text = label, style = MaterialTheme.typography.bodyLarge)
                        Text(text = app.packageName, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun KeywordFilterList(
    blockedKeywords: Set<String>,
    onAddKeyword: (String) -> Unit,
    onRemoveKeyword: (String) -> Unit
) {
    var newKeyword by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Block Keywords",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Notifications containing these words in title or content will be ignored.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newKeyword,
                onValueChange = { newKeyword = it },
                label = { Text("Add keyword") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.size(8.dp))
            Button(onClick = {
                onAddKeyword(newKeyword)
                newKeyword = ""
            }) {
                Text("Add")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(blockedKeywords.toList()) { keyword ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = keyword, modifier = Modifier.weight(1f))
                    IconButton(onClick = { onRemoveKeyword(keyword) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: OrganizedNotification, onResend: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onResend() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = notification.sender,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(notification.timestamp)),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "App: ${notification.packageName}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = notification.message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
