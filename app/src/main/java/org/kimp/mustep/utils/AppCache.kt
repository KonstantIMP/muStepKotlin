package org.kimp.mustep.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.net.Uri
import android.util.Log
import android.util.LruCache
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.google.common.reflect.TypeToken
import com.google.gson.GsonBuilder
import java.io.File
import java.lang.reflect.Type
import java.nio.file.Files
import org.kimp.mustep.domain.Floor
import org.kimp.mustep.domain.University

object AppCache {
    const val IMAGE_CACHE_SIZE: Long = 32 * 1024 * 1024
    private const val DATA_CACHE_SIZE: Int = 16 * 1024 * 1024

    private var floorCache: LruCache<String, List<Floor>> = LruCache(DATA_CACHE_SIZE)

    fun getCacheSupportUri(relativePath: String, context: Context): Uri {
        Log.i("Cache", String.format("Requested: %s", relativePath))

        val local = File(String.format("%s%s%s", context.cacheDir, File.separator, relativePath))
        if (local.exists()) return Uri.fromFile(local)

        var s3Client = AmazonS3Client(StorageCredentials(), Region.getRegion(
            Regions.US_WEST_2
        ))
        s3Client.endpoint = "storage.yandexcloud.net"

        return Uri.parse(
            s3Client.getResourceUrl("mustep", relativePath)
        )
    }

    fun isUniversityCached(uid: String, context: Context) : Boolean {
        return File(context.cacheDir, uid).exists() &&
                context.getSharedPreferences(PreferencesData.BASE_PREFERENCES_NAME, MODE_PRIVATE)
                    .getStringSet("cached", HashSet())!!.contains(uid)
    }

    fun loadCachedFloors(context: Context) {
        for (uid in context.getSharedPreferences(PreferencesData.BASE_PREFERENCES_NAME, MODE_PRIVATE)
            .getStringSet("cached", HashSet())!!) {
            try {
                val floors = File(context.cacheDir, String.format("%s%sfloors.txt", uid, File.separator))
                val listType: Type = object : TypeToken<List<Floor>>() {}.type
                (GsonBuilder().create().fromJson(
                    String(Files.readAllBytes(floors.toPath())), listType
                ) as List<Floor>).apply {
                    putFloors(uid, this)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun putFloors(uid: String, floors: List<Floor>) {
        floorCache.put(uid, floors)
    }

    fun getFloors(uid: String) : List<Floor>? = floorCache.get(uid)
}