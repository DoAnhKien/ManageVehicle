package com.example.vehiclessale.profile

import android.transition.TransitionInflater
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.navigation.fragment.findNavController
import butterknife.BindView
import com.example.vehiclessale.BaseFragment
import com.example.vehiclessale.R
import com.example.vehiclessale.UserPrefs
import com.example.vehiclessale.base.BaseFragmentA
import com.example.vehiclessale.databinding.FragmentEditUserBinding
import com.example.vehiclessale.login.LoginData
import com.example.vehiclessale.model.User
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_header.*

class EditUserFragment : BaseFragmentA<FragmentEditUserBinding>(), View.OnClickListener {

    private val userDatabases = FirebaseDatabase.getInstance().getReference("users")
    private lateinit var user: LoginData

    override fun initLayout(): Int = R.layout.fragment_edit_user

    override fun init() {
        actionTransition()
        initData()
    }

    override fun setOnClickForViews() {
        binding?.imgBack?.setOnClickListener(this)
        binding?.btnCancelSaveInformation?.setOnClickListener(this)
        binding?.btnSaveInformation?.setOnClickListener(this)
    }

    override fun initViews() {

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                findNavController().navigate(R.id.action_editUserFragment_to_homeFragment)
            }
            R.id.btnCancelSaveInformation -> {
                findNavController().navigate(R.id.action_editUserFragment_to_homeFragment)
            }
            R.id.btnSaveInformation -> {
                saveInformationForUser()
            }
        }
    }

    private fun saveInformationForUser() {
        Log.d(TAG, "updateAdminInformation: 123")
        val userName = binding?.edtUserName?.text.toString()
        val userGmail = binding?.edtUserGmail?.text.toString()
        val userPhone = binding?.edtUserPhone?.text.toString()
        val userAddress = binding?.edtUerAddress?.text.toString()

        if (userName.isNotEmpty() && userGmail.isNotEmpty() && userPhone.isNotEmpty() && userAddress.isNotEmpty()) {
            val userNameHashMap: HashMap<String, String> = HashMap<String, String>()
            userNameHashMap["address"] = userAddress
            userNameHashMap["email"] = userGmail
            userNameHashMap["name"] = userName
            userNameHashMap["phone"] = userPhone
            userDatabases.child(user.id).updateChildren(userNameHashMap as Map<String, Any>)
                .addOnSuccessListener {
                    Log.d(TAG, "updateAdminInformation: succees")
                    UserPrefs.setLocalData(
                        requireContext(), Gson().toJson(
                            (LoginData(
                                id = user.id,
                                email = userGmail,
                                name = userName,
                                phone = userPhone,
                                address = userAddress
                            ))
                        )
                    )
                }.addOnFailureListener {
                    Log.d(TAG, "updateTheUserPackage: + ${it.message}")
                }
            findNavController().navigate(R.id.action_editUserFragment_to_homeFragment)
            return
        }
        Toast.makeText(requireContext(), "Bạn cần điền đầy đủ thông tin", Toast.LENGTH_SHORT)
            .show()


    }

    private fun initData() {
        user = Gson().fromJson(UserPrefs.getLocalData(requireContext()), LoginData::class.java)

        binding?.user = user
    }

    private fun actionTransition() {
        val inflater = TransitionInflater.from(requireContext())
        exitTransition = inflater.inflateTransition(R.transition.fade)
    }

    companion object {
        private const val TAG = "KienDA"
    }


}