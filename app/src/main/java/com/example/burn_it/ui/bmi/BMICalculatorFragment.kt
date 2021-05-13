package com.example.burn_it.ui.bmi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.burn_it.R
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ui.gender.femaleCard.setOnClickListener {
            selectFemale()
        }
        ui.gender.maleCard.setOnClickListener {
            selectMale()
        }
        ui.age.arrowUp.setOnClickListener {
            ui.age.arrowDown.alpha = 1f
          val currValue =  ui.age.ageValue1.text.toString()
            var newValue = currValue.toInt() + 1
            if(newValue > 99) {
                ui.age.arrowUp.alpha = .3f
                newValue = 99
            }
            ui.age.ageValue1.text = newValue.toString()
        }
        ui.age.arrowDown.setOnClickListener {
            ui.age.arrowUp.alpha = 1f
            val currValue =  ui.age.ageValue1.text.toString()
            var newValue = currValue.toInt() - 1
            if(newValue < 16) {
                ui.age.arrowDown.alpha = .3f
                newValue = 16
            }
            ui.age.ageValue1.text = newValue.toString()
        }


        ui.bodyMetrics.imperialRadio.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                ui.bodyMetrics.imperialInfo.visibility = View.VISIBLE
                ui.bodyMetrics.metricInfo.visibility = View.GONE
            }
        }
        ui.bodyMetrics.metricRadio.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                ui.bodyMetrics.imperialInfo.visibility = View.GONE
                ui.bodyMetrics.metricInfo.visibility = View.VISIBLE
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun selectFemale() {
        ui.gender.femaleCard.setCardBackgroundColor(
            ContextCompat.getColor(
                requireContext(), R.color.colorPrimary
            )
        )
        context?.resources?.getColor(R.color.colorWhite)?.let {
            ui.gender.femaleImg.setColorFilter(
                it
            )
        }
        ui.gender.maleCard.setCardBackgroundColor(
            ContextCompat.getColor(
                requireContext(), R.color.colorDefaultGrey
            )
        )
        context?.resources?.getColor(R.color.colorDefaultBlack)?.let {
            ui.gender.maleImg.setColorFilter(
                it
            )
        }

    }

    private fun selectMale() {
        ui.gender.maleCard.setCardBackgroundColor(
            ContextCompat.getColor(
                requireContext(), R.color.colorPrimary
            )
        )
        context?.resources?.getColor(R.color.colorWhite)?.let {
            ui.gender.maleImg.setColorFilter(
                it
            )
        }
        ui.gender.femaleCard.setCardBackgroundColor(
            ContextCompat.getColor(
                requireContext(), R.color.colorDefaultGrey
            )
        )
        context?.resources?.getColor(R.color.colorDefaultBlack)?.let {
            ui.gender.femaleImg.setColorFilter(
                it
            )
        }

    }


}