package org.kimp.mustep.ui.activity

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import org.kimp.mustep.databinding.ActivityUniversitiesBinding
import org.kimp.mustep.models.UniversitiesCardViewAdapter
import org.kimp.mustep.models.UniversitiesViewModel

class UniversitiesActivity() : AppCompatActivity() {
    lateinit var binding: ActivityUniversitiesBinding
    lateinit var viewModel: UniversitiesViewModel

    lateinit var adapter: UniversitiesCardViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUniversitiesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.uaUniversitiesRv.layoutManager = LinearLayoutManager(this)

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.uaUniversitiesRv.layoutManager = LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false
            )
        }

        viewModel = ViewModelProvider(this)[UniversitiesViewModel::class.java]
        viewModel.getUniversities().observe(this) {
            if (it.isEmpty() || it == null) return@observe

            adapter = UniversitiesCardViewAdapter(it, this)
            binding.uaUniversitiesRv.adapter = adapter

            binding.uaLoadingIndicator.visibility = View.GONE
            adapter.bindToService()
        }

        binding.uaReturnBtn.setOnClickListener { finish() }
    }

    override fun onStart() {
        super.onStart()
        if (this::adapter.isInitialized) adapter.bindToService()
    }

    override fun onStop() {
        super.onStop()
        if (this::adapter.isInitialized) adapter.unbindFromService()
    }
}
