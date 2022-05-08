package com.example.vehiclessale.productDetail

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.example.vehiclessale.*
import com.example.vehiclessale.R
import com.example.vehiclessale.dialog.NotifyDialogFragment
import com.example.vehiclessale.home.VehicleData
import com.example.vehiclessale.login.LoginData
import com.example.vehiclessale.utils.Const
import com.google.android.material.animation.AnimationUtils
import com.google.firebase.database.*
import com.google.gson.Gson
import com.smarteist.autoimageslider.SliderView

import android.net.Uri
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.navigation.NavController


class ProductDetailFragment : BaseFragment(), OnBottomSheetClick {

    @BindView(R.id.tvTitle)
    lateinit var tvTitle: AppCompatTextView

    @BindView(R.id.tvDes)
    lateinit var tvDes: AppCompatTextView

    @BindView(R.id.tvPrice)
    lateinit var tvPrice: AppCompatTextView

    @BindView(R.id.tvNameSeller)
    lateinit var tvNameSeller: AppCompatTextView

    @BindView(R.id.tvAddress)
    lateinit var tvAddress: AppCompatTextView

    @BindView(R.id.tvEmail)
    lateinit var tvEmail: AppCompatTextView

    @BindView(R.id.tvPhone)
    lateinit var tvPhone: AppCompatTextView

    @BindView(R.id.imageSlider)
    lateinit var imageSlider: SliderView

    @BindView(R.id.imgChat)
    lateinit var imgChat: AppCompatImageView

    @BindView(R.id.imgPhone)
    lateinit var imgPhone: AppCompatImageView

    @BindView(R.id.btnBuyNow)
    lateinit var btnBuyNow: AppCompatButton

    @BindView(R.id.imgBack)
    lateinit var imgBack: AppCompatImageView

    @BindView(R.id.tvTitleCenter)
    lateinit var tvTitleCenter: AppCompatTextView

    @BindView(R.id.layout_notify)
    lateinit var layout_notify: ConstraintLayout

    @BindView(R.id.imgArrow)
    lateinit var imgArrow: AppCompatImageView

    @BindView(R.id.viewContact)
    lateinit var viewContact: View

    private val args: ProductDetailFragmentArgs by navArgs()
    private lateinit var unbinder: Unbinder
    private lateinit var bottomSheet: BottomSheetChoosePhoneCall

    private val reference: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("cart")
    }
    private val refProduct: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("listProduct")
    }

    private var count = 0
    private var countProduct = 0

    private var check = false
    private var isExpandView = true
    private var countId = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_product_detail, container, false)
        unbinder = ButterKnife.bind(this, view)
        return view
    }


    private fun actionAddCart(user: LoginData, product: VehicleData) {
        check = true
        countProduct = 0

        var idCart = reference.push().key.toString()
        var data = mapOf(
            "idCart" to idCart,
            "idUser" to user.id,
            "product" to product
        )
        reference.child(idCart).setValue(data)

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    var idUser = data.child("idUser").value.toString()
                    if (idUser == user.id) {
                        updateProduct(product) {
                            countProduct++
                            view?.post {
                                (activity as MainActivity).createBadge(countProduct)
                                NotifyDialogFragment.newInstance(
                                    requireActivity().supportFragmentManager,
                                    type = NotifyDialogFragment.CHECK_INFO,
                                    content = getString(R.string.txt_add_cart), onCallback = {
//                                        dialog.dismiss()
                                        findNavControllerSafely()?.navigate(R.id.homeFragment)
                                        count = 0
                                        check = false
                                        countProduct = 0
                                        countId = 0
                                    }, onCancelCallback = {

                                    })
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun actionCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = "tel: $phoneNumber".toUri()
        startActivity(intent)
    }

    override fun initUI() {
        ViewAnimationUtils.expand(layout_notify)
        changeRotate(imgArrow, 0f, 180f).start()
        (activity as MainActivity).apply {
            enableBottom(false)
            //notificationOrder()
        }
        val data = Gson().fromJson(args.data, VehicleData::class.java)
        var user = Gson().fromJson(UserPrefs.getLocalData(requireContext()), LoginData::class.java)
        user?.let {
            if (data.createBy.id == user.id) {
                imgPhone.isVisible = false
                imgChat.isVisible = false
                btnBuyNow.visibility = View.INVISIBLE
            } else {
                imgPhone.isVisible = true
                imgChat.isVisible = true
                btnBuyNow.isVisible = true
            }
        } ?: kotlin.run {
            imgPhone.isVisible = false
            imgChat.isVisible = false
            btnBuyNow.visibility = View.INVISIBLE
        }
        tvTitle.text = data.title
        tvDes.text = data.des
        tvPrice.text = MyUtils.formatPrice(data.price.toDouble()) + " đ"
        tvNameSeller.text = "Seller: ${data.createBy.name}"
        tvAddress.text = "Address: ${data.createBy.address}"
        imageSlider.indicatorSelectedColor = Color.WHITE
        imageSlider.indicatorUnselectedColor = Color.GRAY
        imageSlider.isAutoCycle = false
        imageSlider.setSliderAdapter(SliderAdapter(data.imgs))
        initText(data)
    }

    private fun initText(data: VehicleData) {
        tvTitleCenter.text = getString(R.string.txt_product_detail)
        tvEmail.text = "Email: ${data.createBy.email}"
        tvPhone.text = "Phone: ${data.createBy.phone}"
        tvAddress.text = "Address: ${data.createBy.address}"

    }

    override fun initControl() {
        val data = Gson().fromJson(args.data, VehicleData::class.java)
        var user = Gson().fromJson(UserPrefs.getLocalData(requireContext()), LoginData::class.java)
        imgPhone.setOnClickListener {
            showPayOffBottomSheet(data.createBy.phone)
            Log.d(TAG, "initControl: ")
        }
        imgChat.setOnClickListener {
            findNavController().navigate(
                ProductDetailFragmentDirections.actionProductDetailFragmentToChatFragment(
                    Gson().toJson(data)
                )
            )
        }
        imgBack.setOnClickListener {
            backToPrevious()
        }
        btnBuyNow.setOnClickListener {
            val dataCart = VehicleData(
                status = MyEnum.ADDED_CART.Name(),
                id = data.id, imgs = data.imgs,
                title = data.title, des = data.des,
                price = data.price, phone = data.phone,
                createBy = data.createBy
            )
            actionAddCart(user, dataCart)
        }
        viewContact.setOnClickListener {
            if (isExpandView) {
                ViewAnimationUtils.collapse(layout_notify)
                changeRotate(imgArrow, 180f, 0f).start()
            } else {
                ViewAnimationUtils.expand(layout_notify)
                changeRotate(imgArrow, 0f, 180f).start()
            }
            isExpandView = !isExpandView
        }
    }

    private fun makeThePhoneCallToZalo(phoneNumber: String?) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW, Uri.parse(
                    "https://zalo.me/$phoneNumber"
                )
            )
        )
    }

    private fun showPayOffBottomSheet(phoneNumber: String?) {
        bottomSheet = BottomSheetChoosePhoneCall()
        bottomSheet.show(activity?.supportFragmentManager!!, "MyBottomSheetA")
        bottomSheet.setUserPhoneNumber(phoneNumber)
        bottomSheet.setOnClick(this)
    }

    @SuppressLint("RestrictedApi")
    private fun changeRotate(button: AppCompatImageView?, from: Float, to: Float): ObjectAnimator {
        val animator = ObjectAnimator.ofFloat(button, "rotation", from, to)
        animator.duration = 300
        animator.interpolator = AnimationUtils.LINEAR_INTERPOLATOR
        return animator
    }

    private fun updateProduct(dataCart: VehicleData, onCallback: () -> Unit) {
        refProduct.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    if (it.child("idProduct").value.toString() == dataCart.id) {
                        countId++
                        if (countId == 1) {
                            refProduct.child(dataCart.id).child("status").setValue(dataCart.status)
                            onCallback.invoke()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            100 -> {
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onBottomSheetClick(option: String, phoneNumber: String?) {
        Log.d(TAG, "onClick: $phoneNumber")
        when (option) {
            Const.FRAGMENT_EDIT_USER_MAKE_PHONE_CALL_BY_ZALO -> {
                makeThePhoneCallToZalo(phoneNumber)
                bottomSheet.dismissDialog()
            }
            Const.FRAGMENT_EDIT_USER_MAKE_PHONE_CALL -> {
                makePhoneCall(phoneNumber)
                bottomSheet.dismissDialog()
            }
            Const.FRAGMENT_EDIT_USER_SEND_MESSAGE -> {
                sendMessageToUser(phoneNumber)
                bottomSheet.dismissDialog()
            }
        }
    }

    private fun sendMessageToUser(phoneNumber: String?) {
        val sendIntent = Intent(Intent.ACTION_VIEW)
        sendIntent.data = Uri.parse("sms:$phoneNumber")
        startActivity(sendIntent);
    }

    private fun makePhoneCall(phoneNumber: String?) {
        if (phoneNumber != null) {
            if (MyUtils.hasPermission(requireContext(), android.Manifest.permission.CALL_PHONE)) {
                actionCall(phoneNumber!!)
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(android.Manifest.permission.CALL_PHONE), 100
                )
            }
        }

    }

    companion object {
        private const val TAG = "KienDa"
    }


    fun Fragment.findNavControllerSafely(): NavController? {
        return if (isAdded) {
            findNavController()
        } else {
            null
        }
    }
}