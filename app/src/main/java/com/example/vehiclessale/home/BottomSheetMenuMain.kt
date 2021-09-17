package com.example.vehiclessale.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.vehiclessale.R
import com.example.vehiclessale.databinding.BottomSheetChoosePhoneCallBinding
import com.example.vehiclessale.databinding.BottomSheetHomeBinding
import com.example.vehiclessale.utils.Const
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetMenuMain : BottomSheetDialogFragment(), View.OnClickListener {
    private var binding: BottomSheetHomeBinding? = null
    private lateinit var onPayOffClickEvent: OnBottomSheetMenuMain


    fun setOnClick(onPayOffClickEvent: OnBottomSheetMenuMain) {
        this.onPayOffClickEvent = onPayOffClickEvent
    }

    fun dismissDialog() {
        dismiss()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetHomeBinding.inflate(inflater, container, false)
        setOnClickForView()
        return binding?.root
    }

    private fun setOnClickForView() {
        binding?.tvShowCar?.setOnClickListener(this)
        binding?.tvShowElectricBike?.setOnClickListener(this)
        binding?.tvShowMotorbike?.setOnClickListener(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            onPayOffClickEvent = context as OnBottomSheetMenuMain
        } catch (e: Exception) {
            Log.d(TAG, "onAttach: ${e.message}")
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvShowCar -> {
                onPayOffClickEvent.onBottomSheetMenuMain(
                    Const.FRAGMENT_HOME_SHOW_CAR,
                )
            }
            R.id.tvShowElectricBike -> {
                onPayOffClickEvent.onBottomSheetMenuMain(
                    Const.FRAGMENT_HOME_SHOW_ELECTRIC_BIKE,
                )
            }
            R.id.tvShowMotorbike -> {
                onPayOffClickEvent.onBottomSheetMenuMain(
                    Const.FRAGMENT_HOME_SHOW_MOTORBIKE,
                )
            }
        }
    }

    companion object {
        private const val TAG = "BottomSheetPayOff"
    }

}