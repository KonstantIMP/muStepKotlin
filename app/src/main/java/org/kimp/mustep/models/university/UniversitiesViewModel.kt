package org.kimp.mustep.models.university

import android.app.Application
import android.content.Context.MODE_PRIVATE
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kimp.mustep.domain.University
import org.kimp.mustep.rest.MuStepServiceBuilder
import org.kimp.mustep.utils.PreferencesData
import kotlin.streams.toList

class UniversitiesViewModel(application: Application) : AndroidViewModel(application) {
    private var universities: MutableLiveData<List<University>> = MutableLiveData()

    init {
        viewModelScope.launch (Dispatchers.IO) {
            val uniSet = HashSet<University>()

            for (uid in getApplication<Application>().getSharedPreferences(PreferencesData.BASE_PREFERENCES_NAME, MODE_PRIVATE)
                .getStringSet("cached", HashSet())!!) {
                try {
                    val uniFile = File(getApplication<Application>().cacheDir, String.format("%s%sdata.txt", uid, File.separator))
                    GsonBuilder().create().fromJson<University>(
                        String(Files.readAllBytes(uniFile.toPath())), University::class.java
                    ).apply {
                        uniSet.add(this)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            try {
                var response = MuStepServiceBuilder.build()
                    .getAvailableUniversities()
                    .execute()

                if (response.isSuccessful) uniSet.addAll(response.body()!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            universities.postValue(uniSet.stream().toList())
        }
    }

    fun getUniversities() : LiveData<List<University>> = universities
}