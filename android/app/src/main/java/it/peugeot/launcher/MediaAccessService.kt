package it.peugeot.launcher

import android.service.notification.NotificationListenerService

/** Solo abilitazione MediaSession; non memorizza né processa le notifiche. */
class MediaAccessService : NotificationListenerService()
