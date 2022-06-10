package com.example.vehiclessale.orders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.example.vehiclessale.MyEnum
import com.example.vehiclessale.MyUtils
import com.example.vehiclessale.R
import com.example.vehiclessale.cart.CartData
import com.example.vehiclessale.cart.PlaceOrderData

class ItemOrderAdapter (var list: MutableList<PlaceOrderData> = mutableListOf(), var type: Int = 0): RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    var onUpdateStatus: (PlaceOrderData) -> Unit = {}

    fun removeItem(position: Int){
        list.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, list.size)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.layout_item_cart, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        @BindView(R.id.layout_delete)
        lateinit var layout_delete: FrameLayout
        @BindView(R.id.img)
        lateinit var img: AppCompatImageView
        @BindView(R.id.tvTitle)
        lateinit var tvTitle: AppCompatTextView
        @BindView(R.id.tvDes)
        lateinit var tvDes: AppCompatTextView
        @BindView(R.id.tvPrice)
        lateinit var tvPrice: AppCompatTextView
        @BindView(R.id.checkbox)
        lateinit var checkbox: AppCompatCheckBox
        @BindView(R.id.btnDelivering)
        lateinit var btnDelivering: AppCompatButton
        @BindView(R.id.tvUserBuyer)
        lateinit var tvUserBuyer: AppCompatTextView
        @BindView(R.id.swipelayout)
        lateinit var swipelayout: SwipeRevealLayout
        init {
            ButterKnife.bind(this, itemView)
        }
        fun bind(data: PlaceOrderData){
            checkbox.isVisible = type == 0
            swipelayout.setLockDrag(type != 0)
            btnDelivering.isVisible = type == 2
            tvUserBuyer.isVisible = type == 2
            if(data.product.product.status == MyEnum.ADDED_CART.Name()){
                btnDelivering.text = MyEnum.DELIVERING.Name()
            }
            if(data.product.product.status == MyEnum.DELIVERING.Name()){
                btnDelivering.text = MyEnum.COMPLETE.Name()
            }
            tvUserBuyer.text = "Buyer: ${data.nameBuyer}"
            Glide.with(itemView).load(data.product.product.imgs[0].urlImg).into(img)
            tvTitle.text = data.product.product.title
            tvDes.text = data.product.product.des
            tvPrice.text = MyUtils.formatPrice(data.product.product.price.toDouble()) + "$"
            btnDelivering.setOnClickListener {
                onUpdateStatus.invoke(data)
            }
        }
    }
}