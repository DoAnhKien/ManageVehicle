package com.example.vehiclessale.profile

import android.transition.TransitionInflater
import android.view.View
import com.example.vehiclessale.BaseFragment
import com.example.vehiclessale.R
import com.example.vehiclessale.UserPrefs
import com.example.vehiclessale.base.BaseFragmentA
import com.example.vehiclessale.databinding.FragmentEditUserBinding
import com.example.vehiclessale.login.LoginData
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_header.*

class EditUserFragment : BaseFragmentA<FragmentEditUserBinding>(), View.OnClickListener {

    override fun initLayout(): Int = R.layout.fragment_edit_user

    override fun init() {
        actionTransition()
        initData()
    }

    override fun setOnClickForViews() {
        imgBack.setOnClickListener(this)
        binding?.btnCancelSaveInformation?.setOnClickListener(this)
        binding?.btnSaveInformation?.setOnClickListener(this)
    }

    override fun initViews() {

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {

            }
            R.id.btnCancelSaveInformation -> {

            }
            R.id.btnSaveInformation -> {

            }
        }
    }

    private fun initData() {
        var user = Gson().fromJson(UserPrefs.getLocalData(requireContext()), LoginData::class.java)
        binding?.user = user
    }

    private fun actionTransition() {
        val inflater = TransitionInflater.from(requireContext())
        exitTransition = inflater.inflateTransition(R.transition.fade)
    }


}