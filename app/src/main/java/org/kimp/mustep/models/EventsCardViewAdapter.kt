package org.kimp.mustep.models

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import io.getstream.avatarview.coil.loadImage
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import org.kimp.mustep.R
import org.kimp.mustep.databinding.ViewEventCardBinding
import org.kimp.mustep.databinding.ViewNothingBinding

import org.kimp.mustep.domain.Event
import org.kimp.mustep.rest.MuStepServiceBuilder
import org.kimp.mustep.utils.AppCache
import org.kimp.mustep.utils.PreferencesData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.streams.toList

class EventsCardViewAdapter(
    private var events: List<Event>,
    private val owner: AppCompatActivity
) : RecyclerView.Adapter<ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (events.isEmpty()) object : ViewHolder(
            ViewNothingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ).root
        ) {}
        else EventsCardViewHolder(
            ViewEventCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ).root
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        if (events.isEmpty()) return

        val holder = viewHolder as EventsCardViewHolder

        holder.eventNameMsg!!.text = events[position].name.getTranslatedValue()
        holder.eventUsersChip!!.text = String.format("% 2d / % 2d", events[position].users.size, events[position].users_count)

        holder.eventRuSupported!!.visibility = if (events[position].languages.ru.isEmpty()) View.GONE else View.VISIBLE
        holder.eventEnSupported!!.visibility = if (events[position].languages.en.isEmpty()) View.GONE else View.VISIBLE
        holder.eventZhSupported!!.visibility = if (events[position].languages.zh.isEmpty()) View.GONE else View.VISIBLE

        holder.eventGuideName!!.text = events[position].guide.getTranslatedValue()
        
        holder.eventDateChip!!.text = String.format("%02d.%02d.%d - %02d:%02d",
            events[position].date.day,
            events[position].date.mounth,
            events[position].date.year,
            events[position].time.hours,
            events[position].time.minutes
        )

        val nameParts = events[position].guide.getTranslatedValue().split("\\s*")
        var nameInitials = StringBuilder()
        for (part in nameParts) if (part.isNotEmpty()) nameInitials.append(part[0])

        holder.eventGuideAvatar!!.avatarInitials = nameInitials.toString().uppercase()
        if (events[position].guide_avatar.isNotEmpty()) {
            holder.eventGuideAvatar!!.loadImage(
                AppCache.getCacheSupportUri(events[position].guide_avatar, owner),
                onSuccess = { _, _ ->
                    holder.eventGuideAvatar!!.avatarInitials = ""
                    holder.eventGuideAvatar!!.avatarBorderWidth = 0
                }
            )
        }

        setEventState(events[position], holder)
    }

    private fun setEventState(event: Event, holder: EventsCardViewHolder) {
        if (event.users_count == event.users.size) {
            holder.ecvRegisterButton!!.text = owner.getString(R.string.ecv_closed)
            holder.ecvRegisterButton!!.isEnabled = false
        }
        else if (event.users.contains(FirebaseAuth.getInstance().currentUser!!.uid)) {
            holder.ecvRegisterButton!!.text = owner.getString(R.string.ecv_unregister)
            holder.ecvRegisterButton!!.isEnabled = true

            holder.ecvRegisterButton!!.setOnClickListener {
                it.isEnabled = false

                MuStepServiceBuilder.build()
                    .unregisterFromEvent(event.uid, FirebaseAuth.getInstance().currentUser!!.uid)
                    .enqueue(
                        object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                event.users = event.users.stream()
                                    .filter{ x -> x != FirebaseAuth.getInstance().currentUser!!.uid}
                                    .toList()

                                val newEvents = ArrayList(events.stream()
                                    .filter { x -> x.uid != event.uid }
                                    .toList())
                                newEvents.add(event)

                                AppCache.putEvents(event.university, newEvents)
                                events = newEvents

                                holder.eventUsersChip!!.text = String.format("% 2d / % 2d", event.users.size, event.users_count)

                                holder.ecvRegisterButton!!.text = owner.getString(R.string.ecv_register)
                                it.isEnabled = true

                                Snackbar.make(
                                    owner, it,
                                    owner.resources.getString(R.string.ecv_unregister_ok),
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Snackbar.make(
                                    owner, it,
                                    String.format(
                                        "%s: %s",
                                        owner.resources.getString(R.string.error_preview),
                                        t.localizedMessage
                                    ),
                                    Snackbar.LENGTH_SHORT
                                ).show()
                                it.isEnabled = true
                            }
                        }
                    )
            }
        }
        else {
            holder.ecvRegisterButton!!.setOnClickListener {
                it.isEnabled = false

                MuStepServiceBuilder.build()
                    .registerToEvent(event.uid, FirebaseAuth.getInstance().currentUser!!.uid)
                    .enqueue(
                        object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                ArrayList(event.users).apply {
                                    add(FirebaseAuth.getInstance().currentUser!!.uid)
                                    event.users = this
                                }

                                val newEvents = ArrayList(events.stream()
                                    .filter { x -> x.uid != event.uid }
                                    .toList())
                                newEvents.add(event)

                                AppCache.putEvents(event.university, newEvents)
                                events = newEvents

                                holder.eventUsersChip!!.text = String.format("% 2d / % 2d", event.users.size, event.users_count)

                                holder.ecvRegisterButton!!.text = owner.getString(R.string.ecv_unregister)
                                it.isEnabled = true

                                Snackbar.make(
                                    owner, it,
                                        owner.resources.getString(R.string.ecv_register_ok),
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Snackbar.make(
                                    owner, it,
                                    String.format(
                                        "%s: %s",
                                        owner.resources.getString(R.string.error_preview),
                                        t.localizedMessage
                                    ),
                                    Snackbar.LENGTH_SHORT
                                ).show()
                                it.isEnabled = true
                            }
                        }
                    )
            }
        }
    }

    override fun getItemCount(): Int = kotlin.math.max(1, events.size)
}