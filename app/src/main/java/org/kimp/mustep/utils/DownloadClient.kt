package org.kimp.mustep.utils

import android.net.Uri
import java.io.File
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink

class DownloadClient {
    private var client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(700, TimeUnit.MILLISECONDS)
        .build()

    fun downloadFile(path: Uri, out: File) : Boolean {
        if (out.exists()) return true
        try {
            val request = Request.Builder().url(path.toString()).build()
            val response = client.newCall(request).execute()

            val sink = out.sink().buffer()
            sink.writeAll(response.body.source())
            sink.close()

            out.setReadable(true)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }
}