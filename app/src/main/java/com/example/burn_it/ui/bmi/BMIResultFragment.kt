package com.example.burn_it.ui.bmi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.burn_it.R
import com.example.burn_it.databinding.FragmentBMI2Binding
import com.example.burn_it.databinding.FragmentBMIResultBinding

class BMIResultFragment : Fragment() {

    private var _binding: FragmentBMIResultBinding? = null
    private val ui get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBMIResultBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}