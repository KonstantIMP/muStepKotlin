package org.kimp.mustep.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.kimp.mustep.databinding.ActivityMainBinding
import org.kimp.mustep.utils.service.BackgroundDownloadingService

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startService(Intent(this, BackgroundDownloadingService::class.java))
    }
}
