package com.example.burn_it.ui

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.burn_it.R
import com.example.burn_it.databinding.FragmentTrackingBinding
import com.example.burn_it.db.Run
import com.example.burn_it.services.Polyline
import com.example.burn_it.services.TrackingService
import com.example.burn_it.ui.dialog.CancelDataDialog
import com.example.burn_it.ui.viewModels.MainViewModel
import com.example.burn_it.utils.Constants.ACTION_PAUSE_SERVICE
import com.example.burn_it.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.burn_it.utils.Constants.ACTION_STOP_SERVICE
import com.example.burn_it.utils.Constants.MAP_ZOOM
import com.example.burn_it.utils.Constants.PERCENTAGE
import com.example.burn_it.utils.Constants.POLYLINE_COLOR
import com.example.burn_it.utils.Constants.POLYLINE_WIDTH
import com.example.burn_it.utils.Constants.POSITION
import com.example.burn_it.utils.Constants.TARGET
import com.example.burn_it.utils.TrackingUtility
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.round

const val CANCEL_DIALOG = "Cancel"
const val TAG = "Tracking Fragment"
@AndroidEntryPoint
class TrackingFragment : Fragment() {

    private var _binding: FragmentTrackingBinding? = null
    private val ui get() = _binding!!
    private val viewModel by viewModels<MainViewModel>()
    private var map: GoogleMap? = null
    private lateinit var mapView: MapView
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private var curTimeInMillis = 0L
    private var menu: Menu? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var target = 0F
    private var position = 0F
    private    var distanceInMeters = 0
    @set:Inject
    var weight = 80f
    @Inject
    lateinit var sharedPref: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        _binding = FragmentTrackingBinding.inflate(inflater, container, false)

        mapView = ui.mapView
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        mapView.getMapAsync {
            map = it
            addAllPolylines()
        }

        ui.btnToggleRun.setOnClickListener {
            toggleRun()
        }

        ui.btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
        }

        target = sharedPref.getFloat(TARGET, 1f)
        position = sharedPref.getFloat(POSITION, 0f)

        subscribeToObservers()
        getLocation()
        startLocation()

        /***call stop run on screen rotation**/
        if (savedInstanceState != null) {
            val cancelDialog =
                parentFragmentManager.findFragmentByTag(CANCEL_DIALOG) as CancelDataDialog?
            cancelDialog?.setYesListener {
                stopRun()
            }
        }
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(curTimeInMillis, true)
            ui.tvTimer.text = formattedTime
        })
    }

    private fun getLocation() {

        if (TrackingUtility.hasLocationPermissions(requireContext())) {

            locationRequest = LocationRequest().apply {
                interval = TimeUnit.SECONDS.toMillis(1000)
                fastestInterval = TimeUnit.SECONDS.toMillis(2000)
                maxWaitTime = TimeUnit.MINUTES.toMillis(1)
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    super.onLocationResult(locationResult)

                    if (locationResult?.lastLocation != null) {
                        val lat = locationResult.lastLocation.latitude
                        val long = locationResult.lastLocation.longitude

                        viewModel.getWeatherInfo(lat, long, ui.celsius, ui.weatherIcon)


                        viewModel.weatherData.observe(viewLifecycleOwner, Observer {

                            Timber.d("${it.main}")


                            val list = it.weather
                            for (info in list) {
                                ui.weatherType.text = info.description
                            }
                            ui.degrees.text = (round(it.main.temp - 273.15)).toString()
                            ui.today.text = it.name

                        })
                    } else {
                        Timber.d("Location missing in callback.")
                    }


                }
            }


        }
    }

    private fun startLocation() {
        if (TrackingUtility.hasLocationPermissions(requireContext())) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return
            }
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
                /***current thread will be use for the callBack**/
            )
        }
    }


    private fun addAllPolylines() {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyline() {
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }


    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking && curTimeInMillis > 0L) {
            ui.btnToggleRun.setText(R.string.start)
            ui.btnFinishRun.visibility = View.VISIBLE
        } else if (isTracking) {
            ui.btnToggleRun.setText(R.string.stop)
            menu?.getItem(0)?.isVisible = true
            ui.btnFinishRun.visibility = View.GONE
        }
    }

    private fun toggleRun() {
        if(isTracking) {
            sendCommandToService(ACTION_PAUSE_SERVICE)
            menu?.getItem(0)?.isVisible = true
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for(polyline in pathPoints) {
            if(polyline.isNotEmpty()) {
                for (pos in polyline) {
                    bounds.include(pos)
                }
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height * 0.05f).toInt()
                /**padding**/
            )
        )
    }

    fun saveImageOnInternalStorage(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b: ByteArray = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    private fun endRunAndSaveToDb() {
        map?.snapshot { bmp ->

            for(polyline in pathPoints) {
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }
            val avgSpeed = round((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 60 / 60) * 10) / 10f/**in km/hr round-off to 1dp**/
            val dateTimestamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
            val imagePath = saveImageOnInternalStorage(bmp)
            val run = Run(
                imagePath,
                dateTimestamp,
                avgSpeed,
                distanceInMeters,
                curTimeInMillis,
                caloriesBurned
            )
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run saved successfully",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if(curTimeInMillis > 0L) {
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.miCancelTracking -> {
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCancelTrackingDialog() {
        CancelDataDialog(R.string.cancel_mesage_track, R.string.cancel_title_track).apply{
            setYesListener {
                stopRun()
            }
        }.show(parentFragmentManager, CANCEL_DIALOG)
    }

    private fun getTargetResult(): Int{
      return  when(position){
          0f, 1f -> round((curTimeInMillis.toDouble() / target.toDouble()) * 100).toInt()
          2f -> {
              val inKm = distanceInMeters / 1000f
              round((inKm.toDouble() / target.toDouble()) * 100).toInt()
          }
          else -> round((curTimeInMillis.toDouble() / target.toDouble()) * 100).toInt()
      }
    }

    private fun stopRun() {
        ui.tvTimer.setText(R.string.timer)
        sendCommandToService(ACTION_STOP_SERVICE)
        if(target == 0f){
            findNavController().navigate(R.id.runFragment)
        }
        else{
            val percentage = getTargetResult()
           sharedPref.edit()
               .putFloat(PERCENTAGE, percentage.toFloat())
               .apply()
            Timber.d("$percentage, $curTimeInMillis, $target, $position")
            findNavController().navigate(R.id.runFragment)
        }

    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}