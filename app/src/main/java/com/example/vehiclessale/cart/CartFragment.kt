package com.example.vehiclessale.cart

import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
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
import com.example.vehiclessale.dialog.LoadingDialogFragment
import com.example.vehiclessale.dialog.NotifyDialogFragment
import com.example.vehiclessale.home.VehicleData
import com.example.vehiclessale.login.LoginData
import com.google.firebase.database.*
import com.google.gson.Gson

class CartFragment : BaseFragment() {


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

    lateinit var unbinder: Unbinder
    private val reference: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("cart")
    }
    private val refProduct: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("listProduct")
    }
    private var lstProduct: MutableList<CartData> = mutableListOf()

    private var cartAdapter = ItemProductAdapter()
    private var idCart = ""
    private var countProduct = 0
    private var subTotal: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)
        unbinder = ButterKnife.bind(this, view)
        showDialogLoading()
        return view
    }
    private fun actionTransition(){
        val inflater = TransitionInflater.from(requireContext())
        exitTransition = inflater.inflateTransition(R.transition.fade)
        //enterTransition = inflater.inflateTransition(R.transition.fade)
    }
    override fun initUI() {
        //actionTransition()
        (activity as MainActivity).apply {
            enableBottom(false)
            //notificationOrder()
        }
        var user = Gson().fromJson(UserPrefs.getLocalData(requireContext()), LoginData::class.java)
        user?.let {
            getListFromFirebase(user){ hideDialogLoading()}
            cartAdapter = ItemProductAdapter()
            rvProducts.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = cartAdapter
                setItemViewCacheSize(cartAdapter.list.size)
            }

            cartAdapter.apply {
                onDeleteCallback = { position, data ->
                    idCart = data.idCart
                    removeValue(user)
                    cartAdapter.removeItem(position)
                }
                onCheckBoxCallback = { data, isCheck ->
                    if (isCheck) {
                        subTotal += data.product.price.toDouble()
                        lstProduct.add(data)
                    } else {
                        subTotal -= data.product.price.toDouble()
                        lstProduct.remove(data)
                    }
                    tvSubTotal.text = MyUtils.formatPrice(subTotal) + " đ"
                }
                onItemCallback = {
                    findNavController().navigate(
                            CartFragmentDirections.actionCartFragmentToProductDetailFragment(
                                    Gson().toJson(it.product)
                            )
                    )
                }
            }
        } ?: kotlin.run {
            NotifyDialogFragment.newInstance(
                    requireActivity().supportFragmentManager,
                    type = NotifyDialogFragment.LOGIN,
                    content = getString(R.string.txt_please_login),
                    onCancelCallback = {
                        hideDialogLoading()
                    },
                    onCallback = {
                        findNavController().navigate(CartFragmentDirections.actionCartFragmentToLoginFragment())
                        hideDialogLoading()
                    })
        }
        tvTitleCenter.text = getString(R.string.txt_shopping_cart)
    }

    override fun initControl() {
        imgBack.setOnClickListener {
            backToPrevious()
        }
        btnPayment.setOnClickListener {
            if (lstProduct.isNotEmpty())
                findNavController().navigate(
                        CartFragmentDirections.actionCartFragmentToCheckOutFragment(
                                Gson().toJson(lstProduct),
                                subTotal.toString()
                        )
                )
            else {
                NotifyDialogFragment.newInstance(
                        requireActivity().supportFragmentManager,
                        type = NotifyDialogFragment.CHECK_INFO,
                        content = getString(R.string.txt_choose_product),
                        onCancelCallback = {
                            //dialog.dismiss()
                        },
                        onCallback = {
                            // dialog.dismiss()
                        })
            }
        }
    }

    private fun removeValue(user: LoginData) {
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {

                    var idUser = data.child("idUser").value.toString()
                    var product = data.child("product").getValue(VehicleData::class.java)

                    if (idUser == user.id) {
                        countProduct++
                        if (data.child("idCart").value.toString() == idCart) {
                            countProduct--
                            data.ref.removeValue()
                            product?.let {
                                updateProductRemoveCart(product)
                            }

                            cartAdapter.notifyDataSetChanged()
                        }
                    }
                }
                view?.post {
                    (activity as MainActivity).createBadge(countProduct)
                    countProduct = 0
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun getListFromFirebase(user: LoginData, onCallback: () -> Unit) {
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cartAdapter.list.clear()
                snapshot.children.filter { it.child("idUser").value.toString() == user.id }
                    ?.let { list ->
                        list.forEach {
                            var product = it.child("product").getValue(VehicleData::class.java)
                            var idUser = it.child("idUser").value.toString()
                            var idCart = it.child("idCart").value.toString()
                            product?.let {
                                cartAdapter.list.add(
                                    CartData(
                                        idCart = idCart,
                                        idUser = idUser,
                                        product
                                    )
                                )
                            }
                        }
                    }
                cartAdapter.notifyDataSetChanged()
                onCallback.invoke()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun updateProductRemoveCart(dataCart: VehicleData){
        refProduct.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    if(it.child("idProduct").value.toString() == dataCart.id && it.child("status").value.toString() != MyEnum.POST_NEW.Name()){
                        refProduct.child(dataCart.id).child("status").setValue(MyEnum.POST_NEW.Name())
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    override fun onStop() {
        lstProduct = mutableListOf()
        subTotal = 0.0
        super.onStop()
    }
}