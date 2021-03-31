package com.example.burn_it.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.burn_it.R
import com.example.burn_it.adapters.RunAdapter
import com.example.burn_it.databinding.FragmentRunBinding
import com.example.burn_it.ui.viewModels.MainViewModel
import com.example.burn_it.utils.Constants.PERCENTAGE
import com.example.burn_it.utils.Constants.POSITION
import com.example.burn_it.utils.Constants.TARGET
import com.example.burn_it.utils.IntroBalloonFactory.Companion.balloonBuilder
import com.example.burn_it.utils.SortType
import com.example.burn_it.utils.TrackingUtility
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.skydoves.balloon.Balloon
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import javax.inject.Inject



const val REQUEST_CODE_LOCATION_PERMISSION = 1

@AndroidEntryPoint
class RunFragment : Fragment(), EasyPermissions.PermissionCallbacks {
    private val viewModel by viewModels<MainViewModel>()
    private var _binding: FragmentRunBinding? = null
    private val ui get() = _binding!!
    private lateinit var runAdapter: RunAdapter
    private var runResult = 0F
    @set:Inject
    var isFirstAppOpen = true
    @Inject
    lateinit var sharedPref: SharedPreferences
    private lateinit var bottomNav: BottomNavigationView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRunBinding.inflate(inflater, container, false)
        bottomNav = activity?.findViewById(R.id.bottomNavigationView)!!

        return ui.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isFirstAppOpen) {
            showBalloon()
        }

        ui.fabRun.setOnClickListener {
            findNavController().navigate(R.id.trackingFragment)
        }

        ui.fabTarget.setOnClickListener {
            findNavController().navigate(R.id.targetFragment)
        }

        lifecycleScope.launch {
            callPermissions()
        }


        setupRecyclerView()
        val spFilter = ui.spFilter
        when (viewModel.sortType) {
            SortType.DATE -> spFilter.setSelection(0)
            SortType.RUNNING_TIME -> spFilter.setSelection(1)
            SortType.DISTANCE -> spFilter.setSelection(2)
            SortType.AVG_SPEED -> spFilter.setSelection(3)
            SortType.CALORIES_BURNED -> spFilter.setSelection(4)
        }

        spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                pos: Int,
                id: Long
            ) {
                when (pos) {
                    0 -> viewModel.sortRuns(SortType.DATE)
                    1 -> viewModel.sortRuns(SortType.RUNNING_TIME)
                    2 -> viewModel.sortRuns(SortType.DISTANCE)
                    3 -> viewModel.sortRuns(SortType.AVG_SPEED)
                    4 -> viewModel.sortRuns(SortType.CALORIES_BURNED)
                }
            }
        }

        viewModel.runs.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty()) {
                ui.spFilter.visibility = View.GONE
                ui.tvFilterBy.visibility = View.GONE
                ui.center.visibility = View.VISIBLE
            }
            runAdapter.submitList(it)
        })

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val run = runAdapter.differ.currentList[position]
                viewModel.deleteRun(run)
                Snackbar.make(view, "Successfully deleted run", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo") {
                        viewModel.insertRun(run)
                    }
                    show()
                }
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(ui.rvRuns)
        }

        runResult = sharedPref.getFloat(PERCENTAGE, 0F)
        Timber.d("$runResult")

        displayedResult(runResult, ui.result)
    }

    private fun showBalloon() {
        val targetBalloon =
            balloonBuilder(requireContext(), viewLifecycleOwner)
        val runBalloon =
            balloonBuilder(requireContext(), viewLifecycleOwner)
        val runBottomBalloon =
            balloonBuilder(requireContext(), viewLifecycleOwner)
        val statsBottomBalloon =
            balloonBuilder(requireContext(), viewLifecycleOwner)
        val settingsBottomBalloon =
            balloonBuilder(requireContext(), viewLifecycleOwner)

        setUpBalloon(targetBalloon, R.string.target_text)
        setUpBalloon(runBalloon, R.string.run_text)
        setUpBalloon(runBottomBalloon, R.string.all_runs)
        setUpBalloon(statsBottomBalloon, R.string.run_stats)
        setUpBalloon(settingsBottomBalloon, R.string.settings_text, R.string.done)

        targetBalloon
            .relayShow(runBalloon, ui.fabRun)
            .relayShowAlignBottom(runBottomBalloon, ui.center)
            .relayShow(statsBottomBalloon, bottomNav, 130, 0)
            .relayShowAlignTop(settingsBottomBalloon, bottomNav, 390, 0)

        targetBalloon.showAlignTop(ui.fabTarget)


    }

    private fun setUpBalloon(balloon: Balloon, @StringRes text: Int, btnText : Int = R.string.next) {
        val btn = balloon.getContentView().findViewById<Button>(R.id.balloon_btn)
        val textView = balloon.getContentView().findViewById<TextView>(R.id.balloon_text)
        textView.text = getString(text)
        btn.text = getString(btnText)
        btn.setOnClickListener { balloon.dismiss() }
    }

    private suspend fun callPermissions() {
        delay(30000)
        requestPermissions()
    }

    private fun setupRecyclerView() = ui.rvRuns.apply {
        runAdapter = RunAdapter()
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    @SuppressLint("SetTextI18n")
    private fun displayedResult(num: Float, textView: TextView) {
        when {
            num == 0f -> {
                textView.visibility = View.GONE
            }
            num < 70f -> {
                textView.text = "${num.toInt()}% complete, you need to sit up"
                textView.visibility = View.VISIBLE
            }
            num <= 99f -> {
                textView.text = "${num.toInt()}%  complete, you were so close"
                textView.visibility = View.VISIBLE
            }
            num == 100f -> {
                textView.text = "${num.toInt()}% complete, you are a beast"
                textView.visibility = View.VISIBLE
            }
            num > 100f -> {
                textView.text = "Way over your target. World Greatest!!!"
                textView.visibility = View.VISIBLE
            }
        }
        clearFromSharedPref()
    }

    private fun clearFromSharedPref(){
        sharedPref.edit()
            .remove(PERCENTAGE)
            .remove(POSITION)
            .remove(TARGET)
            .apply()
    }

    private fun requestPermissions() {
        if (TrackingUtility.hasLocationPermissions(requireContext())) {
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        }

    }


    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()

        } else {
            requestPermissions()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onSaveInstanceState(outState: Bundle) {

    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}