package com.example.vehiclessale.notification

import android.os.Bundle
import android.os.Handler
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
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
import com.example.vehiclessale.cart.CartFragmentDirections
import com.example.vehiclessale.cart.ItemProductAdapter
import com.example.vehiclessale.cart.PlaceOrderData
import com.example.vehiclessale.dialog.NotifyDialogFragment
import com.example.vehiclessale.home.VehicleData
import com.example.vehiclessale.login.LoginData
import com.google.firebase.database.*
import com.google.gson.Gson


class NotificationFragment : BaseFragment() {

    @BindView(R.id.tvTitleCenter)
    lateinit var tvTitleCenter: AppCompatTextView

    @BindView(R.id.imgBack)
    lateinit var imgBack: AppCompatImageView

    @BindView(R.id.rvNotify)
    lateinit var rvNotify: RecyclerView
    lateinit var unbinder: Unbinder
    private lateinit var itemAdapter: ItemProductAdapter

    private val reference: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("notification")
    }

    private val refNotification: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("notification")
    }

    private var countNotify = 0

    override fun initUI() {
        actionTransition()
        (activity as MainActivity).apply {
            enableBottom(true)
        }

        tvTitleCenter.text = getString(R.string.txt_notify)
        imgBack.visibility = View.INVISIBLE
        itemAdapter = ItemProductAdapter(type = 5)
        rvNotify.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemAdapter
        }
        var user = Gson().fromJson(UserPrefs.getLocalData(requireContext()), LoginData::class.java)
        user?.let {
            getNotify(it){Handler().postDelayed({ hideDialogLoading() }, 1000)}
            setNumberNotify(it)
        }?: kotlin.run {
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
    }

    private fun actionTransition(){
        val inflater = TransitionInflater.from(requireContext())
        exitTransition = inflater.inflateTransition(R.transition.fade)
        //enterTransition = inflater.inflateTransition(R.transition.fade)
    }

    private fun setNumberNotify(user: LoginData) {
        refNotification.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    var idReceiver = data.child("idReceiver").value.toString()
                    var status = data.child("seen").value.toString()
                    if (idReceiver == user.id && status == MyEnum.NO_SEEN.Name()) {
                        countNotify++
                    }
                }
                try {
                    (activity as MainActivity).createBadgeNotification(countNotify)
                    countNotify = 0
                }catch (e: Exception){}

            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun initControl() {

        itemAdapter.onItemCallback = {
            updateStatus(it.idCart)
            findNavController().navigate(R.id.action_notificationFragment_to_ordersFragment)
        }
    }

    private fun updateStatus(_idNotify: String){
        reference.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val idNotify = it.child("idNotify").value.toString()
                    if (it.child("seen").value.toString() == MyEnum.NO_SEEN.Name() && (idNotify == _idNotify)) {
                        reference.child(idNotify).child("seen").setValue(MyEnum.SEEN.Name())
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun getNotify(user: LoginData, onCallback: () -> Unit) {
        showDialogLoading()
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    val genericTypeIndicator: GenericTypeIndicator<List<ImageData>> =
                        object : GenericTypeIndicator<List<ImageData>>() {}

                    val generic: GenericTypeIndicator<PlaceOrderData> =
                        object : GenericTypeIndicator<PlaceOrderData>() {}

                    val content = data.child("content").value.toString()
                    val idNotify = data.child("idNotify").value.toString()
                    val idReceiver = data.child("idReceiver").value.toString()
                    val placeOrder = data.child("placeOrderData").getValue(generic)
                    val seen = data.child("seen").value.toString()

                    placeOrder?.let {
                        if(idReceiver == user.id && seen == MyEnum.NO_SEEN.Name()){
                            var cartData = CartData(idCart = idNotify, idUser = it.idUserBuyer, product = it.product.product)
                            itemAdapter.list.add(cartData)
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)
        unbinder = ButterKnife.bind(this, view)
        return view
    }

}