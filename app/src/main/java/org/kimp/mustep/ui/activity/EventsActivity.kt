package org.kimp.mustep.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import org.kimp.mustep.databinding.ActivityEventsBinding
import org.kimp.mustep.domain.University
import org.kimp.mustep.models.event.EventsCardViewAdapter
import org.kimp.mustep.models.event.EventsViewModel

class EventsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEventsBinding
    private lateinit var university: University

    private lateinit var eventsViewModel: EventsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        university =
            if (savedInstanceState == null) {
                intent.extras?.getParcelable<University>("university") as University
            } else {
                savedInstanceState.getParcelable<University>("university") as University
            }

        binding.eaNameMsg.text = university.name.getTranslatedValue()
        binding.eaAddrMsg.text = university.address.getTranslatedValue()

        binding.eaEventsRv.layoutManager = LinearLayoutManager(this)
        binding.eaEventsRv.adapter = EventsCardViewAdapter(ArrayList(), university, this)

        eventsViewModel = ViewModelProvider(this)[EventsViewModel::class.java]
        eventsViewModel.loadEvents(university.uid)
        eventsViewModel.getEvents().observe(this) {
            binding.eaEventsRv.adapter =
                EventsCardViewAdapter(it!!, university, this)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("university", university)
    }
}
