package it.peugeot.launcher

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.SystemClock
import android.provider.Settings
import android.view.KeyEvent

/** Usa soltanto sessioni multimediali Android standard, senza inventare API XY AUTO. */
class RealMediaBackend(private val context: Context) : CarBackend {
    private val manager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
    private val listener = ComponentName(context, MediaAccessService::class.java)
    private var player: MediaController? = null

    val accessGranted: Boolean
        get() {
            val enabled = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
                ?: return false
            return enabled.split(':').any { ComponentName.unflattenFromString(it) == listener }
        }

    fun refresh() {
        player = if (!accessGranted) null else try {
            manager.getActiveSessions(listener).asSequence()
                .filter { it.packageName != context.packageName }
                .sortedByDescending { it.playbackState?.state == PlaybackState.STATE_PLAYING }
                .firstOrNull { it.metadata != null || it.playbackState != null }
        } catch (_: SecurityException) { null }
          catch (_: IllegalArgumentException) { null }
    }

    val source: String
        get() = player?.let {
            try {
                val app = context.packageManager.getApplicationInfo(it.packageName, 0)
                context.packageManager.getApplicationLabel(app).toString()
            } catch (_: Exception) { it.packageName }
        } ?: "Nessun player"

    override val connected get() = player != null
    override val playing get() = player?.playbackState?.state == PlaybackState.STATE_PLAYING
    override val track: Track?
        get() {
            val m = player?.metadata ?: return null
            val title = m.getString(MediaMetadata.METADATA_KEY_TITLE)
                ?: m.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE) ?: return null
            val artist = m.getString(MediaMetadata.METADATA_KEY_ARTIST)
                ?: m.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST) ?: ""
            val duration = (m.getLong(MediaMetadata.METADATA_KEY_DURATION) / 1000L)
                .coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
            return Track(title, artist, duration)
        }

    override val elapsed: Int
        get() {
            val state = player?.playbackState ?: return 0
            var position = state.position.coerceAtLeast(0L)
            if (state.state == PlaybackState.STATE_PLAYING && state.lastPositionUpdateTime > 0) {
                val delta = (SystemClock.elapsedRealtime() - state.lastPositionUpdateTime).coerceAtLeast(0L)
                position += (delta * state.playbackSpeed).toLong()
            }
            val duration = track?.duration ?: 0
            return (position / 1000L).coerceIn(0L,
                if (duration > 0) duration.toLong() else Int.MAX_VALUE.toLong()).toInt()
        }

    val canSeek get() = player?.playbackState?.let {
        (it.actions and PlaybackState.ACTION_SEEK_TO) != 0L && (track?.duration ?: 0) > 0
    } ?: false

    private fun key(control: MediaController, keyCode: Int) {
        control.dispatchMediaButtonEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        control.dispatchMediaButtonEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
    }

    override fun togglePlayback() {
        val c = player ?: return
        val actions = c.playbackState?.actions ?: 0L
        when {
            playing && (actions and PlaybackState.ACTION_PAUSE) != 0L -> c.transportControls.pause()
            !playing && (actions and PlaybackState.ACTION_PLAY) != 0L -> c.transportControls.play()
            else -> key(c, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        }
    }

    override fun skip(step: Int) {
        val c = player ?: return
        val actions = c.playbackState?.actions ?: 0L
        if (step > 0) {
            if ((actions and PlaybackState.ACTION_SKIP_TO_NEXT) != 0L) c.transportControls.skipToNext()
            else key(c, KeyEvent.KEYCODE_MEDIA_NEXT)
        } else {
            if ((actions and PlaybackState.ACTION_SKIP_TO_PREVIOUS) != 0L) c.transportControls.skipToPrevious()
            else key(c, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
        }
    }

    override fun seek(seconds: Int) {
        if (canSeek) player?.transportControls?.seekTo(seconds.toLong() * 1000)
    }

    // Il dialer del Bluetooth cinese non è una MediaSession.
    override val callNumber get() = ""
    override fun dial(number: String) = Unit
    override fun hangUp() = Unit
}
