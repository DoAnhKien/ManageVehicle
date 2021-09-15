package com.example.vehiclessale.orders

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.example.vehiclessale.cart.PlaceOrderData
import com.example.vehiclessale.dialog.NotifyDialogFragment
import com.example.vehiclessale.home.VehicleData
import com.example.vehiclessale.login.LoginData
import com.google.firebase.database.*
import com.google.gson.Gson


class CompleteFragment : BaseFragment() {

    @BindView(R.id.rvComplete)
    lateinit var rvComplete: RecyclerView

    lateinit var unbinder: Unbinder
    private lateinit var itemAdapter: ItemOrderAdapter

    private val reference: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("orders")
    }
    private val refProduct: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("listProduct")
    }

    private val refNotify: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("notification")
    }
    override fun initUI() {
        var user = Gson().fromJson(UserPrefs.getLocalData(requireContext()), LoginData::class.java)
        user?.let { user ->
            initRv(user)
            itemAdapter.onUpdateStatus = {
                showDialogLoading()
                updateOrder(it, user)
            }
        }
    }

    private fun updateOrder(order: PlaceOrderData, user: LoginData) {
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var count = -1
                snapshot.children.forEach {
                    if (it.child("idOrder").value.toString() == order.idOrder) {
                        val generic: GenericTypeIndicator<MutableList<CartData>> =
                                object : GenericTypeIndicator<MutableList<CartData>>() {}

                        val lstProduct = it.child("lstProducts").getValue(generic)
                        lstProduct?.let { lst ->
                            updateCartInOrder(lst, order) {
                                updateStatusOrder(order, user)
                            }
                        }

                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateStatusOrder(order: PlaceOrderData, user: LoginData) {
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    if (it.child("idOrder").value.toString() == order.idOrder) {
                        val generic: GenericTypeIndicator<MutableList<CartData>> =
                                object : GenericTypeIndicator<MutableList<CartData>>() {}

                        val lstProduct = it.child("lstProducts").getValue(generic)
                        lstProduct?.let { lst ->
                            val lst = lstProduct.filter { it.product.status != MyEnum.COMPLETE.Name() }
                            if (lst.isEmpty())
                                reference.child(order.idOrder).child("status").setValue(MyEnum.COMPLETE.Name())
                            hideDialogLoading()
                            NotifyDialogFragment.newInstance(
                                    requireActivity().supportFragmentManager,
                                    type = NotifyDialogFragment.CHECK_INFO,
                                    content = getString(R.string.txt_order_success_Complete), onCallback = {
                                getProducts(user = user)
                            }, onCancelCallback = {  }
                            )
                        }
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun updateCartInOrder(listCart: MutableList<CartData>, order: PlaceOrderData, onCallBackNoti: () -> Unit) {
        var count = -1
        listCart.forEach {
            count++
            if (it.idCart == order.product.idCart) {
                refProduct.child(it.product.id).child("status").setValue(MyEnum.COMPLETE.Name())
                reference.child(order.idOrder).child("lstProducts").child("$count").child("product").child("status").setValue(MyEnum.COMPLETE.Name())
                var idKeyNoti = reference.push().key.toString()
                var contentDelivering = getString(R.string.txt_notify_my_order, "delivering")
                var map = mapOf(
                        "idNotify" to idKeyNoti,
                        "content" to contentDelivering,
                        "idReceiver" to it.idUser,
                        "seen" to MyEnum.NO_SEEN.Name(),
                        "placeOrderData" to order
                )
                refNotify.child(idKeyNoti).setValue(map)
                onCallBackNoti.invoke()
            }
        }
    }

    private fun initRv(user: LoginData) {
        getProducts(user)
        itemAdapter = ItemOrderAdapter(type = 2)
        rvComplete.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemAdapter
        }
    }

    override fun initControl() {

    }

    private fun getProducts(user: LoginData) {
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
                        if (it.product.createBy.id == user.id && it.product.status == MyEnum.DELIVERING.Name()) {
                            val productCart = PlaceOrderData(nameOrder, idUser, idOrder = idOrder, status = status, product = CartData(it.idCart, nameOrder, it.product))
                            itemAdapter.list.add(productCart)

                        }
                    }
                }
                itemAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_complete, container, false)
        unbinder = ButterKnife.bind(this, view)
        return view
    }

}