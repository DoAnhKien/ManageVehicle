package com.example.vehiclessale.notification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.example.vehiclessale.R
import com.example.vehiclessale.profile.FunctionData

class ItemNotifyAdapter (var list: MutableList<NotificationData> = mutableListOf()): RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    var onItemCallBack: (FunctionData) -> Unit = {}
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.layout_item_card_profile, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        @BindView(R.id.img)
        lateinit var img: AppCompatImageView
        @BindView(R.id.tvContent)
        lateinit var tvContent: AppCompatTextView

        init {
            ButterKnife.bind(this, itemView)
        }

        fun bind(data: NotificationData){

        }
    }
}