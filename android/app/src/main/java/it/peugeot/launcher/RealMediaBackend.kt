package it.peugeot.launcher

import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent

/**
 * Usa soltanto i normali tasti multimediali Android, senza accesso
 * alle notifiche o a dati privati. Il firmware Bluetooth XY AUTO
 * potrebbe non gestire questi tasti.
 */
class RealMediaBackend(context: Context) : CarBackend {
    private val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    // Questo indica che si possono inviare tasti, non che il Bluetooth sia collegato.
    override val connected = true
    override val playing: Boolean get() = audio.isMusicActive
    override val track: Track? = null
    override val elapsed = 0
    override val callNumber = ""
    val canSeek = false

    fun refresh() = Unit

    private fun key(code: Int) {
        audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, code))
        audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, code))
    }

    override fun togglePlayback() = key(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
    override fun skip(step: Int) =
        key(if (step > 0) KeyEvent.KEYCODE_MEDIA_NEXT else KeyEvent.KEYCODE_MEDIA_PREVIOUS)

    override fun seek(seconds: Int) = Unit
    override fun dial(number: String) = Unit
    override fun hangUp() = Unit
}
