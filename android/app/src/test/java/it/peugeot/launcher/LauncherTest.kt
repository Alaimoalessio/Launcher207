package it.peugeot.launcher

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class LauncherTest {
    @Config(sdk = [23, 28, 35])
    @Test fun screensInflateAndDemoControlsWork() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        try {
            activity.findViewById<View>(R.id.nav_music).performClick()
            activity.findViewById<View>(R.id.music_next).performClick()
            assertEquals("Luci di città", activity.findViewById<TextView>(R.id.music_title).text.toString())
            activity.findViewById<View>(R.id.nav_phone).performClick()
            val canvas = (activity.findViewById<android.view.ViewGroup>(android.R.id.content)).getChildAt(0)
            canvas.measure(View.MeasureSpec.makeMeasureSpec(1024, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY))
            canvas.layout(0, 0, 1024, 600)
            val keypad = activity.findViewById<View>(R.id.keypad)
            val parent = keypad.parent as View
            assertTrue(keypad.right <= parent.width)
            assertTrue(keypad.bottom <= parent.height)
            assertTrue(keypad.top >= 0)
            activity.findViewById<View>(R.id.digit_1).performClick()
            activity.findViewById<View>(R.id.digit_2).performClick()
            activity.findViewById<View>(R.id.call).performClick()
            assertEquals("Chiamata simulata in corso", activity.findViewById<TextView>(R.id.call_status).text.toString())
            assertFalse(activity.findViewById<View>(R.id.digit_1).isEnabled)
            activity.findViewById<View>(R.id.call).performClick()
            activity.findViewById<View>(R.id.nav_auto).performClick()
            activity.findViewById<View>(R.id.auto_open).performClick()
            assertEquals("Sessione demo attiva", activity.findViewById<TextView>(R.id.auto_title).text.toString())
            activity.findViewById<View>(R.id.settings).performClick()
            activity.findViewById<View>(R.id.connect).performClick()
            activity.findViewById<View>(R.id.nav_music).performClick()
            assertFalse(activity.findViewById<View>(R.id.music_play).isEnabled)
            assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.music_fallback).visibility)
        } finally { controller.pause().stop().destroy() }
    }

    @Test fun homeIntentOverridesRestoredSettingsAndBackStaysHome() {
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val saved = Bundle().apply { putString("page", "settings") }
        val controller = Robolectric.buildActivity(MainActivity::class.java, home).create(saved).start().resume()
        val activity = controller.get()
        try {
            assertNotNull(activity.findViewById<View>(R.id.clock))
            activity.findViewById<View>(R.id.settings).performClick()
            controller.newIntent(home)
            assertNotNull(activity.findViewById<View>(R.id.clock))
            activity.onBackPressed()
            assertFalse(activity.isFinishing)
        } finally { controller.pause().stop().destroy() }
    }

    @Test fun stateSurvivesRecreationAndDeviceModeDisablesMockCommands() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        try {
            controller.get().findViewById<View>(R.id.nav_phone).performClick()
            controller.get().findViewById<View>(R.id.digit_1).performClick()
            controller.recreate()
            val activity = controller.get()
            assertEquals("1", activity.findViewById<TextView>(R.id.number).text.toString())
            activity.findViewById<View>(R.id.settings).performClick()
            activity.findViewById<View>(R.id.demo).performClick()
            activity.findViewById<View>(R.id.nav_phone).performClick()
            assertFalse(activity.findViewById<Button>(R.id.call).isEnabled)
            assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.phone_fallback).visibility)
            assertEquals(0, activity.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE)
        } finally { controller.pause().stop().destroy() }
    }

    @Test fun appLinksRejectSelfAndHandleRemovedApps() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        try {
            val preferences = activity.getSharedPreferences("test_links", android.content.Context.MODE_PRIVATE)
            val links = AppLinks(activity, preferences)
            preferences.edit().putString("link_bt",
                android.content.ComponentName(activity, MainActivity::class.java).flattenToString()).commit()
            links.open("bt")
            assertNull(org.robolectric.Shadows.shadowOf(activity).nextStartedActivity)
            preferences.edit().putString("link_bt", "missing.package/missing.package.Screen").commit()
            assertEquals("App non più disponibile", links.label("bt"))
            links.open("bt")
            assertNull(org.robolectric.Shadows.shadowOf(activity).nextStartedActivity)
        } finally { controller.pause().stop().destroy() }
    }

    @Test fun restoreClearsStaleCallAndClampsInputs() {
        val backend = DemoBackend()
        backend.dial("123")
        backend.restore(999, true, true, -10, "invalid", false)
        assertEquals("", backend.callNumber)
        assertEquals(0, backend.elapsed)
        assertEquals(2, backend.index)
        backend.connectDemo(false)
        assertFalse(backend.playing)
        backend.togglePlayback()
        backend.dial("123")
        assertEquals("", backend.callNumber)
        assertFalse(backend.playing)
    }
}
