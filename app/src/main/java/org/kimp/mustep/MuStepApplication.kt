package org.kimp.mustep

import android.app.Application
import android.content.Context
import com.google.android.material.color.DynamicColors
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import java.util.Locale
import org.kimp.mustep.utils.AppCache
import org.kimp.mustep.utils.PreferencesData


class MuStepApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val pref = getSharedPreferences(PreferencesData.BASE_PREFERENCES_NAME, MODE_PRIVATE)
        if (!pref.contains(PreferencesData.PREFERRED_LANGUAGE_PREF)) {
            val editor = pref.edit()
            when (Locale.getDefault().language) {
                "ru" -> editor.putString(PreferencesData.PREFERRED_LANGUAGE_PREF, "ru")
                "zh" -> editor.putString(PreferencesData.PREFERRED_LANGUAGE_PREF, "zh")
                else -> editor.putString(PreferencesData.PREFERRED_LANGUAGE_PREF, "en")
            }
            editor.apply()
        }
        if (!pref.contains(PreferencesData.DYNAMIC_COLORS_PREF)) {
            val editor = pref.edit()
            editor.putBoolean(PreferencesData.DYNAMIC_COLORS_PREF, true)
            editor.apply()
        }

        if (pref.getBoolean(PreferencesData.DYNAMIC_COLORS_PREF, true)) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }

        Picasso.setSingletonInstance(
            Picasso.Builder(this)
                .downloader(OkHttp3Downloader(this, AppCache.IMAGE_CACHE_SIZE))
                .build()
        )
    }

    fun getContext() : Context = applicationContext
}
