package org.kimp.mustep.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import android.util.LruCache
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import java.io.File
import org.kimp.mustep.domain.Floor

object AppCache {
    const val IMAGE_CACHE_SIZE: Long = 32 * 1024 * 1024
    private const val DATA_CACHE_SIZE: Int = 16 * 1024 * 1024

    private var floorCache: LruCache<String, List<Floor>> = LruCache(DATA_CACHE_SIZE)

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

    fun putFloors(uid: String, floors: List<Floor>) {
        floorCache.put(uid, floors)
    }

    fun getFloors(uid: String) : List<Floor>? = floorCache.get(uid)
}