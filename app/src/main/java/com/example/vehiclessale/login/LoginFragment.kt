package com.example.vehiclessale.login

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.doOnTextChanged
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.example.vehiclessale.*
import com.example.vehiclessale.MyUtils.Companion.toEditable
import com.example.vehiclessale.R
import com.example.vehiclessale.dialog.LoadingDialogFragment
import com.example.vehiclessale.dialog.NotifyDialogFragment
import com.example.vehiclessale.register.RegisterData
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson


class LoginFragment : BaseFragment(), View.OnClickListener {

    @BindView(R.id.tvRegister)
    lateinit var tvRegister: AppCompatTextView

    @BindView(R.id.btnLogin)
    lateinit var btnLogin: AppCompatButton

    @BindView(R.id.edtEmail)
    lateinit var edtEmail: AppCompatEditText

    @BindView(R.id.edtPass)
    lateinit var edtPass: AppCompatEditText

    @BindView(R.id.layoutPass)
    lateinit var layoutPass: TextInputLayout
    @BindView(R.id.layoutEmail)
    lateinit var layoutEmail: TextInputLayout


    lateinit var unbinder: Unbinder
    private var countProduct = 0
    private var countNotify = 0
    private var showPass = false

    private val reference: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("users")
    }
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val refCart: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("cart")
    }
    private val refNotification: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("notification")
    }

    override fun initUI() {

    }

    override fun initControl() {
        (activity as MainActivity).enableBottom(true)
        tvRegister.setOnClickListener(this)
        btnLogin.setOnClickListener(this)
        layoutPass.setEndIconOnClickListener {
            if(!showPass) {
                showPass = true
                edtPass.transformationMethod = HideReturnsTransformationMethod()
                edtPass.setSelection(edtPass.text.toString().length)
            }else{
                showPass = false
                edtPass.transformationMethod = PasswordTransformationMethod()
                edtPass.setSelection(edtPass.text.toString().length)
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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        unbinder = ButterKnife.bind(this, view)
        return view
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.tvRegister -> {
                Navigation.findNavController(p0)
                    .navigate(R.id.action_loginFragment_to_registerFragment2)
            }
            R.id.btnLogin -> {
                if(checkValidate(mapOf(LOGIN_EMAIL to edtEmail.text.toString(), LOGIN_PASS to edtPass.text.toString()))){
                    showDialogLoading()
                    (activity as MainActivity).createBadge(0)
                    handleLogin(edtEmail.text.toString(), edtPass.text.toString())
                }
            }
        }
    }

    private fun checkValidate(map: Map<String, String>): Boolean{
        var check = true
        map.forEach{
            val value = it.value.toString()
            when(it.key){
                LOGIN_EMAIL -> if(value.isEmpty()){
                    check = false
                    layoutEmail.error = getString(R.string.txt_field_required)
                } else  if(!MyUtils.checkInvalidEmail(value)){
                    check = false
                    layoutEmail.error = getString(R.string.txt_format_email)
                }
                else edtEmail.error = null

                LOGIN_PASS -> if(value.isEmpty()) {
                    check = false
                    layoutPass.error = getString(R.string.txt_field_required)
                } else if(!MyUtils.checkInvalidPass(edtPass.text.toString())){
                    check = false
                    layoutPass.error = getString(R.string.txt_format_Pass)
                }
                else {
                    layoutPass.error = null
                }
            }
        }
        return check
    }

    private fun handleLogin(email: String, pass: String) {
        firebaseAuth.signInWithEmailAndPassword(email, pass).addOnSuccessListener {
            it.user?.apply {
                var idUser = this.uid
                getUser(idUser)
                UserPrefs.clearPrefs(requireContext(), UserPrefs.USER_TEMP)
            }
        }.addOnFailureListener {
            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
            hideDialogLoading()
        }
    }

    private fun setNumberCart(user: LoginData) {
        refCart.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    var idUser = data.child("idUser").value.toString()
                    if (idUser == user.id) {
                        countProduct++
                    }
                }
                try {
                    (activity as MainActivity).createBadge(countProduct)
                    countProduct = 0
                } catch (e: Exception) {
                }

            }

            override fun onCancelled(error: DatabaseError) {}
        })
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
                } catch (e: Exception) {
                }

            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getUser(idUser: String) {
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.find { it.child("id").value.toString() == idUser }?.let {
                    UserPrefs.setLocalData(
                        requireContext(), Gson().toJson(
                            (LoginData(
                                id = idUser,
                                email = it.child("email").value.toString(),
                                name = it.child("name").value.toString(),
                                phone = it.child("phone").value.toString(),
                                address = it.child("address").value.toString()
                            ))
                        )
                    )
                }
                var user =
                    Gson().fromJson(UserPrefs.getLocalData(requireContext()), LoginData::class.java)
                user?.let {
                    setNumberCart(it)
                    setNumberNotify(it)
                }
                backToPrevious()
                hideDialogLoading()
            }

            override fun onCancelled(error: DatabaseError) {
                hideDialogLoading()
                Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
            }

        })
    }

    override fun onResume() {
        val userPref = UserPrefs.getUserTemp(requireContext())
        userPref?.let {
            val userTemp = Gson().fromJson(it, RegisterData::class.java)
            edtEmail.text = userTemp.email.toEditable()
            edtPass.text = userTemp.pass.toEditable()
        }
        super.onResume()
    }
    companion object{
        const val LOGIN_EMAIL = "login email"
        const val LOGIN_PASS = "login pass"
    }
}