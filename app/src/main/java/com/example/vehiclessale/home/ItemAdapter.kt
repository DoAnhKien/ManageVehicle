package com.example.vehiclessale.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.example.vehiclessale.MyUtils
import com.example.vehiclessale.R

class ItemAdapter(val list: MutableList<VehicleData> = mutableListOf()) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    fun addAll(lst: MutableList<VehicleData>){
        list.clear()
        list.addAll(lst)
        notifyDataSetChanged()
    }

    var onClickCallback: (VehicleData) -> Unit = {}
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.layout_item_vehicle, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.imgVehicle)
        lateinit var imgVehicle: AppCompatImageView

        @BindView(R.id.tvtitle)
        lateinit var tvtitle: AppCompatTextView

        @BindView(R.id.tvDes)
        lateinit var tvDes: AppCompatTextView

        @BindView(R.id.tvPrice)
        lateinit var tvPrice: AppCompatTextView

        init {
            ButterKnife.bind(this, itemView)
        }
        fun bind(data: VehicleData){
            tvtitle.text = data.title
            tvDes.text = data.des
            tvPrice.text = MyUtils.formatPrice(data.price.toDouble()) + "$"
            Glide.with(itemView).load(data.imgs[0].urlImg).into(imgVehicle)
            itemView.setOnClickListener { onClickCallback.invoke(data) }
        }

    }
}