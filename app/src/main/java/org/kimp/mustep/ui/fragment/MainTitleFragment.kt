package org.kimp.mustep.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.kimp.mustep.databinding.FragmentMainTitleBinding

class MainTitleFragment() : Fragment() {
    private lateinit var binding: FragmentMainTitleBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainTitleBinding.inflate(inflater, container, false)
        return binding.root
    }
}
