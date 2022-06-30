package org.kimp.mustep.utils

import org.kimp.mustep.MuStepApplication

object PreferencesData {
    const val BASE_PREFERENCES_NAME: String = "org.kimp.mustep_preferences"

    const val PREFERRED_LANGUAGE_PREF: String = "preferred_language"
    const val DYNAMIC_COLORS_PREF: String = "dynamic_colors"
    const val AUTO_DOWNLOAD_PREF: String = "auto_download"
    const val REMOVE_CACHED_PREF: String = "remove_cached"

    var currentLanguage: String = ""

    fun getAudioSuffix() : String = if (currentLanguage == "ru") "ru" else "en"
}