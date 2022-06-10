package org.kimp.mustep.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.AmazonS3URI
import java.io.File

object AppCache {
    const val IMAGE_CACHE_SIZE: Long = 32 * 1024 * 1024

    fun getCacheSupportUri(relativePath: String, context: Context): Uri {
        Log.i("Cache", String.format("Requested: %s", relativePath))

        val local = File(String.format("%s%s%s", context.cacheDir, File.pathSeparator, relativePath))
        if (local.exists()) return Uri.fromFile(local)

        var s3Client = AmazonS3Client(StorageCredentials(), Region.getRegion(
            Regions.US_WEST_2
        ))
        s3Client.endpoint = "storage.yandexcloud.net"

        return Uri.parse(
            s3Client.getResourceUrl("mustep", relativePath)
        )
    }
}