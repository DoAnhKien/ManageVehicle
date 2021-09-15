package com.example.vehiclessale.purchase

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.vehiclessale.orders.CompleteFragment
import com.example.vehiclessale.orders.ToShipFragment

class HistoryPurchaseAdapter(fm: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fm, lifecycle) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return if (position == 0)
            ToConfirmFragment()
        else if(position == 1)
            DeliveringFragment()
        else
            CompletedFragment()
    }
}