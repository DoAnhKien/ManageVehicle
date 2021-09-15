package com.example.vehiclessale.orders

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.example.vehiclessale.BaseFragment
import com.example.vehiclessale.MainActivity
import com.example.vehiclessale.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OrdersFragment : BaseFragment() {
    private lateinit var adapterViewPager: HistoryPagerAdapter
    @BindView(R.id.tvTitleCenter)
    lateinit var tvTitleCenter: AppCompatTextView

    @BindView(R.id.imgBack)
    lateinit var imgBack: AppCompatImageView

    @BindView(R.id.tab)
    lateinit var tab: TabLayout
    @BindView(R.id.viewpager)
    lateinit var viewpager: ViewPager2
    lateinit var unbinder: Unbinder

    override fun initUI() {
        tvTitleCenter.text = getString(R.string.txt_Order)
        (activity as MainActivity).apply {
            enableBottom(false)
            //notificationOrder()
        }
       // actionTransition()
        initTab()
    }

    override fun initControl() {
        imgBack.setOnClickListener { backToPrevious()}
    }
    private fun actionTransition(){
        val inflater = TransitionInflater.from(requireContext())
        //exitTransition = inflater.inflateTransition(R.transition.fade)
        enterTransition = inflater.inflateTransition(R.transition.fade)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_orders, container, false)
        unbinder = ButterKnife.bind(this, view)
        return view
    }

    private fun initTab(){
        adapterViewPager = HistoryPagerAdapter(fManager, lifecycle)
        viewpager.adapter = adapterViewPager
        TabLayoutMediator(
            tab,
            viewpager
        ) { tab, position ->
            tab.apply {
                text = if (position == 0)
                    getString(R.string.txt_to_ship)
                else
                    getString(R.string.txt_Delivering)
            }
        }.attach()
        viewpager.isUserInputEnabled = false
    }
}