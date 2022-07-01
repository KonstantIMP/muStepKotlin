package org.kimp.mustep.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.kimp.mustep.BuildConfig
import org.kimp.mustep.databinding.ActivityMainBinding
import org.kimp.mustep.ui.dialog.ChangelogDialog
import org.kimp.mustep.utils.PreferencesData
import org.kimp.mustep.utils.service.BackgroundDownloadingService
import org.kimp.mustep.utils.service.MediaPoolService

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startService(Intent(this, BackgroundDownloadingService::class.java))
        startService(Intent(this, MediaPoolService::class.java))

        val prefs = getSharedPreferences(PreferencesData.BASE_PREFERENCES_NAME, MODE_PRIVATE)
        if (BuildConfig.VERSION_NAME != prefs.getString("last_start_version", "")!!) {
            ChangelogDialog().show(
                supportFragmentManager, "changelog_dialog"
            )
        }
    }
}
