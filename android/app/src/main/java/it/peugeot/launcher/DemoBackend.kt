package it.peugeot.launcher

data class Track(val title: String, val artist: String, val duration: Int)

/** Un adattatore XY AUTO futuro dovrà esporre solo capacità verificate sul dispositivo. */
interface CarBackend {
    val connected: Boolean
    val track: Track?
    val playing: Boolean
    val elapsed: Int
    val callNumber: String
    fun togglePlayback()
    fun skip(step: Int)
    fun seek(seconds: Int)
    fun dial(number: String)
    fun hangUp()
}

class DemoBackend : CarBackend {
    private val tracks = listOf(
        Track("Midnight Drive", "Northline", 228),
        Track("Luci di città", "Via Notturna", 196),
        Track("Blue Hour", "Marea", 243)
    )
    var index = 0
        private set
    override var connected = true
        private set
    override val track get() = if (connected) tracks[index] else null
    override var playing = false
        private set
    override var elapsed = 0
        private set
    override var callNumber = ""
        private set
    var projection = false
        private set

    fun connectDemo(value: Boolean) {
        connected = value
        if (!value) {
            playing = false
            callNumber = ""
            projection = false
        }
    }
    override fun togglePlayback() { if (connected) playing = !playing }
    override fun skip(step: Int) {
        if (!connected) return
        index = ((index + step) % tracks.size + tracks.size) % tracks.size
        elapsed = 0
    }
    override fun seek(seconds: Int) {
        if (connected) elapsed = seconds.coerceIn(0, tracks[index].duration)
    }
    override fun dial(number: String) {
        if (connected && number.matches(Regex("[0-9*#]{1,20}"))) callNumber = number
    }
    override fun hangUp() { callNumber = "" }
    fun toggleProjection() { if (connected) projection = !projection }
    fun tick() {
        if (!playing) return
        elapsed++
        if (elapsed >= tracks[index].duration) skip(1)
    }
    fun restore(index: Int, connected: Boolean, playing: Boolean, elapsed: Int, number: String, projection: Boolean) {
        this.index = index.coerceIn(tracks.indices)
        connectDemo(connected)
        this.playing = connected && playing
        this.elapsed = elapsed.coerceIn(0, tracks[this.index].duration)
        if (connected) {
            dial(number)
            this.projection = projection
        }
    }
}

/** Stato esplicito di indisponibilità finché le API del firmware non sono verificate. */
class UnavailableBackend : CarBackend {
    override val connected = false
    override val track: Track? = null
    override val playing = false
    override val elapsed = 0
    override val callNumber = ""
    override fun togglePlayback() = Unit
    override fun skip(step: Int) = Unit
    override fun seek(seconds: Int) = Unit
    override fun dial(number: String) = Unit
    override fun hangUp() = Unit
}
