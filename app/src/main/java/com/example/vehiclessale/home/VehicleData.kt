package com.example.vehiclessale.home

import com.example.vehiclessale.MyEnum
import com.example.vehiclessale.addProduct.Model.ImageData
import com.example.vehiclessale.addProduct.Model.Owner

data class VehicleData(
        val status: String = MyEnum.POST_NEW.Name(),
        val id: String = "",
        val imgs: List<ImageData> = listOf(),
        val title: String = "",
        val des: String = "",
        val price: String = "",
        val phone: String = "",
        val createBy: Owner = Owner()
)