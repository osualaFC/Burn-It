package com.example.burn_it.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.burn_it.databinding.FragmentTargetBinding


class TargetFragment : Fragment() {
    private var _binding: FragmentTargetBinding? = null
    private val ui get() = _binding!!


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

            val target = when (position) {
                0 -> targetTime.toInt().minstoMilliSec()
                1 -> targetTime.toInt().hrstoMilliSec()
                else -> targetTime.toInt().toLong()
            }

            val action = TrackingFragmentDirections.actionGlobalTrackingFragment(target, position)
            findNavController().navigate(action)
        }
    }

    fun Int.minstoMilliSec(): Long {
        return (this * 60000).toLong()
    }

    fun Int.hrstoMilliSec(): Long {
        return (this * 60000).toLong()
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}