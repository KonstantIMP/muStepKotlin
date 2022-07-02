package org.kimp.mustep.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import androidx.core.app.NotificationCompat
import org.kimp.mustep.R
import org.kimp.mustep.domain.Event
import org.kimp.mustep.domain.University
import java.text.DateFormat

class EventsNotifyReceiver : BroadcastReceiver() {
    private val NOTIFICATION_CHANNEL_ID = "org.kimp.mustep.events"

    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.events_service_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )

        notificationManager.createNotificationChannel(notificationChannel)

        PreferencesData.currentLanguage = context.getSharedPreferences(
            PreferencesData.BASE_PREFERENCES_NAME,
            MODE_PRIVATE
        ).getString(PreferencesData.PREFERRED_LANGUAGE_PREF, "en")!!

        val university = intent?.extras?.getParcelable<University>("uni")!!
        val event = intent.extras?.getParcelable<Event>("event")!!

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(event.name.getTranslatedValue())
            .setContentText(
                context.getString(
                    R.string.events_service_body,
                    DateFormat.getTimeInstance(DateFormat.SHORT).format(DateFormatter.toAndroidDate(event.date, event.time)),
                    event.name.getTranslatedValue(),
                    university.address.getTranslatedValue()
                )
            ).setShowWhen(true)
            .setSmallIcon(R.drawable.ic_event)
            .build()

        notificationManager.notify(
            event.date.year * 10000 + (event.date.mounth - event.time.hours) * 100 + event.date.day - event.time.minutes,
            notification
        )
    }
}
