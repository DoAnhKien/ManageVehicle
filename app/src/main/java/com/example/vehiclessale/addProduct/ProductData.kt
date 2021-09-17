package com.example.vehiclessale.addProduct.Model

import com.example.vehiclessale.MyEnum

data class ProductRequestData(
    val status: String = MyEnum.POST_NEW.Name(),
    val list: MutableList<ImageData> = mutableListOf(),
    val idProduct: String = "",
    val nameProduct: String = "",
    val des: String = "",
    val price: String = "",
    val phone: String = "",
    val createby: Owner = Owner(),
    val type: String = ""
)

data class ImageData(val id: String = "", val urlImg: String = "")

data class Owner(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = ""
)