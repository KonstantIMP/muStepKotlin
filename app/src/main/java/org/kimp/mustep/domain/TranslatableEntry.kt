package org.kimp.mustep.domain

import org.kimp.mustep.utils.PreferencesData

data class TranslatableEntry(
    var en: String = "",
    var ru: String = "",
    var zh: String = ""
) {
    fun getTranslatedValue() : String {
        return if (PreferencesData.currentLanguage == "ru" && ru.isNotEmpty()) ru
            else if (PreferencesData.currentLanguage == "zh" && zh.isNotEmpty()) zh
            else en
    }
}
