package com.example.vehiclessale.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.example.vehiclessale.MainActivity
import com.example.vehiclessale.R
import com.example.vehiclessale.UserPrefs
import com.example.vehiclessale.dialog.LoadingDialogFragment
import com.example.vehiclessale.dialog.NotifyDialogFragment
import com.example.vehiclessale.login.LoginData
import com.google.firebase.database.*
import com.google.gson.Gson
import java.util.*

class ListChatsFragment : Fragment() {

    @BindView(R.id.imgBack)
    lateinit var imgBack: AppCompatImageView

    @BindView(R.id.tvTitleCenter)
    lateinit var tvTitleCenter: AppCompatTextView

    @BindView(R.id.rvListChats)
    lateinit var rvListChats: RecyclerView

    private lateinit var unbinder: Unbinder
    private lateinit var chatsAdapter: ItemChatAdapter
    private val reference: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("chats")
    }
    private val user1: LoginData by lazy {
        Gson().fromJson(UserPrefs.getLocalData(requireContext()), LoginData::class.java)
    }
    private val args: ListChatsFragmentArgs by navArgs()
    private lateinit var dialog: LoadingDialogFragment

    private var sender = ""
    private var receiver = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list_chats, container, false)
        unbinder = ButterKnife.bind(this, view)
        (activity as MainActivity).apply {
            enableBottom(false)
            //notificationOrder()
        }
        tvTitleCenter.text = getString(R.string.txt_list_chat)
        imgBack.setOnClickListener {
            findNavController().popBackStack()
        }
        dialog = LoadingDialogFragment()
        dialog.show(requireActivity().supportFragmentManager, "")

        initRv()
        chatsAdapter.onClickCallback = { vehicle, mess ->
            findNavController().navigate(
                ListChatsFragmentDirections.actionListChatsFragmentToChatFragment(
                    createBy = Gson().toJson(
                        vehicle
                    ), message = Gson().toJson(mess)
                )
            )
        }
        return view
    }

    private fun initRv() {
        chatsAdapter = ItemChatAdapter()
        rvListChats.apply {
            val _layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            layoutManager = _layoutManager
            adapter = chatsAdapter
        }
    }

    private fun getListChat(user: LoginData) {
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                chatsAdapter.lstChat.clear()
                for (data in dataSnapshot.children) {
                    val generic: GenericTypeIndicator<Date> =
                        object : GenericTypeIndicator<Date>() {}

                    val idProduct = data.child("idProduct").value.toString()
                    val idSender = data.child("idSender").value.toString()
                    val idReceiver = data.child("idReceiver").value.toString()
                    val nameReceiver = data.child("name receiver").value.toString()
                    val nameSender = data.child("name sender").value.toString()
                    val time = data.child("time").getValue(generic)
                    val content = data.child("content").value.toString()
                    time?.let {

                        if(idSender != user.id && idReceiver == user.id){
                            val lst = chatsAdapter.lstChat.filter { it.idSender == idSender && it.idProduct == idProduct }
                            if(lst.isEmpty()){
                                chatsAdapter.lstChat.add(
                                    MessData(
                                        idProduct,
                                        idSender,
                                        nameSender,
                                        idReceiver,
                                        nameReceiver,
                                        content,
                                        time
                                    )
                                )
                            }
                        }

                    }
                }
                chatsAdapter.notifyDataSetChanged()
                rvListChats.smoothScrollToPosition(chatsAdapter.lstChat.size)
                dialog.dismiss()
            }


            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onResume() {
        sender = ""
        var user = Gson().fromJson(UserPrefs.getLocalData(requireContext()), LoginData::class.java)
        user?.let {
            getListChat(user)
        } ?: kotlin.run {
            NotifyDialogFragment.newInstance(requireActivity().supportFragmentManager,
                type = NotifyDialogFragment.LOGIN,
                content = getString(
                    R.string.txt_please_login
                ),
                onCancelCallback = {
                    dialog.dismiss()
                },
                onCallback = {
                    findNavController().navigate(R.id.action_listChatsFragment_to_loginFragment)
                    dialog.dismiss()
                })
        }

        super.onResume()

    }

    override fun onStop() {
        chatsAdapter.lstChat.clear()
        sender = ""
        super.onStop()
    }

}