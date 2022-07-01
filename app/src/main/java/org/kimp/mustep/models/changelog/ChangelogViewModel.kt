package org.kimp.mustep.models.changelog

import android.app.Application
import android.graphics.BitmapFactory
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kimp.mustep.BuildConfig
import org.kimp.mustep.domain.Change

class ChangelogViewModel(application: Application) : AndroidViewModel(application) {
    private var changes: MutableLiveData<List<Change>> = MutableLiveData()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val changesFile = GsonBuilder().create()
                .fromJson<ChangesFile>(
                    BufferedReader(InputStreamReader(
                        application.assets.open(
                            String.format("changes/%s/data.json", BuildConfig.VERSION_NAME)
                        )
                    )),
                    ChangesFile::class.java
                )

            if (changesFile.version != BuildConfig.VERSION_NAME) return@launch

            val res = changesFile.changes
            res.stream().forEach {
                try {
                    it.imageBitmap  = BitmapFactory.decodeStream(
                        application.assets.open(
                            String.format("changes/%s/%s", changesFile.version, it.image)
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            changes.postValue(res)
        }
    }

    data class ChangesFile (
        val version: String = "",
        val changes: List<Change>
    )

    fun getChanges() : LiveData<List<Change>> = changes
}