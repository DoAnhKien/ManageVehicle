package com.example.vehiclessale.productDetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.example.vehiclessale.R
import com.example.vehiclessale.addProduct.Model.ImageData
import com.smarteist.autoimageslider.SliderViewAdapter

class SliderAdapter(val listImgSlide: List<ImageData>): SliderViewAdapter<SliderAdapter.ViewHolder>() {
    override fun getCount(): Int {
        return listImgSlide.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?): SliderAdapter.ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.layout_image_slide, null)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: SliderAdapter.ViewHolder?, position: Int) {
        viewHolder?.bind(listImgSlide[position])
    }

    inner class ViewHolder(itemView: View?) : SliderViewAdapter.ViewHolder(itemView){

        @BindView(R.id.img)
        lateinit var img: AppCompatImageView
        init {
            ButterKnife.bind(this, itemView!!)
        }
        fun bind(data: ImageData){
            Glide.with(itemView).load(data.urlImg).into(img)
        }
    }
}