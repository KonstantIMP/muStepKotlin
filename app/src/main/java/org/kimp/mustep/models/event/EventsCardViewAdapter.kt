package org.kimp.mustep.models.event

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import io.getstream.avatarview.coil.loadImage
import org.kimp.mustep.R
import org.kimp.mustep.databinding.ViewEventCardBinding
import org.kimp.mustep.databinding.ViewNothingBinding
import org.kimp.mustep.domain.Event
import org.kimp.mustep.domain.University
import org.kimp.mustep.rest.MuStepServiceBuilder
import org.kimp.mustep.utils.AppCache
import org.kimp.mustep.utils.DateFormatter
import org.kimp.mustep.utils.EventsNotifyReceiver
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import kotlin.streams.toList

class EventsCardViewAdapter(
    private var events: List<Event>,
    val university: University,
    private val owner: AppCompatActivity
) : RecyclerView.Adapter<ViewHolder>() {
    val alarmManager = owner.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (events.isEmpty()) object : ViewHolder(
            ViewNothingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ).root
        ) {}
        else EventsCardViewHolder(
            ViewEventCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
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

        holder.eventDateChip!!.text = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT)
            .format(DateFormatter.toAndroidDate(events[position].date, events[position].time))

        val nameParts = events[position].guide.getTranslatedValue().split("\\s*")
        val nameInitials = StringBuilder()
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
        } else if (event.users.contains(FirebaseAuth.getInstance().currentUser!!.uid)) {
            holder.ecvRegisterButton!!.text = owner.getString(R.string.ecv_unregister)
            holder.ecvRegisterButton!!.isEnabled = true

            holder.ecvRegisterButton!!.setOnClickListener {
                it.isEnabled = false

                MuStepServiceBuilder.build()
                    .unregisterFromEvent(event.uid, FirebaseAuth.getInstance().currentUser!!.uid)
                    .enqueue(
                        object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                alarmManager.cancel(
                                    generatePendingIntent(event)
                                )

                                event.users = event.users.stream()
                                    .filter { x -> x != FirebaseAuth.getInstance().currentUser!!.uid }
                                    .toList()

                                val newEvents = ArrayList(
                                    events.stream()
                                        .filter { x -> x.uid != event.uid }
                                        .toList()
                                )
                                newEvents.add(event)

                                AppCache.putEvents(event.university, newEvents)
                                events = newEvents

                                holder.eventUsersChip!!.text = String.format("% 2d / % 2d", event.users.size, event.users_count)

                                holder.ecvRegisterButton!!.text = owner.getString(R.string.ecv_register)
                                it.isEnabled = true

                                Snackbar.make(
                                    owner,
                                    it,
                                    owner.resources.getString(R.string.ecv_unregister_ok),
                                    Snackbar.LENGTH_SHORT
                                ).show()

                                setEventState(event, holder)
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Snackbar.make(
                                    owner,
                                    it,
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
        } else {
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

                                val newEvents = ArrayList(
                                    events.stream()
                                        .filter { x -> x.uid != event.uid }
                                        .toList()
                                )
                                newEvents.add(event)

                                AppCache.putEvents(event.university, newEvents)
                                events = newEvents

                                holder.eventUsersChip!!.text = String.format("% 2d / % 2d", event.users.size, event.users_count)

                                holder.ecvRegisterButton!!.text = owner.getString(R.string.ecv_unregister)
                                it.isEnabled = true

                                Snackbar.make(
                                    owner,
                                    it,
                                    owner.resources.getString(R.string.ecv_register_ok),
                                    Snackbar.LENGTH_SHORT
                                ).show()

                                setEventState(event, holder)

                                alarmManager.set(
                                    AlarmManager.RTC_WAKEUP,
                                    DateFormatter.toAndroidDate(
                                        event.date,
                                        event.time
                                    ).time - 2 * 60 * 60 * 1000,
                                    generatePendingIntent(event)
                                )
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Snackbar.make(
                                    owner,
                                    it,
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

    private fun generatePendingIntent(event: Event): PendingIntent {
        val intent = Intent(owner, EventsNotifyReceiver::class.java)

        intent.putExtra("uni", university)
        intent.putExtra("event", event)

        return PendingIntent.getBroadcast(owner, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    override fun getItemCount(): Int = kotlin.math.max(1, events.size)
}
