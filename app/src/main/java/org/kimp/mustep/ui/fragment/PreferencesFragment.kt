package org.kimp.mustep.ui.fragment

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import org.kimp.mustep.R

class PreferencesFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}
