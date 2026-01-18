# Snap Notification Organizer

This app is vibe coded through Gemini in Android Studio.

Snap Notification Organizer is a utility specifically designed to intercept **Snapchat** notifications and reorganize them into modern, grouped conversation-style notifications. While optimized for Snapchat, it may also work with other messaging apps. It helps you keep your notification tray clean while ensuring you never miss an important message.

## Features

- **Snapchat Optimized**: Tailored to handle the unique way Snapchat sends notifications.
- **Conversation Notifications**: Transforms standard notifications into interactive conversation threads (Android 11+).
- **App Filtering**: Choose exactly which apps you want the organizer to handle (Snapchat is the primary target).
- **Keyword Blocking**: Automatically ignore notifications containing specific keywords or phrases in the title or content.
- **Auto-Dismiss**: Optionally clear the original notification from the source app as soon as it's reorganized.
- **Notification Logs**: View a persistent history of all reorganized notifications.
- **Resend History**: Tap any notification in the logs to immediately repost it to your notification drawer.
- **Persistent Storage**: Your logs and settings are saved across app restarts and device reboots.

## How to Use

### 1. Initial Setup
When you first open the app, you will be prompted to grant two essential permissions:
- **Step 1: Grant Listener Access**: This allows the app to "read" incoming notifications from other apps. You will be redirected to the system settings to enable "Snap Notification Organizer".
- **Step 2: Grant Post Permission**: Required for the app to "write" (repost) the new organized notifications to your tray.

### 2. Configure Filters
Navigate to the **Filter** tab (the list icon in the bottom bar):
- **Apps Sub-tab**: Check **Snapchat** (and any other apps you want to organize). If no apps are selected, the organizer will process notifications from all user-installed apps.
- **Keywords Sub-tab**: Add any words (like "Marketing", "Promo", etc.) that you want to use to automatically ignore specific notifications.

### 3. General Settings
Navigate to the **Settings** tab:
- **Dismiss original notification**: Toggle this on if you want the app to automatically clear the original Snapchat notification once it has been reorganized.

### 4. Viewing Logs
The **Logs** tab (the bell icon) shows a live feed of all processed notifications.
- Tapping on any notification in this list will **resend** it to your notification drawer.
- Tapping the notification in the drawer will trigger the original action (e.g., opening the specific chat in Snapchat).

## Privacy Note
This app works entirely locally on your device. Notification content is read and processed only to create the reorganized view and is stored locally in your app's private storage. No data is sent to external servers.

## Attributions
- The app icon is based on the [Ghost SVG Vector](https://www.svgrepo.com/svg/535418/ghost) from SVG Repo, created by **Noah Jacobus**.
