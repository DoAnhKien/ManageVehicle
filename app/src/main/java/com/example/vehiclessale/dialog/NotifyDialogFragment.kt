package com.example.vehiclessale.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.example.vehiclessale.R

class NotifyDialogFragment(private var type: String, private var value: String, private var onCallback: () -> Unit = {},
                           private var onCancelCallback: () -> Unit = {}): DialogFragment() {
    lateinit var unbinder: Unbinder

    @BindView(R.id.tvContent)
    lateinit var tvContent: AppCompatTextView
    @BindView(R.id.btnClose)
    lateinit var btnClose: AppCompatButton
    @BindView(R.id.btnActive)
    lateinit var btnActive: AppCompatButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =  inflater.inflate(R.layout.layout_notify_dialog, container, false)
        unbinder = ButterKnife.bind(this, view)
        tvContent.text = value
        btnActive.setOnClickListener {
            onCallback.invoke()
            dialog?.dismiss()
        }
        btnClose.setOnClickListener {
            dialog?.dismiss()
            onCancelCallback.invoke()
        }
        when(type){
            CHECK_INFO, SUCCESSFUL, FAIL ->{
                btnClose.isVisible = false
                btnActive.text = getString(R.string.txt_Cancel)
            }
            LOGIN -> {
                btnClose.text = getString(R.string.txt_Cancel)
                btnActive.text = getString(R.string.txt_login)
            }
        }
        return view
    }

    override fun onStart() {
        val params = dialog?.window?.attributes
        // Assign window properties to fill the parent
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog?.window?.attributes = params as WindowManager.LayoutParams
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.setCancelable(false)
        super.onStart()
    }

    companion object{
        const val CHECK_INFO = "Check_info"
        const val LOGIN = "login"

        const val SUCCESSFUL = "Success"
        const val FAIL = "Fail"

        fun newInstance(
                fragmentManager: FragmentManager,
                content: String,
                type: String,
                onCancelCallback: () -> Unit = {},
                onCallback: () -> Unit = {}
        ) {
            val dialog = NotifyDialogFragment(type, content, onCallback,onCancelCallback)
            dialog.show(fragmentManager, dialog.tag)
        }
    }


}