package com.example.vehiclessale.cart

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.example.vehiclessale.*
import com.example.vehiclessale.R
import com.example.vehiclessale.dialog.NotifyDialogFragment
import com.example.vehiclessale.home.VehicleData
import com.example.vehiclessale.login.LoginData
import com.example.vehiclessale.notification.NotificationData
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import java.util.*


class CheckOutFragment : BaseFragment() {

    @BindView(R.id.tvDeliveryAddress)
    lateinit var tvDeliveryAddress: AppCompatTextView

    @BindView(R.id.tvTitleCenter)
    lateinit var tvTitleCenter: AppCompatTextView

    @BindView(R.id.imgBack)
    lateinit var imgBack: AppCompatImageView

    @BindView(R.id.rvProducts)
    lateinit var rvProducts: RecyclerView

    @BindView(R.id.btnPayment)
    lateinit var btnPayment: AppCompatButton

    @BindView(R.id.tvSubTotal)
    lateinit var tvSubTotal: AppCompatTextView

    @BindView(R.id.tvTitleSubTotal)
    lateinit var tvTitleSubTotal: AppCompatTextView

    lateinit var unbinder: Unbinder
    private var cartAdapter = ItemProductAdapter()
    private val args: CheckOutFragmentArgs by navArgs()
    private var lstProduct: MutableList<CartData> = mutableListOf()
    private val reference: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference
    }
    private val refOrder: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("orders")
    }
    private val refProduct: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("listProduct")
    }
    private var countProduct = 0
    private var count = 0

    override fun initUI() {
        btnPayment.text = getString(R.string.txt_place_order)
        tvTitleCenter.text = getString(R.string.txt_payment)
        var user = Gson().fromJson(UserPrefs.getLocalData(requireContext()), LoginData::class.java)
        user?.let {
            tvDeliveryAddress.text = user.address
            tvTitleSubTotal.text = getString(R.string.txt_total_payment)
            tvSubTotal.text = MyUtils.formatPrice(args.totalPrice.toDouble())
            if (args.lstProduct != "no") {
                lstProduct = Gson().fromJson(args.lstProduct, genericType<MutableList<CartData>>())
            }
            cartAdapter = ItemProductAdapter(lstProduct, 1)
            rvProducts.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = cartAdapter
                setItemViewCacheSize(lstProduct.size)
            }
        }
    }

    override fun initControl() {
        imgBack.setOnClickListener { backToPrevious() }
        var user = Gson().fromJson(
                UserPrefs.getLocalData(requireContext()),
                LoginData::class.java
        )
        btnPayment.setOnClickListener {
            user?.let {
                showDialogLoading()
                pushOrderToFirebase(it) { id, user ->
                    removeCartMain(id, user)
                }
            }
        }

    }

    private fun pushOrderToFirebase(user: LoginData, onCallback: (String, LoginData) -> Unit) {
        var id = reference.push().key.toString()
        var map = mapOf(
                "nameOrder" to user.name,
                "idUser" to user.id,
                "status" to MyEnum.CONFIRMATION.Name(),
                "idOrder" to id,
                "total" to args.totalPrice,
                "lstProducts" to Gson().fromJson(args.lstProduct, genericType<MutableList<CartData>>())
        )
        reference.child("orders").child(id).setValue(map)
        onCallback.invoke(id, user)
    }

    private fun removeCartMain(id: String, user: LoginData) {
        reference.child("orders").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val idOrder = it.child("idOrder").value.toString()
                    val nameOrder = it.child("nameOrder").value.toString()
                    val idUser = it.child("idUser").value.toString()
                    val total = it.child("total").value.toString()
                    val status = it.child("status").value.toString()
                    val generic: GenericTypeIndicator<MutableList<CartData>> =
                            object : GenericTypeIndicator<MutableList<CartData>>() {}
                    val lstProduct = it.child("lstProducts").getValue(generic)

                    if (idOrder == id) {
                        lstProduct?.forEach {
                            pushNotiToFirebase(PlaceOrderData(nameOrder, idUser, status, idOrder, total, it))
                        }
                        removeCart(
                                Gson().fromJson(
                                        args.lstProduct,
                                        genericType<MutableList<CartData>>()
                                ), user
                        ) {
                            hideDialogLoading()
                            NotifyDialogFragment.newInstance(
                                    requireActivity().supportFragmentManager,
                                    type = NotifyDialogFragment.CHECK_INFO,
                                    content = getString(R.string.txt_order_success)
                            ) {
                                backToPrevious()
                            }
                        }

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                hideDialogLoading()
            }

        })
    }

    private fun updateProductToConfirm(dataCart: VehicleData) {
        refProduct.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    if (it.child("idProduct").value.toString() == dataCart.id && it.child("status").value.toString() != MyEnum.CONFIRMATION.Name()) {
                        refProduct.child(dataCart.id).child("status").setValue(MyEnum.CONFIRMATION.Name())
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun pushNotiToFirebase(placeOrderData: PlaceOrderData) {
        var idKeyNoti = refOrder.push().key.toString()
        val content = getString(R.string.txt_new_order)
        var map = mapOf(
                "idNotify" to idKeyNoti,
                "content" to content,
                "idReceiver" to placeOrderData.product.product.createBy.id,
                "seen" to MyEnum.NO_SEEN.Name(),
                "placeOrderData" to placeOrderData
        )
        reference.child("notification").child(idKeyNoti).setValue(map)
    }

    private fun removeCart(lst: MutableList<CartData>, user: LoginData, onCallback: () -> Unit) {
        reference.child("cart").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { data ->
                    if (data.child("idUser").value.toString() == user.id) {
                        countProduct++
                        lst.forEach {
                            var product = data.child("product").getValue(VehicleData::class.java)
                            if (it.product.id == product?.id) {
                                data.ref.removeValue()
                                updateProductToConfirm(product)
                                countProduct--
                            }
                        }
                    }
                }
                view?.post {
                    (activity as MainActivity).createBadge(countProduct)
                    countProduct = 0
                }
                onCallback.invoke()
            }

            override fun onCancelled(error: DatabaseError) {
                hideDialogLoading()
            }

        })

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_check_out, container, false)
        unbinder = ButterKnife.bind(this, view)
        return view
    }


}