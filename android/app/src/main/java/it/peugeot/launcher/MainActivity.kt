package it.peugeot.launcher

import android.app.Activity
import android.app.AlertDialog
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import java.util.Locale

class MainActivity : Activity() {
    private val demo = DemoBackend()
    private val unavailable = UnavailableBackend()
    private val preferences by lazy { getSharedPreferences("launcher", MODE_PRIVATE) }
    private val links by lazy { AppLinks(this, preferences) }
    private val handler = Handler(Looper.getMainLooper())
    private var demoEnabled = true
    private var page = "home"
    private var number = ""
    private var resumed = false
    private val backend: CarBackend get() = if (demoEnabled) demo else unavailable
    private val tick = object : Runnable {
        override fun run() {
            if (!resumed) return
            val previous = demo.index
            if (demoEnabled) demo.tick()
            findViewById<ClockView>(R.id.clock)?.refresh()
            if (previous != demo.index) update()
            else updateProgress()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        demoEnabled = preferences.getBoolean("demo", true)
        savedInstanceState?.let {
            page = it.getString("page", "home")
            number = it.getString("number", "")
            demo.restore(it.getInt("index"), it.getBoolean("connected", true), it.getBoolean("playing"),
                it.getInt("elapsed"), it.getString("call", ""), it.getBoolean("projection"))
        }
        setContentView(R.layout.activity_main)
        click(R.id.brand) { navigate("home") }
        click(R.id.back) { navigate("home") }
        click(R.id.settings) { navigate("settings") }
        for ((id, destination) in navigation) click(id) { navigate(destination) }
        navigate(page)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.hasCategory(Intent.CATEGORY_HOME)) navigate("home")
    }

    override fun onResume() {
        super.onResume()
        resumed = true
        immersive()
        update()
        findViewById<ClockView>(R.id.clock)?.refresh()
        handler.removeCallbacks(tick)
        handler.postDelayed(tick, 1000)
    }

    override fun onPause() {
        resumed = false
        handler.removeCallbacks(tick)
        super.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) immersive()
    }

    @Suppress("DEPRECATION")
    private fun immersive() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("page", page)
        outState.putString("number", number)
        outState.putInt("index", demo.index)
        outState.putBoolean("connected", demo.connected)
        outState.putBoolean("playing", demo.playing)
        outState.putInt("elapsed", demo.elapsed)
        outState.putString("call", demo.callNumber)
        outState.putBoolean("projection", demo.projection)
        super.onSaveInstanceState(outState)
    }

    @Deprecated("Legacy device back navigation")
    override fun onBackPressed() {
        if (page != "home") navigate("home") else super.onBackPressed()
    }

    private val navigation = listOf(
        R.id.nav_home to "home", R.id.nav_auto to "auto",
        R.id.nav_music to "music", R.id.nav_phone to "phone"
    )

    private fun navigate(destination: String) {
        page = destination
        val layout = when (page) {
            "music" -> R.layout.screen_music
            "phone" -> R.layout.screen_phone
            "auto" -> R.layout.screen_auto
            "settings" -> R.layout.screen_settings
            else -> { page = "home"; R.layout.screen_home }
        }
        findViewById<FrameLayout>(R.id.content).apply {
            removeAllViews()
            layoutInflater.inflate(layout, this, true)
        }
        findViewById<View>(R.id.page_header).visibility = if (page == "home") View.GONE else View.VISIBLE
        text(R.id.page_title, when (page) {
            "music" -> "Musica Bluetooth"
            "phone" -> "Telefono"
            "auto" -> "Android Auto"
            "settings" -> "Impostazioni"
            else -> ""
        })
        navigation.forEach { (id, target) ->
            findViewById<Button>(id).apply {
                isSelected = target == page
                setTextColor(if (isSelected) Color.rgb(240, 199, 126) else Color.rgb(244, 239, 228))
            }
        }
        if (page == "home" || page == "music") bindPlayer()
        if (page == "phone") bindPhone()
        if (page == "settings") bindSettings()
        if (page == "auto") click(R.id.auto_open) {
            if (demoEnabled && demo.connected) { demo.toggleProjection(); update() }
            else if (demoEnabled) navigate("settings")
            else links.open("auto")
        }
        update()
        findViewById<ClockView>(R.id.clock)?.refresh()
    }

    private fun bindPlayer() {
        val ids = if (page == "home")
            listOf(R.id.home_prev, R.id.home_play, R.id.home_next, R.id.home_fallback)
        else listOf(R.id.music_prev, R.id.music_play, R.id.music_next, R.id.music_fallback)
        click(ids[0]) { backend.skip(-1); update() }
        click(ids[1]) { backend.togglePlayback(); update() }
        click(ids[2]) { backend.skip(1); update() }
        click(ids[3]) { if (demoEnabled) navigate("settings") else links.open("bt") }
        findViewById<SeekBar>(R.id.seek)?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) { backend.seek(progress); updateProgress() }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
        })
    }

    private val digits = listOf(
        R.id.digit_1 to "1", R.id.digit_2 to "2", R.id.digit_3 to "3",
        R.id.digit_4 to "4", R.id.digit_5 to "5", R.id.digit_6 to "6",
        R.id.digit_7 to "7", R.id.digit_8 to "8", R.id.digit_9 to "9",
        R.id.digit_star to "*", R.id.digit_0 to "0", R.id.digit_hash to "#"
    )

    private fun bindPhone() {
        digits.forEach { (id, digit) -> click(id) {
            if (number.length < 20 && backend.connected && backend.callNumber.isEmpty()) number += digit
            update()
        } }
        click(R.id.erase) { number = number.dropLast(1); update() }
        click(R.id.call) {
            if (backend.callNumber.isEmpty()) backend.dial(number) else backend.hangUp()
            update()
        }
        click(R.id.phone_fallback) { links.open("phone") }
    }

    private fun bindSettings() {
        click(R.id.demo) {
            demoEnabled = !demoEnabled
            preferences.edit().putBoolean("demo", demoEnabled).apply()
            demo.connectDemo(demoEnabled)
            number = ""
            update()
        }
        click(R.id.connect) { demo.connectDemo(!demo.connected); update() }
        for ((id, key) in listOf(R.id.choose_bt to "bt", R.id.choose_phone to "phone", R.id.choose_auto to "auto")) {
            click(id) { links.choose(key) { update() } }
        }
        click(R.id.system) { openSystem(Settings.ACTION_SETTINGS) }
        click(R.id.home_role) { configureHome() }
        click(R.id.diagnostics) { diagnostics() }
    }

    private fun configureHome() {
        val name = ComponentName(this, "$packageName.HomeEntry")
        val enabled = packageManager.getComponentEnabledSetting(name) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        if (enabled) {
            openSystem(Settings.ACTION_HOME_SETTINGS)
            return
        }
        AlertDialog.Builder(this).setTitle("Rendi disponibile come Home")
            .setMessage("Il launcher comparirà tra le app Home. Potrai scegliere la Home preferita nelle impostazioni Android.")
            .setPositiveButton("Abilita") { _, _ ->
                packageManager.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP)
                update()
                openSystem(Settings.ACTION_HOME_SETTINGS)
            }.setNegativeButton("Annulla", null).show()
    }

    private fun openSystem(action: String) {
        try {
            startActivity(Intent(action))
        } catch (_: android.content.ActivityNotFoundException) {
            Toast.makeText(this, "Questa schermata di sistema non è disponibile.", Toast.LENGTH_LONG).show()
        } catch (_: SecurityException) {
            Toast.makeText(this, "Il firmware impedisce l’apertura delle impostazioni.", Toast.LENGTH_LONG).show()
        }
    }

    private fun diagnostics() {
        val memory = ActivityManager.MemoryInfo()
        (getSystemService(ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(memory)
        val metrics = resources.displayMetrics
        val info = """
            Android ${Build.VERSION.RELEASE} • API ${Build.VERSION.SDK_INT}
            Modello ${Build.MANUFACTURER} ${Build.MODEL}
            Build ${Build.DISPLAY}
            ABI ${Build.SUPPORTED_ABIS.joinToString()}
            Area app ${metrics.widthPixels} × ${metrics.heightPixels}
            RAM di sistema ${memory.totalMem / (1024 * 1024)} MB
            RAM disponibile ${memory.availMem / (1024 * 1024)} MB

            Dati dichiarati dal sistema. La versione MCU richiede le informazioni del firmware.
        """.trimIndent()
        AlertDialog.Builder(this).setTitle("Informazioni dispositivo").setMessage(info)
            .setPositiveButton("Chiudi", null).show()
    }

    private fun update() {
        val current = backend
        text(R.id.status, if (!demoEnabled) "Bluetooth • stato non verificato"
            else if (current.connected) "Telefono demo connesso" else "Telefono demo scollegato")
        text(R.id.mode, "1024 × 600 • " + if (demoEnabled) "Demo attiva • audio e chiamate simulati" else "Modalità dispositivo")
        when (page) {
            "home", "music" -> {
                val title = if (demoEnabled) current.track?.title ?: "Sorgente scollegata" else "Musica Bluetooth"
                val artist = if (demoEnabled) current.track?.artist ?: "Connetti il telefono demo nelle impostazioni."
                    else "Controlli e metadati da verificare sulla radio."
                text(if (page == "home") R.id.home_title else R.id.music_title, title)
                text(if (page == "home") R.id.home_artist else R.id.music_artist, artist)
                val controls = if (page == "home") listOf(R.id.home_prev, R.id.home_play, R.id.home_next)
                    else listOf(R.id.music_prev, R.id.music_play, R.id.music_next)
                controls.forEach { findViewById<View>(it).isEnabled = current.connected }
                findViewById<ImageButton>(controls[1]).apply {
                    setImageResource(if (current.playing) R.drawable.ic_pause else R.drawable.ic_play)
                    contentDescription = if (current.playing) "Pausa" else "Riproduci"
                }
                val fallback = if (page == "home") R.id.home_fallback else R.id.music_fallback
                findViewById<Button>(fallback).apply {
                    visibility = if (current.connected) View.GONE else View.VISIBLE
                    text = if (demoEnabled) "Apri impostazioni" else "Apri app Bluetooth"
                }
                findViewById<SeekBar>(R.id.seek)?.apply {
                    isEnabled = current.connected
                    max = current.track?.duration ?: 1
                }
                updateProgress()
            }
            "phone" -> {
                val call = current.callNumber
                text(R.id.call_status, if (!demoEnabled) "Usa l’app telefono originale"
                    else if (call.isNotEmpty()) "Chiamata simulata in corso"
                    else if (current.connected) "Tastierino demo" else "Telefono demo scollegato")
                text(R.id.number, call.ifEmpty { number.ifEmpty { "—" } })
                val available = current.connected && call.isEmpty()
                digits.forEach { (id, _) -> findViewById<View>(id).isEnabled = available && number.length < 20 }
                findViewById<View>(R.id.erase).isEnabled = available && number.isNotEmpty()
                findViewById<Button>(R.id.call).apply {
                    isEnabled = current.connected && (number.isNotEmpty() || call.isNotEmpty())
                    text = if (call.isEmpty()) "Simula chiamata" else "Termina chiamata"
                    backgroundTintList = android.content.res.ColorStateList.valueOf(
                        if (call.isEmpty()) Color.rgb(240, 199, 126) else Color.rgb(166, 74, 58))
                    setTextColor(if (call.isEmpty()) Color.rgb(42, 29, 11) else Color.WHITE)
                }
                findViewById<View>(R.id.phone_fallback).visibility = if (demoEnabled) View.GONE else View.VISIBLE
            }
            "auto" -> {
                text(R.id.auto_title, if (demoEnabled && demo.projection) "Sessione demo attiva" else "Proiezione Android Auto")
                text(R.id.auto_status, if (!demoEnabled) "Apri l’app di proiezione scelta nelle impostazioni."
                    else if (demo.connected) "Anteprima dello stato di proiezione."
                    else "Connetti il telefono demo nelle impostazioni.")
                text(R.id.auto_open, if (!demoEnabled) "Apri Android Auto"
                    else if (!demo.connected) "Apri impostazioni"
                    else if (demo.projection) "Chiudi demo" else "Avvia demo")
            }
            "settings" -> {
                text(R.id.demo, if (demoEnabled) "Disattiva demo" else "Attiva demo")
                findViewById<View>(R.id.connect).isEnabled = demoEnabled
                text(R.id.connect, if (demo.connected) "Disconnetti telefono demo" else "Connetti telefono demo")
                text(R.id.choose_bt, "App musica Bluetooth • ${links.label("bt")}")
                text(R.id.choose_phone, "App telefono • ${links.label("phone")}")
                text(R.id.choose_auto, "App Android Auto • ${links.label("auto")}")
                val enabled = packageManager.getComponentEnabledSetting(
                    ComponentName(this, "$packageName.HomeEntry")) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                text(R.id.home_role, if (enabled) "Scegli Home predefinita" else "Abilita come launcher")
            }
        }
    }

    private fun updateProgress() {
        if (page != "music") return
        findViewById<SeekBar>(R.id.seek)?.progress = backend.elapsed
        text(R.id.track_time, "${formatTime(backend.elapsed)} / ${formatTime(backend.track?.duration ?: 0)}")
    }

    private fun formatTime(seconds: Int) = String.format(Locale.ITALIAN, "%d:%02d", seconds / 60, seconds % 60)
    private fun text(id: Int, value: String) { findViewById<TextView>(id)?.text = value }
    private fun click(id: Int, action: () -> Unit) { findViewById<View>(id)?.setOnClickListener { action() } }
}
