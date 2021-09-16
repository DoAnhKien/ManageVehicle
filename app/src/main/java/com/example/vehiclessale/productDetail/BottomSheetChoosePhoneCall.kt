package com.example.vehiclessale.productDetail

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.vehiclessale.R
import com.example.vehiclessale.databinding.BottomSheetChoosePhoneCallBinding
import com.example.vehiclessale.utils.Const
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetChoosePhoneCall : BottomSheetDialogFragment(), View.OnClickListener {
    private var binding: BottomSheetChoosePhoneCallBinding? = null
    private lateinit var onPayOffClickEvent: OnBottomSheetClick
    private var userPhone: String? = null


    fun setUserPhoneNumber(userPhone: String?) {
        this.userPhone = userPhone
    }

    fun setOnClick(onPayOffClickEvent: OnBottomSheetClick) {
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
        binding = BottomSheetChoosePhoneCallBinding.inflate(inflater, container, false)
        setOnClickForView()
        return binding?.root
    }

    private fun setOnClickForView() {
        binding?.tvSendMessageToZalo?.setOnClickListener(this)
        binding?.tvSendMessage?.setOnClickListener(this)
        binding?.tvMakePhoneCall?.setOnClickListener(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            onPayOffClickEvent = context as OnBottomSheetClick
        } catch (e: Exception) {
            Log.d(TAG, "onAttach: ${e.message}")
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvMakePhoneCall -> {
                onPayOffClickEvent.onBottomSheetClick(
                    Const.FRAGMENT_EDIT_USER_MAKE_PHONE_CALL,
                    userPhone
                )
            }
            R.id.tvSendMessage -> {
                onPayOffClickEvent.onBottomSheetClick(
                    Const.FRAGMENT_EDIT_USER_SEND_MESSAGE,
                    userPhone
                )
            }
            R.id.tvSendMessageToZalo -> {
                onPayOffClickEvent.onBottomSheetClick(
                    Const.FRAGMENT_EDIT_USER_MAKE_PHONE_CALL_BY_ZALO,
                    userPhone
                )
            }
        }
    }

    companion object {
        private const val TAG = "BottomSheetPayOff"
    }

}