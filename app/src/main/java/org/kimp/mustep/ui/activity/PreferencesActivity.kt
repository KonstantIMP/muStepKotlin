package org.kimp.mustep.ui.activity

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import org.kimp.mustep.ui.fragment.PreferencesFragment

class PreferencesActivity : AppCompatActivity() {
    private lateinit var preferencesFragmentContainer: FragmentContainerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferencesFragmentContainer = FragmentContainerView(this)
        preferencesFragmentContainer.id = View.generateViewId()
        setContentView(preferencesFragmentContainer)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(preferencesFragmentContainer.id, PreferencesFragment())
                .commit()
        }
    }
}
