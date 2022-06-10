package com.example.vehiclessale.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.bumptech.glide.Glide
import com.example.vehiclessale.*
import com.example.vehiclessale.R
import com.example.vehiclessale.home.VehicleData
import com.example.vehiclessale.login.LoginData
import com.google.firebase.database.*
import com.google.gson.Gson
import java.util.*


class ChatFragment : Fragment() {

    @BindView(R.id.imgBack)
    lateinit var imgBack: AppCompatImageView

    @BindView(R.id.tvTitleBack)
    lateinit var tvTitleBack: AppCompatTextView

    @BindView(R.id.tvTitleCenter)
    lateinit var tvTitleCenter: AppCompatTextView

    @BindView(R.id.rvMess)
    lateinit var rvMess: RecyclerView

    @BindView(R.id.imgSend)
    lateinit var imgSend: AppCompatImageView

    @BindView(R.id.edtTypeMess)
    lateinit var edtTypeMess: AppCompatEditText

    @BindView(R.id.img)
    lateinit var img: AppCompatImageView

    @BindView(R.id.tvDes)
    lateinit var tvDes: AppCompatTextView

    @BindView(R.id.tvPrice)
    lateinit var tvPrice: AppCompatTextView

    @BindView(R.id.cardview)
    lateinit var cardview: CardView

    private val args: ChatFragmentArgs by navArgs()


    private lateinit var unbinder: Unbinder

    private val reference: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("chats")
    }
    private val user: LoginData by lazy {
        Gson().fromJson(UserPrefs.getLocalData(requireContext()), LoginData::class.java)
    }
    private lateinit var messageAdapter: MessageAdapter

    private var checkToScroll = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        unbinder = ButterKnife.bind(this, view)
        tvTitleCenter.isVisible = false
        tvTitleBack.isVisible = true
        (activity as MainActivity).apply {
            enableBottom(false)
            //notificationOrder()
        }

        var data = Gson().fromJson(args.createBy, VehicleData::class.java)
        var user = Gson().fromJson(UserPrefs.getLocalData(requireContext()), LoginData::class.java)

        data?.let {
            tvTitleBack.text = data.createBy.name
            Glide.with(view).load(data.imgs[0].urlImg).into(img)
            tvDes.text = data.des
            tvPrice.text = MyUtils.formatPrice(data.price.toDouble()) + "$"

        }

        var mess = MessData()
        if (args.message != "no") {
            mess = Gson().fromJson(args.message, MessData::class.java)
            tvTitleBack.text = mess.nameSender
        }

        imgBack.setOnClickListener {
            findNavController().popBackStack()
        }
        imgSend.setOnClickListener {
            senMess(mess, data)
            edtTypeMess.text?.clear()
        }
        cardview.setOnClickListener {
            findNavController().navigate(
                ChatFragmentDirections.actionChatFragmentToProductDetailFragment(
                    args.createBy
                )
            )
        }
        initRv(data, user)
        return view
    }

    private fun senMess(mess: MessData, data: VehicleData) {
        reference.addValueEventListener(object : ValueEventListener {
            var date = Date()
            var txt = edtTypeMess.text.toString()
            override fun onDataChange(snapshot: DataSnapshot) {
                if (args.message != "no") {
                    var map = mapOf(
                        "idSender" to user.id,
                        "name sender" to user.name,
                        "idReceiver" to mess.idSender,
                        "name receiver" to mess.nameSender,
                        "content" to txt,
                        "idProduct" to data.id,
                        "time" to date
                    )
                    reference.child(date.toString()).setValue(map)
                } else {
                    var map = mapOf(
                        "idSender" to user.id,
                        "name sender" to user.name,
                        "idReceiver" to data.createBy.id,
                        "name receiver" to data.createBy.name,
                        "content" to txt,
                        "idProduct" to data.id,
                        "time" to date
                    )
                    reference.child(date.toString()).setValue(map)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun initRv(data: VehicleData, user: LoginData) {
        checkToScroll = true
        getList(data, user)
        messageAdapter = MessageAdapter(user = user)
        rvMess.apply {
            val _layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            _layoutManager.stackFromEnd = true
            layoutManager = _layoutManager
            adapter = messageAdapter
        }
    }

    private fun getList(owner: VehicleData, user: LoginData) {
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                messageAdapter.list.clear()
                for (data in dataSnapshot.children) {
                    val generic: GenericTypeIndicator<Date> =
                        object : GenericTypeIndicator<Date>() {}

                    val idSender = data.child("idSender").value.toString()
                    val idReceiver = data.child("idReceiver").value.toString()
                    val nameReceiver = data.child("name receiver").value.toString()
                    val nameSender = data.child("name sender").value.toString()
                    val time = data.child("time").getValue(generic)
                    val content = data.child("content").value.toString()
                    val idProduct = data.child("idProduct").value.toString()
                    time?.let {
                        if ((idReceiver == user.id || idSender == user.id)
                            && (idReceiver == owner.createBy.id || idSender == owner.createBy.id)
                            && owner.id == idProduct
                        ) {
                            messageAdapter.addItem(
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
                messageAdapter.notifyDataSetChanged()
                //  if(checkToScroll){
                //checkToScroll = false
                rvMess.smoothScrollToPosition(messageAdapter.list.size)
                // }
            }


            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }


}