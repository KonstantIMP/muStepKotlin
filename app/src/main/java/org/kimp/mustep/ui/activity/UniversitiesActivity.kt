package org.kimp.mustep.ui.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import org.kimp.mustep.databinding.ActivityUniversitiesBinding
import org.kimp.mustep.models.university.UniversitiesCardViewAdapter
import org.kimp.mustep.models.university.UniversitiesViewModel
import org.kimp.mustep.utils.service.MediaPoolService

class UniversitiesActivity() : AppCompatActivity() {
    lateinit var binding: ActivityUniversitiesBinding
    lateinit var viewModel: UniversitiesViewModel

    lateinit var adapter: UniversitiesCardViewAdapter

    private lateinit var mService: MediaPoolService
    private var mBound: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUniversitiesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.uaUniversitiesRv.layoutManager = LinearLayoutManager(this)

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.uaUniversitiesRv.layoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
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

        Intent(this, MediaPoolService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStart() {
        super.onStart()
        if (this::adapter.isInitialized) adapter.bindToService()
    }

    override fun onResume() {
        super.onResume()
        if (mBound) mService.stopPlaying()
    }

    override fun onStop() {
        super.onStop()
        if (this::adapter.isInitialized) adapter.unbindFromService()
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MediaPoolService.MediaPoolBinder
            mService = binder.getService()
            mBound = true

            mService.stopPlaying()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }
}
