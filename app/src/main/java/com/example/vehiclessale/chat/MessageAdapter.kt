package com.example.vehiclessale.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.example.vehiclessale.R
import com.example.vehiclessale.login.LoginData
import java.text.SimpleDateFormat

class MessageAdapter(var list: MutableList<MessData> = mutableListOf(), val user: LoginData = LoginData()) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun addAll(lst: MutableList<MessData>){
        list.clear()
        list.addAll(lst)
        notifyDataSetChanged()
    }
    fun addItem(item: MessData){
        list.add(item)
        notifyItemInserted(list.size - 1)
       // notifyItemRangeChanged(list.size - 1, list.size)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> {
                SenderViewHolder(
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.layout_item_sender, parent, false)
                )
            }
            else -> {
                ReceiverViewHolder(
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.layout_item_receiver, parent, false)
                )
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            1 -> (holder as SenderViewHolder).bind(list[position])
            else -> (holder as ReceiverViewHolder).bind(list[position])
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].idSender == user.id) {
            1
        } else {
            2
        }
    }

    inner class SenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.tvNoiDung)
        lateinit var tvNoiDung: AppCompatTextView
        @BindView(R.id.tvTime)
        lateinit var tvTime: AppCompatTextView

        init {
            ButterKnife.bind(this, itemView)
        }

        fun bind(messData: MessData) {
            tvNoiDung.text = messData.content
            val sdf = SimpleDateFormat("dd/MM/yyyy")
            tvTime.text = sdf.format(messData.time)
        }
    }

    inner class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.tvNoiDung)
        lateinit var tvNoiDung: AppCompatTextView
        @BindView(R.id.tvTime)
        lateinit var tvTime: AppCompatTextView

        init {
            ButterKnife.bind(this, itemView)
        }

        fun bind(messData: MessData) {
            tvNoiDung.text = messData.content
            val sdf = SimpleDateFormat("dd/MM/yyyy")
            tvTime.text = sdf.format(messData.time)
        }
    }
}