package org.kimp.mustep.models

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kimp.mustep.domain.Event
import org.kimp.mustep.rest.MuStepServiceBuilder
import org.kimp.mustep.utils.AppCache

class EventsViewModel(application: Application) : AndroidViewModel(application) {
    private var events: MutableLiveData<List<Event>> = MutableLiveData()

    fun loadEvents(uid: String) {
        viewModelScope.launch (Dispatchers.IO) {
            if (AppCache.getEvents(uid) != null) {
                events.postValue(AppCache.getEvents(uid)!!)
                return@launch
            }

            try {
                val loadedEvents = MuStepServiceBuilder.build()
                    .getEvents(uid)
                    .execute()
                    .body()!!

                AppCache.putEvents(uid, loadedEvents)
                 events.postValue(loadedEvents)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getEvents() : LiveData<List<Event>> = events
}