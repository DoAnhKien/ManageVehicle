package com.example.vehiclessale.base

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets

import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding


abstract class BaseActivity<T : ViewDataBinding?>(private var binding: T? = null) :
    AppCompatActivity() {

    private lateinit var viewRoot: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.binding = DataBindingUtil.setContentView<T>(this, this.initLayout())!!
        this.viewRoot = this.binding!!.root
        this.init()
        this.initViews()
        this.setOnClickForViews()
    }

    protected abstract fun initLayout(): Int

    protected abstract fun init()

    protected abstract fun setOnClickForViews()

    protected abstract fun initViews()

    protected open fun fullscreen(): Boolean {
        return false
    }

    protected open fun statusTranSlucent(): Boolean {
        return false
    }

    protected open fun navigationTranSlucent(): Boolean {
        return false
    }

    private fun setupFullScreen() {
        val window = window ?: return
        if (Build.VERSION.SDK_INT < 30) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            return
        }
//        val controller = window.insetsController
//        if (controller != null) {
//            controller.hide(WindowInsets.Builder.statusBars() or WindowInsets.Type.navigationBars())
//            controller.systemBarsBehavior =
//                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//        }
    }


}