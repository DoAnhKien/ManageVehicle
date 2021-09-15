package com.example.vehiclessale.base


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment


abstract class BaseFragmentA<T : ViewDataBinding?>(protected var binding: T? = null) :
    Fragment() {

    private lateinit var viewRoot: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.binding =
            DataBindingUtil.inflate<T>(inflater, this.initLayout(), container, false)
        this.viewRoot = binding!!.root

        /*
        * 1
        * */
        this.init()

        /*
        * 2
        * */
        this.initViews()
        this.setOnClickForViews()

        return this.viewRoot
    }

    protected abstract fun initLayout(): Int

    protected abstract fun init()

    protected abstract fun setOnClickForViews()

    protected abstract fun initViews()

}