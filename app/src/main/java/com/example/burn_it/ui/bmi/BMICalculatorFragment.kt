package com.example.burn_it.ui.bmi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.burn_it.databinding.FragmentBMIBinding


class BMICalculatorFragment : Fragment() {

    private var _binding: FragmentBMIBinding? = null
    private val ui get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBMIBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}