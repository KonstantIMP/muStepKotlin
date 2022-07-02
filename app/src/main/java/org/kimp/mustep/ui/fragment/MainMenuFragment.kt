package org.kimp.mustep.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.kimp.mustep.databinding.FragmentMainMenuBinding
import org.kimp.mustep.ui.activity.PreferencesActivity
import org.kimp.mustep.ui.activity.UniversitiesActivity

class MainMenuFragment() : Fragment() {
    lateinit var binding: FragmentMainMenuBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainMenuBinding.inflate(inflater, container, false)

        binding.fmmSettingsBtn.setOnClickListener {
            startActivity(Intent(requireContext(), PreferencesActivity::class.java))
        }

        binding.fmmStartBtn.setOnClickListener {
            startActivity(Intent(requireContext(), UniversitiesActivity::class.java))
        }

        return binding.root
    }
}
