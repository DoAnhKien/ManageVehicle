package com.example.vehiclessale.purchase

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.example.vehiclessale.BaseFragment
import com.example.vehiclessale.MyEnum
import com.example.vehiclessale.R
import com.example.vehiclessale.UserPrefs
import com.example.vehiclessale.cart.CartData
import com.example.vehiclessale.cart.ItemProductAdapter
import com.example.vehiclessale.login.LoginData
import com.google.firebase.database.*
import com.google.gson.Gson

class DeliveringFragment : BaseFragment() {
    @BindView(R.id.rvPurchaseDelivering)
    lateinit var rvPurchaseDelivering: RecyclerView

    lateinit var unbinder: Unbinder
    private lateinit var itemAdapter: ItemProductAdapter

    private val refProduct: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("listProduct")
    }
    private val reference: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("orders")
    }


    override fun initUI() {
        showDialogLoading()
        var user = Gson().fromJson(UserPrefs.getLocalData(requireContext()), LoginData::class.java)
        user?.let {user ->
            initRv(user)
            itemAdapter.onUpdateStatus = {
                showDialogLoading()

            }
        }
    }
    private fun initRv(user: LoginData) {
        getProducts(user){ Handler().postDelayed({ hideDialogLoading() }, 1000)}
        itemAdapter = ItemProductAdapter(type = 1)
        rvPurchaseDelivering.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemAdapter
        }
    }

    override fun initControl() {
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_delivering, container, false)
        unbinder = ButterKnife.bind(this, view)
        return view
    }

    private fun getProducts(user: LoginData, onCallback: () -> Unit) {
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itemAdapter.list.clear()
                for (data in snapshot.children) {
                    val genericTypeIndicator: GenericTypeIndicator<MutableList<CartData>> =
                        object : GenericTypeIndicator<MutableList<CartData>>() {}

                    val nameOrder = data.child("nameOrder").value.toString()
                    val idUser = data.child("idUser").value.toString()
                    val idOrder = data.child("idOrder").value.toString()
                    val status = data.child("status").value.toString()
                    val cartProduct = data.child("lstProducts").getValue(genericTypeIndicator)
                    cartProduct?.forEach {
                        if (idUser == user.id && it.product.status == MyEnum.DELIVERING.Name() && status == MyEnum.DELIVERING.Name()) {
                            val productCart = CartData(idCart = it.idCart, idUser = it.idUser, product = it.product)
                            itemAdapter.list.add(productCart)
                        }
                    }
                }
                itemAdapter.notifyDataSetChanged()
                onCallback.invoke()
            }

            override fun onCancelled(error: DatabaseError) {
                hideDialogLoading()
            }

        })
    }
}