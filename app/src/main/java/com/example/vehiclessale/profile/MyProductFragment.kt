package com.example.vehiclessale.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.bumptech.glide.Glide
import com.example.vehiclessale.BaseFragment
import com.example.vehiclessale.R
import com.example.vehiclessale.UserPrefs
import com.example.vehiclessale.addProduct.Model.ImageData
import com.example.vehiclessale.addProduct.Model.Owner
import com.example.vehiclessale.cart.CartData
import com.example.vehiclessale.cart.ItemProductAdapter
import com.example.vehiclessale.home.ItemAdapter
import com.example.vehiclessale.home.VehicleData
import com.example.vehiclessale.login.LoginData
import com.google.firebase.database.*
import com.google.gson.Gson

class MyProductFragment : BaseFragment() {

    @BindView(R.id.tvTitleCenter)
    lateinit var tvTitleCenter: AppCompatTextView

    @BindView(R.id.imgBack)
    lateinit var imgBack: AppCompatImageView

    @BindView(R.id.rvMyProduct)
    lateinit var rvMyProduct: RecyclerView

    lateinit var unbinder: Unbinder
    private lateinit var itemAdapter: ItemProductAdapter

    private val reference: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("listProduct")
    }

    override fun initUI() {
        tvTitleCenter.text = getString(R.string.txt_My_Product)
        itemAdapter = ItemProductAdapter(type = 3)
        rvMyProduct.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemAdapter
        }
        itemAdapter.onItemCallback = {
            findNavController().navigate(MyProductFragmentDirections.actionMyProductFragmentToProductDetailFragment(Gson().toJson(it.product)))
        }
        showDialogLoading()
        var user = Gson().fromJson(UserPrefs.getLocalData(requireContext()), LoginData::class.java)
        user?.let { getProducts(it) }
    }

    override fun initControl() {
        imgBack.setOnClickListener { backToPrevious() }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_my_product, container, false)
        unbinder = ButterKnife.bind(this, view)
        return view
    }

    private fun getProducts(user: LoginData) {
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    val genericTypeIndicator: GenericTypeIndicator<List<ImageData>> =
                            object : GenericTypeIndicator<List<ImageData>>() {}

                    val generic: GenericTypeIndicator<Owner> =
                            object : GenericTypeIndicator<Owner>() {}

                    var createBy = data.child("create by").getValue(generic)
                    createBy?.let {
                        it.takeIf { it.id == user.id }?.let {
                            var des = data.child("description").value.toString()
                            var id = data.child("idProduct").value.toString()
                            var name = data.child("name product").value.toString()
                            var price = data.child("price").value.toString()
                            var img = data.child("images").getValue(genericTypeIndicator)
                            var contact = data.child("contact").value.toString()
                            var status = data.child("status").value.toString()

                            img?.let {
                                itemAdapter.list.add(CartData("", "", VehicleData(status, id, img, name, des, price, contact, createBy)))
                            }
                        }
                    }
                }
                itemAdapter.notifyDataSetChanged()
                hideDialogLoading()
            }

            override fun onCancelled(error: DatabaseError) {
                hideDialogLoading()
            }

        })
    }
}