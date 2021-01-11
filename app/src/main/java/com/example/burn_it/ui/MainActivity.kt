package com.example.burn_it.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.burn_it.R
import com.example.burn_it.databinding.ActivityMainBinding
import com.example.burn_it.utils.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var ui: ActivityMainBinding
    lateinit var navHost: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)

        setSupportActionBar(ui.toolbar)
         navHost = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val controller = navHost.findNavController()
        val bottomNav =  ui.bottomNavigationView

        /**if activity is destroyed**/
        navigateToTrackingFragmentIfNeeded(intent)

       bottomNav.setupWithNavController(controller)
        bottomNav.setOnNavigationItemReselectedListener { /*NO-OP*/ }

       controller.addOnDestinationChangedListener { _, destination, _ ->
                when(destination.id) {
                    R.id.settingsFragment, R.id.runFragment, R.id.statisticsFragment ->
                        bottomNav.visibility = View.VISIBLE
                    else -> bottomNav.visibility = View.GONE
                }
            }
    }
        /***when activity is not destroyed**/
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        if(intent?.action == ACTION_SHOW_TRACKING_FRAGMENT) {
            navHost.findNavController().navigate(R.id.action_global_trackingFragment)
        }
    }


}

