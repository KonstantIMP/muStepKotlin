package org.kimp.mustep

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import org.kimp.mustep.utils.AppCache
import org.kimp.mustep.utils.PreferencesData
import java.util.Locale

class MuStepApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val pref = getSharedPreferences(PreferencesData.BASE_PREFERENCES_NAME, MODE_PRIVATE)
        val editor = pref.edit()

        if (!pref.contains(PreferencesData.PREFERRED_LANGUAGE_PREF)) {
            when (Locale.getDefault().language) {
                "ru" -> editor.putString(PreferencesData.PREFERRED_LANGUAGE_PREF, "ru")
                "zh" -> editor.putString(PreferencesData.PREFERRED_LANGUAGE_PREF, "zh")
                else -> editor.putString(PreferencesData.PREFERRED_LANGUAGE_PREF, "en")
            }
        }
        if (!pref.contains(PreferencesData.DYNAMIC_COLORS_PREF)) {
            editor.putBoolean(PreferencesData.DYNAMIC_COLORS_PREF, false)
        }
        if (!pref.contains(PreferencesData.AUTO_DOWNLOAD_PREF)) {
            editor.putBoolean(PreferencesData.AUTO_DOWNLOAD_PREF, true)
        }

        editor.apply()

        if (pref.getBoolean(PreferencesData.DYNAMIC_COLORS_PREF, true)) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }

        AppCache.loadCachedFloors(this)
        PreferencesData.currentLanguage = pref.getString(
            PreferencesData.PREFERRED_LANGUAGE_PREF,
            "en"
        )!!

        Picasso.setSingletonInstance(
            Picasso.Builder(this)
                .downloader(OkHttp3Downloader(this, AppCache.IMAGE_CACHE_SIZE))
                .build()
        )
    }
}
