package com.example.burn_it.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.Toast
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
import com.example.burn_it.ui.viewModels.MainViewModel
import com.example.burn_it.utils.Constants.ACTION_PAUSE_SERVICE
import com.example.burn_it.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.burn_it.utils.Constants.ACTION_STOP_SERVICE
import com.example.burn_it.utils.Constants.MAP_ZOOM
import com.example.burn_it.utils.Constants.POLYLINE_COLOR
import com.example.burn_it.utils.Constants.POLYLINE_WIDTH
import com.example.burn_it.utils.TrackingUtility
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.round

const val CANCEL_DIALOG = "Cancel"
const val TAG = "Tracking Fragment"
@AndroidEntryPoint
class TrackingFragment : Fragment() {

    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!
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



    @set:Inject
    var weight = 80f


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        _binding = FragmentTrackingBinding.inflate(inflater, container, false)

        mapView = binding.mapView
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        mapView.getMapAsync {
            map = it
            addAllPolylines()
            //setMapStyle(it)
            //it.mapType = GoogleMap.MAP_TYPE_NORMAL
        }

        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }

        binding.btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
        }


        subscribeToObservers()
        getLocation()
        startLocation()


        /***call stop run on screen rotation**/
        if (savedInstanceState != null) {
            val cancelDialog =
                parentFragmentManager.findFragmentByTag(CANCEL_DIALOG) as CancelTrackingDialogFragment?
            cancelDialog?.setYesListener {
                stopRun()
            }
        }


    }

    /**map style func**/
    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_dark_style
                )
            )

            if (!success) {
               Timber.e("Style parsing failed.")

            }
        } catch (e: Resources.NotFoundException) {
            Timber.e(e, "Can't find style. Error: ")
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
            binding.tvTimer.text = formattedTime
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

                        viewModel.getWeatherInfo(lat, long)

                        viewModel.weatherData.observe(viewLifecycleOwner, Observer {

                            Timber.d("${it.main}")


                            val list = it.weather
                            for (info in list) {
                                binding.weatherType.text = info.description
                            }
                            binding.degrees.text = (round(it.main.temp - 273.15)).toString()
                            binding.today.text = it.name

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
            binding.btnToggleRun.setText(R.string.start)
            binding.btnFinishRun.visibility = View.VISIBLE
        } else if (isTracking) {
            binding.btnToggleRun.setText(R.string.stop)
            menu?.getItem(0)?.isVisible = true
            binding.btnFinishRun.visibility = View.GONE
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
            for(pos in polyline) {
                bounds.include(pos)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height * 0.05f).toInt()/**padding**/
            )
        )
    }

    /***
     * FOR WEEKLY TARGET
     * 1. convert target time to millisec
     * 2. compare with the total time ran
     *      1. if they are equal--mission complete...
     *
     * **/
    private fun saveImageOnInternalStorage(bmp : Bitmap): String{
        var outputStream:FileOutputStream? = null
        val filePath = Environment.getExternalStorageDirectory().toString()
        val dir = File("$filePath/BurnIt/")
        dir.mkdirs()
        val child = System.currentTimeMillis().toString() + ".jpg"
        val file = File(dir, child)
        try {
            outputStream = FileOutputStream(file)
        }catch (e: FileNotFoundException){
            e.printStackTrace()
        }
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        Toast.makeText(requireContext(), "saved to internal storage", Toast.LENGTH_SHORT).show()

        try {
            outputStream?.flush()
        }catch (e: IOException){
            e.printStackTrace()
        }
        try {
            outputStream?.close()
        }catch (e: IOException){
            e.printStackTrace()
        }
        Timber.d( "saveImageOnInternalStorage: $file")
        return file.absolutePath
    }
    private fun endRunAndSaveToDb() {
        map?.snapshot { bmp ->
            var distanceInMeters = 0
            for(polyline in pathPoints) {
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }
            val avgSpeed = round((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 60 / 60) * 10) / 10f/**in km/hr round-off to 1dp**/
            val dateTimestamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
            val imagePath = saveImageOnInternalStorage(bmp)
            val run = Run(imagePath, dateTimestamp, avgSpeed, distanceInMeters, curTimeInMillis, caloriesBurned)
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
        CancelTrackingDialogFragment().apply{
            setYesListener {
                stopRun()
            }
        }.show(parentFragmentManager, CANCEL_DIALOG)
    }

    private fun stopRun() {
        binding.tvTimer.setText(R.string.timer)
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
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