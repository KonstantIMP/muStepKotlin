package org.kimp.mustep.ui.fragment

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.color.DynamicColors
import com.google.android.material.snackbar.Snackbar
import java.io.File
import org.kimp.mustep.R
import org.kimp.mustep.utils.PreferencesData


class PreferencesFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        (findPreference("remove_cached") as Preference?)?.setOnPreferenceClickListener { _ ->
            val sharedPreferences: SharedPreferences =
                requireContext().getSharedPreferences(
                    PreferencesData.BASE_PREFERENCES_NAME,
                    MODE_PRIVATE
                )

            for (uid in sharedPreferences.getStringSet("cached", HashSet())!!) {
                val data =
                    File(requireContext().cacheDir, String.format("%s%s", uid, File.separator))
                data.delete()
            }
            val editor = sharedPreferences.edit()
            editor.putStringSet("cached", HashSet())
            editor.apply()
            Snackbar.make(
                requireContext(), requireView(),
                getString(R.string.pref_done), Snackbar.LENGTH_LONG
            ).show()
            false
        }

        (findPreference("dynamic_colors") as Preference?)?.isEnabled =
            DynamicColors.isDynamicColorAvailable()

        requireContext().getSharedPreferences(PreferencesData.BASE_PREFERENCES_NAME, MODE_PRIVATE)
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(pref: SharedPreferences?, name: String?) {
        when (name) {
            "dynamic_colors" -> Snackbar.make(
                requireContext(), requireView(),
                getString(R.string.pref_done), Snackbar.LENGTH_LONG
            ).show()
            "preferred_language" -> PreferencesData.currentLanguage =
                pref?.getString("preferred_language", PreferencesData.currentLanguage)!!
        }
    }
}
