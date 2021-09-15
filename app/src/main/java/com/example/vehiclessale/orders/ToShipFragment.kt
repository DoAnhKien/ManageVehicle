package com.example.vehiclessale.orders

import android.os.Bundle
import android.os.Handler
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.example.vehiclessale.*
import com.example.vehiclessale.R
import com.example.vehiclessale.addProduct.Model.ImageData
import com.example.vehiclessale.addProduct.Model.Owner
import com.example.vehiclessale.cart.CartData
import com.example.vehiclessale.cart.ItemProductAdapter
import com.example.vehiclessale.cart.PlaceOrderData
import com.example.vehiclessale.dialog.NotifyDialogFragment
import com.example.vehiclessale.home.ItemAdapter
import com.example.vehiclessale.home.VehicleData
import com.example.vehiclessale.login.LoginData
import com.google.firebase.database.*
import com.google.gson.Gson

class ToShipFragment : BaseFragment() {

    @BindView(R.id.rvDelivering)
    lateinit var rvDelivering: RecyclerView

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

    override fun initControl() {

    }

    private fun initRv(user: LoginData) {
        // showDialogLoading()
        getProducts(user){ Handler().postDelayed({ hideDialogLoading() }, 1000)}
        itemAdapter = ItemOrderAdapter(type = 2)
        rvDelivering.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemAdapter
        }
    }


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_to_ship, container, false)
        unbinder = ButterKnife.bind(this, view)
        return view
    }

    private fun updateOrder(order: PlaceOrderData, user: LoginData) {
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
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
                // hideDialogLoading()
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
                            val lst = lstProduct.filter { it.product.status != MyEnum.DELIVERING.Name() }
                            if (lst.isEmpty())
                                reference.child(order.idOrder).child("status").setValue(MyEnum.DELIVERING.Name())
                            hideDialogLoading()
                            NotifyDialogFragment.newInstance(
                                    requireActivity().supportFragmentManager,
                                    type = NotifyDialogFragment.CHECK_INFO,
                                    content = getString(R.string.txt_Product_delivering)
                            ) {
                                //backToPrevious()
                                getProducts(user = user){Handler().postDelayed({ hideDialogLoading() }, 1000)}
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

    private fun updateCartInOrder(listCart: MutableList<CartData>, order: PlaceOrderData, onCallBackNoti: () -> Unit) {
        var count = -1
        listCart.forEach {
            count++
            if (it.idCart == order.product.idCart) {
                refProduct.child(it.product.id).child("status").setValue(MyEnum.DELIVERING.Name())
                reference.child(order.idOrder).child("lstProducts").child("$count").child("product").child("status").setValue(MyEnum.DELIVERING.Name())
                //pushNotiToFirebase(order.idOrder, count.toString(), it){onCallBackNoti.invoke()}
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

    private fun getProducts(user: LoginData, onCallBack: () -> Unit) {
        showDialogLoading()
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
                        if (it.product.createBy.id == user.id && it.product.status == MyEnum.ADDED_CART.Name()) {
                            val productCart = PlaceOrderData(nameOrder, idUser, idOrder = idOrder, status = status, product = CartData(it.idCart, nameOrder, it.product))
                            itemAdapter.list.add(productCart)

                        }
                    }
                }
                itemAdapter.notifyDataSetChanged()
                onCallBack.invoke()
                // hideDialogLoading()
            }

            override fun onCancelled(error: DatabaseError) {
                // hideDialogLoading()
            }

        })
    }
}