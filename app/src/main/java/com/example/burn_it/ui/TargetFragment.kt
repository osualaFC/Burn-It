package com.example.burn_it.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.burn_it.R
import com.example.burn_it.databinding.FragmentTargetBinding
import com.example.burn_it.utils.Constants.POSITION
import com.example.burn_it.utils.Constants.TARGET
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TargetFragment : Fragment() {
    private var _binding: FragmentTargetBinding? = null
    private val ui get() = _binding!!

    @Inject
    lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTargetBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ui.setTodayTarget.setOnClickListener {
            val targetTime = ui.etTodayTarget.text.toString()
            val position = ui.spFilter.selectedItemPosition

            val target  = when (position) {
                0 -> targetTime.toInt().minsToMilliSec()
                1 -> targetTime.toInt().hrsToMilliSec()
                else -> targetTime.toInt().toFloat()
            }

            sharedPref.edit()
                .putFloat(TARGET, target)
                .putFloat(POSITION, position.toFloat())
                .apply()

            findNavController().navigate(R.id.trackingFragment)
        }
    }

    private fun Int.minsToMilliSec(): Float {
        return (this * 60000).toFloat()
    }

    private fun Int.hrsToMilliSec(): Float {
        return (this * 60000).toFloat()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}