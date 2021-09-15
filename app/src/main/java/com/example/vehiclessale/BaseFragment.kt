package com.example.vehiclessale

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import com.example.vehiclessale.dialog.LoadingDialogFragment

abstract class BaseFragment: Fragment() {


    val dialog: LoadingDialogFragment by lazy {
        LoadingDialogFragment()
    }

    val fManager: FragmentManager by lazy {
        requireActivity().supportFragmentManager
    }

    fun backToPrevious() {
        this.findNavController().popBackStack()
    }

    fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    fun showDialogLoading(){
        dialog.show(fManager, "")
    }
    fun hideDialogLoading(){
        try {
            dialog.dismiss()
        }catch (e: Exception){}
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initUI()
        initControl()
        super.onViewCreated(view, savedInstanceState)
    }


    protected abstract fun initUI()
    protected abstract fun initControl()
}