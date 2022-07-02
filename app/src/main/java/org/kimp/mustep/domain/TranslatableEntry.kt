package org.kimp.mustep.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.kimp.mustep.utils.PreferencesData

@Parcelize
data class TranslatableEntry(
    var en: String = "",
    var ru: String = "",
    var zh: String = ""
) : Parcelable {
    fun getTranslatedValue(): String {
        return if (PreferencesData.currentLanguage == "ru" && ru.isNotEmpty()) ru
        else if (PreferencesData.currentLanguage == "zh" && zh.isNotEmpty()) zh
        else en
    }
}
