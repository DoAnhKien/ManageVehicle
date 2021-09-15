package com.example.vehiclessale.register

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.example.vehiclessale.*
import com.example.vehiclessale.MyUtils.Companion.isValidPhoneNumber
import com.example.vehiclessale.R
import com.example.vehiclessale.dialog.LoadingDialogFragment
import com.example.vehiclessale.dialog.NotifyDialogFragment
import com.example.vehiclessale.login.LoginFragment
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson
import java.util.HashMap


class RegisterFragment : BaseFragment() {

    @BindView(R.id.btnRegister)
    lateinit var btnRegister: AppCompatButton

    @BindView(R.id.edtName)
    lateinit var edtName: AppCompatEditText

    @BindView(R.id.edtSdt)
    lateinit var edtSdt: AppCompatEditText

    @BindView(R.id.edtEmail)
    lateinit var edtEmail: AppCompatEditText

    @BindView(R.id.edtAddress)
    lateinit var edtAddress: AppCompatEditText

    @BindView(R.id.edtPass)
    lateinit var edtPass: AppCompatEditText

    @BindView(R.id.edtConfirmPass)
    lateinit var edtConfirmPass: AppCompatEditText


    @BindView(R.id.radioSaler)
    lateinit var radioSaler: RadioButton

    @BindView(R.id.radioBuyer)
    lateinit var radioBuyer: RadioButton

    @BindView(R.id.imgBack)
    lateinit var imgBack: AppCompatImageView

    @BindView(R.id.layoutEmail)
    lateinit var layoutEmail: TextInputLayout

    @BindView(R.id.layoutName)
    lateinit var layoutName: TextInputLayout

    @BindView(R.id.layoutAddress)
    lateinit var layoutAddress: TextInputLayout

    @BindView(R.id.layoutSdt)
    lateinit var layoutSdt: TextInputLayout

    @BindView(R.id.layoutPass)
    lateinit var layoutPass: TextInputLayout

    @BindView(R.id.layoutConfirm)
    lateinit var layoutConfirm: TextInputLayout

    lateinit var unbinder: Unbinder
    private var showPass = false
    private var showPass1 = false


    override fun initUI() {
        (activity as MainActivity).apply {
            enableBottom(false)
            //notificationOrder()
        }
    }

    override fun initControl() {
        btnRegister.setOnClickListener {
            if (checkValidate(mapOf(REGISTER_NAME to edtName.text.toString(), REGISTER_EMAIL to edtEmail.text.toString(),
                            REGISTER_PHONE to edtSdt.text.toString(), REGISTER_ADDRESS to edtAddress.text.toString(),
                            REGISTER_PASSWORD to edtPass.text.toString(), REGISTER_CONFIRM to edtConfirmPass.text.toString()))) {
                showDialogLoading()
                actionRegister(RegisterData(
                        type = if (radioSaler.isChecked) MyEnum.SALER.Name() else MyEnum.BUYER.Name(),
                        name = edtName.text.toString(),
                        phone = edtSdt.text.toString(),
                        email = edtEmail.text.toString(),
                        pass = edtPass.text.toString(),
                        address = edtAddress.text.toString()
                )
                )
            }
        }
        imgBack.setOnClickListener {
            backToPrevious()
        }
        layoutPass.setEndIconOnClickListener {
            if (!showPass) {
                showPass = true
                edtPass.transformationMethod = HideReturnsTransformationMethod()
                edtPass.setSelection(edtPass.text.toString().length)
            } else {
                showPass = false
                edtPass.transformationMethod = PasswordTransformationMethod()
                edtPass.setSelection(edtPass.text.toString().length)
                //layoutPass.setEndIconDrawable(R.drawable.ic_baseline_remove_red_eye_24)
            }
        }
        layoutConfirm.setEndIconOnClickListener {
            if (!showPass1) {
                showPass1 = true
                edtConfirmPass.transformationMethod = HideReturnsTransformationMethod()
                edtConfirmPass.setSelection(edtConfirmPass.text.toString().length)
            } else {
                showPass1 = false
                edtConfirmPass.transformationMethod = PasswordTransformationMethod()
                edtConfirmPass.setSelection(edtConfirmPass.text.toString().length)
                //layoutPass.setEndIconDrawable(R.drawable.ic_baseline_remove_red_eye_24)
            }
        }
        edtPass.doOnTextChanged { text, start, before, count ->
            layoutPass.error = null
            layoutPass.boxStrokeColor = resources.getColor(R.color.transparent)
        }
        edtEmail.doOnTextChanged { text, start, before, count ->
            layoutEmail.error = null
            layoutEmail.boxStrokeColor = resources.getColor(R.color.transparent)
        }
        edtAddress.doOnTextChanged { text, start, before, count ->
            layoutAddress.error = null
            layoutAddress.boxStrokeColor = resources.getColor(R.color.transparent)
        }
        edtName.doOnTextChanged { text, start, before, count ->
            layoutName.error = null
            layoutName.boxStrokeColor = resources.getColor(R.color.transparent)
        }
        edtSdt.doOnTextChanged { text, start, before, count ->
            layoutSdt.error = null
            layoutSdt.boxStrokeColor = resources.getColor(R.color.transparent)
        }
        edtConfirmPass.doOnTextChanged { text, start, before, count ->
            layoutConfirm.error = null
            layoutConfirm.boxStrokeColor = resources.getColor(R.color.transparent)
        }
    }

    private var count = 0

    private val reference: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("users")
    }
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)
        unbinder = ButterKnife.bind(this, view)
        return view
    }

    private fun registerToFirebase(data: RegisterData) {
        var map = HashMap<String, String>()
        var id = FirebaseAuth.getInstance().currentUser?.uid
        map.put("id", id.toString())
        //map.put("type", data.type)
        map.put("name", data.name)
        map.put("phone", data.phone)
        map.put("email", data.email)
        map.put("address", data.address)
        map.put("pass", MyUtils.decodeBase64(data.pass).toString())
        reference.child(id.toString()).setValue(map)
    }

    private fun actionRegister(data: RegisterData) {
        firebaseAuth.createUserWithEmailAndPassword(data.email, data.pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        registerToFirebase(data)
                    }
                }.addOnCanceledListener {

                }.addOnFailureListener {

                }
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                UserPrefs.setUserTemp(requireContext(), Gson().toJson(data))
                hideDialogLoading()
                NotifyDialogFragment.newInstance(requireActivity().supportFragmentManager, type = NotifyDialogFragment.CHECK_INFO, content = getString(R.string.txt_RegisterSuccess),
                        onCancelCallback = {
                            backToPrevious()
                        }, onCallback = { backToPrevious() })
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()

            }

        })
    }


    private fun checkValidate(map: Map<String, String>): Boolean {
        var check = true
        map.forEach {
            val value = it.value
            when (it.key) {
                REGISTER_EMAIL -> if (value.isEmpty()) {
                    check = false
                    layoutEmail.error = getString(R.string.txt_field_required)
                } else if (!MyUtils.checkInvalidEmail(value)) {
                    check = false
                    layoutEmail.error = getString(R.string.txt_format_email)
                } else edtEmail.error = null

                REGISTER_NAME -> if (value.isEmpty()) {
                    check = false
                    layoutName.error = getString(R.string.txt_field_required)
                } else {
                    layoutPass.error = null
                }
                REGISTER_PHONE -> if (value.isEmpty()) {
                    check = false
                    layoutSdt.error = getString(R.string.txt_field_required)
                } else if (!edtSdt.text.toString().isValidPhoneNumber()) {
                    check = false
                    layoutSdt.error = getString(R.string.txt_format_phone)
                } else {
                    layoutSdt.error = null
                }
                REGISTER_ADDRESS -> if (value.isEmpty()) {
                    check = false
                    layoutAddress.error = getString(R.string.txt_field_required)
                } else {
                    layoutAddress.error = null
                }
                REGISTER_PASSWORD -> if (value.isEmpty()) {
                    check = false
                    layoutPass.error = getString(R.string.txt_field_required)
                } else if (!MyUtils.checkInvalidPass(edtPass.text.toString())) {
                    check = false
                    layoutPass.error = getString(R.string.txt_format_Pass)
                } else {
                    layoutPass.error = null
                }
                REGISTER_CONFIRM -> if (value.isEmpty()) {
                    check = false
                    layoutConfirm.error = getString(R.string.txt_field_required)
                } else if (edtPass.text.toString() != edtConfirmPass.text.toString()) {
                    check = false
                    layoutConfirm.error = getString(R.string.txt_compare_pass)
                } else if (!MyUtils.checkInvalidPass(edtPass.text.toString())) {
                    check = false
                    layoutConfirm.error = getString(R.string.txt_format_Pass)
                } else {
                    layoutConfirm.error = null
                }
            }
        }
        return check
    }

    companion object {
        const val REGISTER_NAME = "register name"
        const val REGISTER_PHONE = "register phone"
        const val REGISTER_EMAIL = "register email"
        const val REGISTER_ADDRESS = "register address"
        const val REGISTER_PASSWORD = "register pass"
        const val REGISTER_CONFIRM = "register confirm pass"
    }
}
