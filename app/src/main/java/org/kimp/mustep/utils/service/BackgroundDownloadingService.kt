package org.kimp.mustep.utils.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileWriter
import org.kimp.mustep.R
import org.kimp.mustep.domain.University
import org.kimp.mustep.rest.MuStepServiceBuilder
import org.kimp.mustep.utils.AppCache
import org.kimp.mustep.utils.DownloadClient
import org.kimp.mustep.utils.PreferencesData


private const val DOWNLOADING_SERVICE_NOTIFY: Int = 13
const val DOWNLOADING_SERVICE_MSG_QUEUE = 12
const val DOWNLOADING_SERVICE_MSG_DONE = 11

class BackgroundDownloadingService : Service() {
    private val channelId = "org.kimp.mustep.cache"

    private var queueToCache: ArrayList<University> = ArrayList()

    private lateinit var messenger: Messenger

    fun addUniversityToQueue(uni: University) {
        queueToCache.add(uni)
        uiHandler.sendEmptyMessage(DOWNLOADING_SERVICE_NOTIFY)
    }

    private fun deleteNotificationChannel() {
        (getSystemService(NotificationManager::class.java) as NotificationManager).deleteNotificationChannel(
            channelId
        )
    }

    override fun onBind(intent: Intent): IBinder {
        messenger = Messenger(IncomingHandler(this, this))
        return messenger.binder
    }

    override fun onDestroy() {
        deleteNotificationChannel()
        super.onDestroy()
    }

    private val uiHandler: Handler = Handler(Looper.getMainLooper()) {
        if (queueToCache.size > 0) {
            val message = Message()
            message.what = DOWNLOADING_SERVICE_NOTIFY + 1

            Bundle().apply {
                this.putParcelable("data", queueToCache.removeAt(0))
                message.data = this
            }

            downloadHandler.sendMessage(message)
        }

        return@Handler false
    }

    private val downloadThread: HandlerThread
    private val downloadHandler: Handler

    init {
        downloadThread = HandlerThread("downloadThread")
        downloadThread.start()

        downloadHandler = Handler(downloadThread.looper) {
            if (it.what != DOWNLOADING_SERVICE_NOTIFY) {
                val client = DownloadClient()
                val uni: University =
                    it.data!!.getParcelable<University>("data")!!

                val root = File(cacheDir, String.format("%s%s", uni.uid, File.separator))
                root.mkdirs()

                try {
                    val uniData = File(root, "data.txt").apply {
                        this.createNewFile()
                        this.setReadable(true)
                    }

                    val writer = FileWriter(uniData)
                    writer.write(GsonBuilder().create().toJson(uni))
                    writer.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                client.downloadFile(
                    AppCache.getCacheSupportUri(
                        String.format("%s/head.png", uni.uid), this
                    ), File(root, "head.png")
                )

                val floors = MuStepServiceBuilder.build()
                    .getUniversityData(uni.uid)
                    .execute().body()!!

                try {
                    val floorsData = File(root, "floors.txt").apply {
                        this.createNewFile()
                        this.setReadable(true)
                    }

                    val writer = FileWriter(floorsData)
                    writer.write(GsonBuilder().create().toJson(floors))
                    writer.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                for (floor in floors) {
                    val tmpRoot =
                        File(root, String.format("floor_%d%s", floor.number, File.separator))
                    tmpRoot.mkdirs()

                    for (point in floor.points) {
                        for (suffix in listOf<String>(".png", "_en.mp3", "_ru.mp3")) {
                            client.downloadFile(
                                AppCache.getCacheSupportUri(
                                    String.format(
                                        "%s/floor_%d/%s%s",
                                        uni.uid,
                                        floor.number,
                                        point.uid,
                                        suffix
                                    ),
                                    this
                                ),
                                File(tmpRoot, String.format("%s%s", point.uid, suffix))
                            )
                        }
                    }
                }

                val pref = getSharedPreferences(PreferencesData.BASE_PREFERENCES_NAME, MODE_PRIVATE)
                val cached = pref.getStringSet("cached", HashSet())
                cached!!.add(uni.uid)

                val editor = pref.edit()
                editor.putStringSet("cached", cached)
                editor.apply()

                uiHandler.sendEmptyMessage(DOWNLOADING_SERVICE_NOTIFY)
            }
            return@Handler false
        }
    }

    internal class IncomingHandler(
        context: Context,
        private val serviceInstance: BackgroundDownloadingService
    ) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                DOWNLOADING_SERVICE_MSG_QUEUE -> serviceInstance.addUniversityToQueue(
                    msg.data!!.getParcelable<University>("data")!!
                )
                else -> super.handleMessage(msg)
            }
        }
    }
}