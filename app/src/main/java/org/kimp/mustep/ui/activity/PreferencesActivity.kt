package org.kimp.mustep.ui.activity

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import com.google.firebase.auth.FirebaseAuth
import org.kimp.mustep.databinding.ActivityPreferencesBinding
import org.kimp.mustep.ui.fragment.PreferencesFragment

class PreferencesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPreferencesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreferencesBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.paPreferencesFragment.id, PreferencesFragment())
                .commit()
        }

        setSignState()
    }

    private fun setSignState() {
        binding.paSignedInLayout.visibility = if (FirebaseAuth.getInstance().currentUser == null) View.GONE else View.VISIBLE
        binding.paSignedOutLayout.visibility = if (FirebaseAuth.getInstance().currentUser == null) View.VISIBLE else View.GONE
    }
}
