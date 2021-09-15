package com.example.vehiclessale.addProduct

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.example.vehiclessale.R
import com.example.vehiclessale.addProduct.Model.ImageData

class ImageAdapter(var list: MutableList<ImageData> = mutableListOf()) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_ADD = 1
    private val TYPE_NORMAL = 2
    private val TYPE_LOAD = 3

    var onAddCallback: () -> Unit = {}
    var onRemoveCallback: (Int, String) -> Unit = { _, _ -> }

    fun addImage(image: ImageData) {
        list.add(list.size - 1, image)
        notifyItemInserted(list.size)
    }

    fun removeItem(position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, list.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_NORMAL -> ViewHolder(
                    LayoutInflater.from(parent.context)
                            .inflate(R.layout.layout_item_image, parent, false)
            )
            TYPE_LOAD -> {
                LoadHolder(
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.layout_item_image_load, parent, false)
                )
            }
            else -> {
                AddHolder(
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.layout_item_add_img, parent, false)
                )
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_NORMAL -> (holder as ViewHolder).bind(list[position])
            TYPE_LOAD -> (holder as LoadHolder).bind()
            else -> (holder as AddHolder).bind()
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @BindView(R.id.imgAnh)
        lateinit var imgAnh: AppCompatImageView

        @BindView(R.id.imgRemove)
        lateinit var imgRemove: AppCompatImageView

        init {
            ButterKnife.bind(this, itemView)
        }

        fun bind(data: ImageData) {
            Glide.with(itemView).load(data.urlImg).into(imgAnh)
            imgRemove.setOnClickListener { onRemoveCallback.invoke(adapterPosition, data.urlImg) }
        }

    }

    inner class AddHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @BindView(R.id.imgAdd)
        lateinit var imgAdd: AppCompatImageView

        init {
            ButterKnife.bind(this, itemView)
        }

        fun bind() {
            imgAdd.setOnClickListener {
                onAddCallback.invoke()
            }
        }

    }

    inner class LoadHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @BindView(R.id.progressbar)
        lateinit var progressbar: ProgressBar

        init {
            ButterKnife.bind(this, itemView)
        }

        fun bind() {
            progressbar.isVisible = true
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].id.isEmpty()) {
            TYPE_ADD
        } else if (list[position].id == "Load") {
            TYPE_LOAD
        } else TYPE_NORMAL
    }
}