package com.example.vehiclessale.profile

import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.example.vehiclessale.BaseFragment
import com.example.vehiclessale.MainActivity
import com.example.vehiclessale.R
import com.example.vehiclessale.UserPrefs
import com.example.vehiclessale.login.LoginData
import com.google.gson.Gson

class ProfileFragment : BaseFragment(), View.OnClickListener {

    lateinit var unbinder: Unbinder

    @BindView(R.id.tvTitleCenter)
    lateinit var tvTitleCenter: AppCompatTextView

    @BindView(R.id.imgBack)
    lateinit var imgBack: AppCompatImageView

    @BindView(R.id.tvName)
    lateinit var tvName: AppCompatTextView

    @BindView(R.id.tvEmail)
    lateinit var tvEmail: AppCompatTextView

    @BindView(R.id.tvSdt)
    lateinit var tvSdt: AppCompatTextView

    @BindView(R.id.tvAddress)
    lateinit var tvAddress: AppCompatTextView

    @BindView(R.id.rvFunctions)
    lateinit var rvFunctions: RecyclerView

    @BindView(R.id.btnOut)
    lateinit var btnOut: AppCompatButton

    @BindView(R.id.btnEditUser)
    lateinit var btnEditUserInformation: AppCompatButton

    private lateinit var _adapter: FunctionAdapter

    override fun initUI() {
        (activity as MainActivity).apply {
            enableBottom(true)
            notificationOrder()
        }
        imgBack.visibility = View.INVISIBLE
        var user = Gson().fromJson(UserPrefs.getLocalData(requireContext()), LoginData::class.java)
        user?.let {
            tvName.text = user.name
            tvEmail.text = user.email
            tvSdt.text = user.phone
            tvAddress.text = user.address
            tvTitleCenter.text = user.name
        }
        actionTransition()
        _adapter = FunctionAdapter((prepareData()))
        rvFunctions.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = _adapter
        }
        _adapter.onItemCallBack = {
            when (it.nameFunction) {
                getString(R.string.add_product) -> {
                    findNavController().navigate(R.id.action_profileFragment2_to_addProductFragment)
                }
                getString(R.string.txt_My_Product) -> {
                    findNavController().navigate(R.id.action_profileFragment2_to_myProductFragment)
                }
                getString(R.string.txt_Order) -> {
                    findNavController().navigate(R.id.action_profileFragment2_to_ordersFragment)
                }
                getString(R.string.txt_My_Purchase) -> {
                    findNavController().navigate(R.id.action_profileFragment2_to_purchaseFragment)
                }
            }
        }
    }

    private fun prepareData(): MutableList<FunctionData> {
        val listIcon = mutableListOf<Int>(
            R.drawable.ic_plus,
            R.drawable.ic_box,
            R.drawable.ic_clipboard__1,
            R.drawable.ic_box
        )
        val name = mutableListOf(
            getString(R.string.add_product),
            getString(R.string.txt_Order),
            getString(R.string.txt_My_Purchase),
            getString(R.string.txt_My_Product)
        )
        val lst = mutableListOf<FunctionData>()
        for (i in listIcon.indices)
            lst.add(FunctionData(name[i], listIcon[i]))
        return lst
    }

    override fun initControl() {
        btnOut.setOnClickListener(this)
        btnEditUserInformation.setOnClickListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        unbinder = ButterKnife.bind(this, view)

        return view
    }

    private fun actionTransition() {
        val inflater = TransitionInflater.from(requireContext())
        exitTransition = inflater.inflateTransition(R.transition.fade)
        //enterTransition = inflater.inflateTransition(R.transition.fade)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnOut -> {
                UserPrefs.clearData(requireContext())
                // (activity as MainActivity).hideItem("")
                findNavController().navigate(R.id.action_profileFragment2_to_loginFragment)
            }
            R.id.btnEditUser -> {
                findNavController().navigate(R.id.action_profileFragment2_to_editUserFragment)
            }
        }
    }


}