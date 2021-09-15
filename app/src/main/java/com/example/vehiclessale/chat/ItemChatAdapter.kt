package com.example.vehiclessale.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.example.vehiclessale.R
import com.example.vehiclessale.addProduct.Model.ImageData
import com.example.vehiclessale.addProduct.Model.Owner
import com.example.vehiclessale.home.VehicleData
import com.google.firebase.database.*

class ItemChatAdapter(var lstChat: MutableList<MessData> = mutableListOf()): RecyclerView.Adapter<ItemChatAdapter.ViewHolder>() {

    private val reference: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("listProduct")
    }

    var onClickCallback: (VehicleData, MessData) -> Unit = {_,_ ->}
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemChatAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_item_chat, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ItemChatAdapter.ViewHolder, position: Int) {
        holder.bind(lstChat[position])
    }

    override fun getItemCount(): Int {
        return lstChat.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        @BindView(R.id.tvNameSender)
        lateinit var tvNameSender: AppCompatTextView
        @BindView(R.id.tvNameProduct)
        lateinit var tvNameProduct: AppCompatTextView
        @BindView(R.id.tvDes)
        lateinit var tvDes: AppCompatTextView
        @BindView(R.id.imgProduct)
        lateinit var imgProduct: AppCompatImageView
        init {
            ButterKnife.bind(this, itemView)
        }
        fun  bind(data: MessData){
            tvNameSender.text = data.nameSender
            var vehicle = VehicleData()
            reference.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.find { it.child("idProduct").value.toString() == data.idProduct }?.let {
                        val genericTypeIndicator: GenericTypeIndicator<List<ImageData>> =
                                object : GenericTypeIndicator<List<ImageData>>() {}

                        val generic: GenericTypeIndicator<Owner> =
                                object : GenericTypeIndicator<Owner>() {}

                        var status = it.child("status").value.toString()
                        var des = it.child("description").value.toString()
                        var id = it.child("idProduct").value.toString()
                        var name = it.child("name product").value.toString()
                        var price = it.child("price").value.toString()
                        var img = it.child("images").getValue(genericTypeIndicator)
                        var contact = it.child("contact").value.toString()
                        var createBy = it.child("create by").getValue(generic)

                        img?.let {
                            createBy?.let {
                                Glide.with(itemView).load(img[0].urlImg).into(imgProduct)
                                tvDes.text = des
                                tvNameProduct.text = name
                                vehicle = VehicleData(status, id, img, title = name, des = des, price = price, phone = contact, createBy = createBy)
                            }
                        }

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

            itemView.setOnClickListener {
                onClickCallback.invoke(vehicle, data)
            }
        }
    }
}