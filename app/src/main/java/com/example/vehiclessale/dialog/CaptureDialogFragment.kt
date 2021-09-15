package com.example.vehiclessale.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.example.vehiclessale.R

class CaptureDialogFragment(val onCaptureCallback: () -> Unit, val onGalleryCallback: () -> Unit) : DialogFragment() {

    lateinit var unbinder: Unbinder

    @BindView(R.id.imgCapture)
    lateinit var imgCapture: AppCompatImageView

    @BindView(R.id.imgGallery)
    lateinit var imgGallery: AppCompatImageView

    @BindView(R.id.btnCancel)
    lateinit var btnCancel: AppCompatButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.layout_choose_photo, container, false)
        unbinder = ButterKnife.bind(this, view)

        btnCancel.setOnClickListener { dialog?.dismiss() }
        imgCapture.setOnClickListener {
            onCaptureCallback.invoke()
            dialog?.dismiss()
        }
        imgGallery.setOnClickListener {
            onGalleryCallback.invoke()
            dialog?.dismiss()
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
        super.onStart()
    }

    companion object {
        fun newInstance(
                fragmentManager: FragmentManager,
                onCaptureCallback: () -> Unit = {},
                onGalleryCallback: () -> Unit = {}
        ) {
            val dialog = CaptureDialogFragment(onCaptureCallback, onGalleryCallback)
            dialog.show(fragmentManager, dialog.tag)
        }
    }
}