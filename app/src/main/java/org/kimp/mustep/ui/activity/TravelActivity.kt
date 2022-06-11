package org.kimp.mustep.ui.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.here.sdk.core.Anchor2D
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.Point2D
import com.here.sdk.mapviewlite.MapImageFactory
import com.here.sdk.mapviewlite.MapMarker
import com.here.sdk.mapviewlite.MapMarkerImageStyle
import com.here.sdk.mapviewlite.MapStyle
import com.here.sdk.mapviewlite.PickMapItemsCallback
import com.here.sdk.mapviewlite.WatermarkPlacement
import com.squareup.picasso.Picasso
import java.util.stream.Collectors
import org.kimp.mustep.R
import org.kimp.mustep.databinding.ActivityTravelBinding
import org.kimp.mustep.databinding.ViewFloorButtonBinding
import org.kimp.mustep.domain.Floor
import org.kimp.mustep.domain.Point
import org.kimp.mustep.domain.University
import org.kimp.mustep.rest.MuStepServiceBuilder
import org.kimp.mustep.utils.AppCache
import org.kimp.mustep.utils.PreferencesData
import org.kimp.mustep.utils.service.MediaPoolService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.min


class TravelActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTravelBinding
    private lateinit var university: University

    private var markers: ArrayList<MapMarker> = ArrayList()
    private var floors: List<Floor> = ArrayList()

    private lateinit var activeFloor: Floor
    private lateinit var activePoint: Point

    private var soundSelected: Boolean = false

    private lateinit var mService: MediaPoolService
    private var mBound: Boolean = false

    private val sliderUpdatePeriod: Long = 250

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTravelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.taMapView.onCreate(savedInstanceState)
        binding.taMapView.setWatermarkPosition(WatermarkPlacement.BOTTOM_CENTER, 20)

        university = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (savedInstanceState == null)
                intent.extras?.getParcelable("university", University::class.java)!!
            else
                savedInstanceState.getParcelable("university", University::class.java)!!
        } else {
            if (savedInstanceState == null)
                intent.extras?.getParcelable<University>("university") as University
            else
                savedInstanceState.getParcelable<University>("university") as University
        }

        binding.taContentSv.setMaxHeight(512);
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.taContentSv.setMaxHeight(256);
        }

        loadUniversityData()
        connectSignals()
    }

    private fun connectSignals() {
        soundProgressUpdateHandler.postDelayed(soundProgressUpdateRunnable, sliderUpdatePeriod)

        binding.taBackBtn.setOnClickListener { finish() }
        binding.taCloseBtn.setOnClickListener { unloadPoint() }

        binding.taMapView.gestures.setTapListener(this::pickMapMarker)

        binding.taFloorsTg.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                val floor =
                    (group.findViewById(checkedId) as MaterialButton).text.toString()
                loadFloor(Integer.valueOf(floor).toLong())
            }
        }

        binding.taSoundSlider.addOnChangeListener(Slider.OnChangeListener { _, value, fromUser ->
            if (fromUser && soundSelected && mBound) {
                mService.seekToProgress(value)
            }
        })

        binding.taPlayPauseBtn.setOnClickListener {
            if (mBound && mService.isPlaying()) {
                mService.pause()
                binding.taPlayPauseBtn.icon =
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_play, theme)
            } else {
                if (!soundSelected && mBound) {
                    mService.setSource(
                        AppCache.getCacheSupportUri(
                            String.format(
                                "%s/floor_%d/%s_%s.mp3",
                                university.uid,
                                activeFloor.number,
                                activePoint.uid,
                                PreferencesData.getAudioSuffix()
                            ), this
                        )
                    )
                    soundSelected = true
                }
                binding.taPlayPauseBtn.icon =
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_pause, theme)
                if (mBound) mService.playOrResume()
            }
        }
    }

    private fun loadUniversityData() {
        if (AppCache.getFloors(university.uid) != null) {
            floors = AppCache.getFloors(university.uid)!!
            finishLoading()
            return
        }

        MuStepServiceBuilder.build()
            .getUniversityData(university.uid)
            .enqueue(
                object : Callback<List<Floor>> {
                    override fun onResponse(
                        call: Call<List<Floor>>,
                        response: Response<List<Floor>>
                    ) {
                        floors = response.body()!!
                        AppCache.putFloors(university.uid, floors)

                        finishLoading()
                    }

                    override fun onFailure(call: Call<List<Floor>>, t: Throwable) {
                        Snackbar.make(
                            binding.root,
                            String.format(
                                "%s: %s",
                                resources.getString(R.string.error_preview),
                                t.localizedMessage
                            ),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            )
    }

    private fun finishLoading() {
        loadFloorButtons()
        loadMapScene()

        loadFloor(
            floors.stream().map { (number): Floor -> number }.sorted().collect(
                Collectors.toList()
            )[0]
        )
    }

    private fun loadFloorButtons() {
        floors.stream().map { (number): Floor -> number }.sorted().forEach { integer: Long ->
            val floorButton: MaterialButton = ViewFloorButtonBinding.inflate(
                layoutInflater
            ).root
            floorButton.text = integer.toString()

            binding.taFloorsTg.addView(
                floorButton,
                binding.taFloorsTg.childCount,
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
            )
        }
    }

    private fun loadMapScene() {
        binding.taMapView.mapScene.loadScene(
            MapStyle.NORMAL_DAY
        ) { errorCode ->
            if (errorCode == null) {
                binding.taMapView.camera.target =
                    GeoCoordinates(university.latitude, university.longitude)
                binding.taMapView.camera.zoomLevel = 18.0
                binding.taMapView.camera.tilt = 0.0
            } else {
                Snackbar.make(
                    binding.root,
                    String.format(
                        "%s: %s",
                        resources.getString(R.string.error_preview),
                        errorCode.name
                    ),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun loadFloor(number: Long) {
        unloadPoint()
        for (mapMarker in markers) binding.taMapView.mapScene.removeMapMarker(mapMarker)
        markers = ArrayList()

        binding.taFloorsTg.check(
            binding.taFloorsTg.getChildAt(
                floors.stream().map { (number1): Floor -> number1 }.sorted()
                    .collect(Collectors.toList()).indexOf(number)
            ).id
        )
        val floor: Floor = floors.stream().filter { (number1): Floor ->
            number1 == number
        }.collect(Collectors.toList())[0]

        activeFloor = floor

        val mapMarkerImageStyle = MapMarkerImageStyle()
        mapMarkerImageStyle.anchorPoint = Anchor2D(0.5, 0.5)
        mapMarkerImageStyle.isFlat = true

        for (point in floor.points) {
            val marker = MapMarker(GeoCoordinates(point.latitude, point.longitude))
            marker.addImage(
                MapImageFactory.fromBitmap(createMarkerBitmap(point)),
                mapMarkerImageStyle
            )
            val metadata = com.here.sdk.core.Metadata()
            metadata.setInteger("number", point.number)
            marker.metadata = metadata

            binding.taMapView.mapScene.addMapMarker(marker)
            markers.add(marker)
        }
    }

    private fun loadPoint(number: Int) {
        binding.taCloseBtn.visibility = View.VISIBLE
        binding.taPlayPauseBtn.visibility = View.VISIBLE
        binding.taSoundSlider.visibility = View.VISIBLE

        val point = activeFloor.points.stream()
            .filter { (_, number1): Point ->
                number1 == number
            }.collect(Collectors.toList())[0]
        activePoint = point


        binding.taContentSv.visibility = View.VISIBLE
        binding.taPointNameMsg.text = point.name.getTranslatedValue()
        binding.taPointDescMsg.text = String.format("\t%s", point.data.getTranslatedValue())

        Picasso.get()
            .load(
                AppCache.getCacheSupportUri(
                    String.format(
                        "%s/floor_%d/%s.png",
                        university.uid,
                        activeFloor.number,
                        point.uid
                    ), this
                )
            ).placeholder(R.drawable.ic_downloading)
            .into(binding.taPointImage)
    }

    private fun pickMapMarker(touchPoint: Point2D) {
        val geoCoordinates = binding.taMapView.camera.viewToGeoCoordinates(touchPoint)
        Log.e(
            "Woof",
            "\"latitude\": " + geoCoordinates.latitude + ", \"longitude\": " + geoCoordinates.longitude + ","
        )

        val radiusInPixel = 2f

        binding.taMapView.pickMapItems(touchPoint, radiusInPixel.toDouble(),
            PickMapItemsCallback { pickMapItemsResult ->
                if (pickMapItemsResult == null) {
                    return@PickMapItemsCallback
                }
                val topmostMapMarker =
                    pickMapItemsResult.topmostMarker ?: return@PickMapItemsCallback
                unloadPoint()
                loadPoint(topmostMapMarker.metadata!!.getInteger("number")!!)
            })
    }

    private fun createMarkerBitmap(point: Point): Bitmap {
        val base: Bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(base)

        val drawable: Drawable =
            ResourcesCompat.getDrawable(resources, R.drawable.ic_marker, theme)!!
        drawable.setBounds(0, 0, 64, 64)
        drawable.draw(canvas)

        val typedValue = TypedValue()
        theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.textAlign = Paint.Align.CENTER
        paint.style = Paint.Style.FILL
        paint.textSize = 14f
        paint.color = typedValue.data

        canvas.drawText(
            point.number.toString(),
            36 - paint.measureText(point.number.toString()) / 2,
            36.0f,
            paint
        )
        return base
    }

    private fun unloadPoint() {
        binding.taContentSv.visibility = View.GONE
        binding.taPointNameMsg.setText(R.string.ta_choose_point)
        binding.taCloseBtn.visibility = View.GONE
        binding.taPlayPauseBtn.visibility = View.GONE
        binding.taPlayPauseBtn.icon =
            ResourcesCompat.getDrawable(resources, R.drawable.ic_play, theme)

        soundSelected = false
        if (mBound) mService.stopPlaying()

        binding.taSoundSlider.value = 0.0f
        binding.taSoundSlider.visibility = View.GONE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("university", university)
    }

    override fun onStart() {
        super.onStart()

        Intent(this, MediaPoolService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onPause() {
        super.onPause()
        binding.taMapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.taMapView.onResume()
    }

    override fun onStop() {
        super.onStop()

        unbindService(connection)
        mBound = false
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.taMapView.onDestroy()
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MediaPoolService.MediaPoolBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    private val soundProgressUpdateHandler = Handler(Looper.getMainLooper())
    private val soundProgressUpdateRunnable = object : Runnable {
        override fun run() {
            if (mBound && soundSelected)
                binding.taSoundSlider.value = mService.getProgress()
            soundProgressUpdateHandler.postDelayed(this, sliderUpdatePeriod)
        }
    }
}
