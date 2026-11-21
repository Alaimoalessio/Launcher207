package it.peugeot.launcher

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.widget.Toast

/** Collegamenti scelti dall'utente, senza nomi di pacchetti vendor presunti. */
class AppLinks(private val activity: Activity, private val preferences: SharedPreferences) {
    fun label(key: String): String {
        val component = component(key) ?: return if (key == "auto" &&
            activity.packageManager.getLaunchIntentForPackage("gb.xxy.hr") != null)
            "Headunit Reloaded" else "Da scegliere"
        return try {
            activity.packageManager.getActivityInfo(component, 0)
                .loadLabel(activity.packageManager).toString()
        } catch (_: android.content.pm.PackageManager.NameNotFoundException) {
            "App non più disponibile"
        } catch (_: SecurityException) {
            "App non accessibile"
        }
    }

    private fun component(key: String): ComponentName? =
        preferences.getString("link_$key", null)?.let(ComponentName::unflattenFromString)

    fun open(key: String) {
        val component = component(key)
        if (component == null) {
            if (key == "auto") {
                val hur = activity.packageManager.getLaunchIntentForPackage("gb.xxy.hr")
                if (hur != null) {
                    try {
                        activity.startActivity(hur)
                        return
                    } catch (_: SecurityException) {
                        Toast.makeText(activity, "Il firmware impedisce l'apertura di HUR.", Toast.LENGTH_LONG).show()
                        return
                    } catch (_: ActivityNotFoundException) { /* App rimossa */ }
                }
            }
            Toast.makeText(activity, "Scegli l’app nelle impostazioni del launcher.", Toast.LENGTH_LONG).show()
            return
        }
        try {
            val info = activity.packageManager.getActivityInfo(component, 0)
            if (!info.exported || !info.enabled || !info.applicationInfo.enabled ||
                info.packageName == activity.packageName) {
                Toast.makeText(activity, "Collegamento non disponibile. Scegli un’altra app.", Toast.LENGTH_LONG).show()
                return
            }
            activity.startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                .setComponent(component))
        } catch (_: android.content.pm.PackageManager.NameNotFoundException) {
            Toast.makeText(activity, "App assente. Scegli un nuovo collegamento.", Toast.LENGTH_LONG).show()
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(activity, "App assente. Scegli un nuovo collegamento.", Toast.LENGTH_LONG).show()
        } catch (_: SecurityException) {
            Toast.makeText(activity, "Il sistema impedisce l’apertura di questa app.", Toast.LENGTH_LONG).show()
        }
    }

    fun choose(key: String, onChanged: () -> Unit) {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = activity.packageManager.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != activity.packageName && it.activityInfo.exported &&
                it.activityInfo.enabled && it.activityInfo.applicationInfo.enabled }
            .distinctBy { ComponentName(it.activityInfo.packageName, it.activityInfo.name) }
            .sortedBy { it.loadLabel(activity.packageManager).toString().lowercase() }
        val labels = arrayOf("Rimuovi collegamento") + apps.map {
            "${it.loadLabel(activity.packageManager)}\n${it.activityInfo.packageName}"
        }.toTypedArray()
        AlertDialog.Builder(activity).setTitle("Scegli app da aprire")
            .setItems(labels) { _, which ->
                val selected = apps.getOrNull(which - 1)
                val editor = preferences.edit()
                if (selected == null) editor.remove("link_$key")
                else editor.putString("link_$key", ComponentName(
                    selected.activityInfo.packageName, selected.activityInfo.name).flattenToString())
                editor.apply()
                onChanged()
            }.setNegativeButton("Annulla", null).show()
    }
}
