package org.kimp.mustep.models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kimp.mustep.domain.University
import org.kimp.mustep.rest.MuStepServiceBuilder

class UniversitiesViewModel() : ViewModel() {
    private var universities: MutableLiveData<List<University>> = MutableLiveData()

    init {
        viewModelScope.launch (Dispatchers.IO) {
            var response = MuStepServiceBuilder.build()
                .getAvailableUniversities()
                .execute()

            if (response.isSuccessful) universities.postValue(response.body())
        }
    }

    fun getUniversities() : LiveData<List<University>> = universities
}