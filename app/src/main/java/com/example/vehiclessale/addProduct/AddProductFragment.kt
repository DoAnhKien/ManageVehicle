package com.example.vehiclessale.addProduct

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
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
import com.example.vehiclessale.addProduct.Model.ProductRequestData
import com.example.vehiclessale.dialog.CaptureDialogFragment
import com.example.vehiclessale.dialog.LoadingDialogFragment
import com.example.vehiclessale.dialog.NotifyDialogFragment
import com.example.vehiclessale.login.LoginData
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_add_product.*
import java.io.ByteArrayOutputStream

class AddProductFragment : BaseFragment() {

    @BindView(R.id.tvTitleCenter)
    lateinit var tvTitleCenter: AppCompatTextView

    @BindView(R.id.rvImages)
    lateinit var rvImages: RecyclerView

    @BindView(R.id.imgBack)
    lateinit var imgBack: AppCompatImageView

    @BindView(R.id.btnSave)
    lateinit var btnSave: AppCompatButton

    @BindView(R.id.edtNameProduct)
    lateinit var edtNameProduct: AppCompatEditText

    @BindView(R.id.edtDesProduct)
    lateinit var edtDesProduct: AppCompatEditText

    @BindView(R.id.edtPriceProduct)
    lateinit var edtPriceProduct: AppCompatEditText

    @BindView(R.id.edtContactPhone)

    lateinit var edtContactPhone: AppCompatEditText

    @BindView(R.id.tvType)
    lateinit var tvType: AppCompatTextView

    @BindView(R.id.btnRadioGroup)
    lateinit var radioType: RadioGroup

    lateinit var unbinder: Unbinder
    private var lstImage: MutableList<ImageData> = mutableListOf()

    private var imgAdapter = ImageAdapter()
    private var posRemove = 0
    private var loading = false


    private val reference: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference
    }
    private val refStorage: StorageReference by lazy {
        FirebaseStorage.getInstance().reference
    }

    override fun initUI() {
        tvTitleCenter.text = getString(R.string.add_product)
        (activity as MainActivity).apply {
            enableBottom(false)
            //notificationOrder()
        }

        initRv()
    }

    companion object {
        private const val TAG = "KienDa"
    }

    override fun initControl() {
        imgAdapter.onAddCallback = {
            CaptureDialogFragment.newInstance(
                requireActivity().supportFragmentManager,
                onCaptureCallback = {
                    checkPermissionCamera()
                },
                onGalleryCallback = {
                    openFileSystemManager()
                })
        }

        imgAdapter.onRemoveCallback = { posi, url ->
            posRemove = posi
            deleteImage(url)
        }
        imgBack.setOnClickListener {
            findNavController().popBackStack()
        }

        radioType.setOnCheckedChangeListener { group, checkId ->
            when (checkId) {
                R.id.btnShowCar -> {
                    tvType.text = btnShowCar.text
                    Log.d(TAG, "initControl: $tvType ")
                }
                R.id.btnShowMotorbike -> {
                    tvType.text = btnShowMotorbike.text
                    Log.d(TAG, "initControl: $tvType ")
                }
                R.id.btnShowElectricBike -> {
                    tvType.text = btnShowElectricBike.text
                    Log.d(TAG, "initControl: $tvType ")
                }
            }

        }
        btnSave.setOnClickListener {
            showDialogLoading()
            var user =
                Gson().fromJson(UserPrefs.getLocalData(requireContext()), LoginData::class.java)
            if (imgAdapter.list.size == 1 || loading) {
                NotifyDialogFragment.newInstance(
                    requireActivity().supportFragmentManager,
                    type = NotifyDialogFragment.CHECK_INFO,
                    content = getString(R.string.txt_checkImgs)
                ) {
                    dialog.dismiss()
                }
            } else if (edtNameProduct.text?.isEmpty() == true ||
                edtDesProduct.text?.isEmpty() == true || edtPriceProduct.text?.isEmpty() == true ||
                edtContactPhone.text?.isEmpty() == true
            ) {
                NotifyDialogFragment.newInstance(
                    requireActivity().supportFragmentManager,
                    type = NotifyDialogFragment.CHECK_INFO,
                    content = getString(R.string.txt_fill)
                ) {
                    hideDialogLoading()
                }
            } else
                addProductToFirebase(
                    ProductRequestData(
                        status = MyEnum.POST_NEW.Name(),
                        list = listImage,
                        nameProduct = edtNameProduct.text.toString(),
                        des = edtDesProduct.text.toString(),
                        price = edtPriceProduct.text.toString(),
                        phone = edtContactPhone.text.toString(),
                        type = tvType.text.toString(),
                        createby = Owner(
                            user.id,
                            name = user.name,
                            phone = user.phone,
                            address = user.address,
                            email = user.email
                        )
                    )
                )
        }
    }


    private var listImage: MutableList<ImageData> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_add_product, container, false)
        unbinder = ButterKnife.bind(this, view)
        return view
    }

    private fun deleteImage(img: String) {
        val desertRef = FirebaseStorage.getInstance().getReferenceFromUrl(img)
        desertRef.delete().addOnSuccessListener {
            imgAdapter.removeItem(posRemove)
        }.addOnFailureListener {
        }
    }

    private fun addProductToFirebase(data: ProductRequestData) {
        val images = data.list
        val id = reference.push().key.toString()
        var map = mapOf(
            "status" to data.status,
            "idProduct" to id,
            "name product" to data.nameProduct,
            "description" to data.des,
            "price" to data.price.toString(),
            "images" to images,
            "contact" to data.phone,
            "create by" to data.createby,
            "type" to data.type
        )
        reference.child("listProduct").child(id).setValue(map)
        NotifyDialogFragment.newInstance(
            requireActivity().supportFragmentManager,
            type = NotifyDialogFragment.CHECK_INFO,
            content = getString(R.string.add_product_success)
        ) {
            backToPrevious()
        }
        hideDialogLoading()
    }

    private fun initRv() {
        lstImage.add(ImageData("", ""))
        imgAdapter = ImageAdapter(lstImage.toMutableList())
        rvImages.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = imgAdapter
        }
    }


    private fun saveImage(imgURI: Uri) {
        var count = 0
        val ref = refStorage.child(imgURI.lastPathSegment!!)
        ref.putFile(imgURI).addOnSuccessListener { task ->
            val url = task.storage.downloadUrl
            while (!url.isSuccessful);
            val downloadUrl: Uri? = url.result
            loading = false
            imgAdapter.removeItem(imgAdapter.list.size - 2)
            listImage.add(ImageData(MyUtils.getDate(), downloadUrl.toString()))
            imgAdapter.addImage(ImageData(MyUtils.getDate(), downloadUrl.toString()))
            imgAdapter.notifyDataSetChanged()

        }.addOnFailureListener {

        }.addOnProgressListener {
            loading = true
            count++
            if (count == 1) {
                imgAdapter.addImage(ImageData("Load", ""))
            }
        }.addOnCompleteListener {
        }
    }

    private fun checkPermissionCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requireActivity().checkSelfPermission(Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED ||
                requireActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED
            ) {
                val permission = arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                requestPermissions(permission, MyUtils.PERMISSION_CODE)
            } else {
                openCamera()
            }
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, MyUtils.IMAGE_CAPTURE_CODE)
        } catch (e: ActivityNotFoundException) {

        }
    }

    private fun openFileSystemManager() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        var mimetypes = emptyArray<String>()
        mimetypes = arrayOf("image/jpeg", "image/png")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
        startActivityForResult(intent, MyUtils.OPEN_FILE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            MyUtils.PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED && grantResults[1] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    openCamera()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            MyUtils.IMAGE_CAPTURE_CODE ->
                if (resultCode == Activity.RESULT_OK) {
                    data?.let {
                        val uriImage =
                            convertBitmapToURI(
                                requireContext(),
                                data.extras?.get("data") as Bitmap
                            )
                        saveImage(uriImage)
                    }
                }
            MyUtils.OPEN_FILE -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let {
                        saveImage(it)
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun convertBitmapToURI(context: Context, bitmap: Bitmap): Uri {
        var bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        var path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "", null)
        return Uri.parse(path)
    }


}