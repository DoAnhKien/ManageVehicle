package com.example.vehiclessale.cart

import com.example.vehiclessale.MyEnum
import com.example.vehiclessale.home.VehicleData

data class PlaceOrderData(
    val nameBuyer: String = "",
    val idUserBuyer: String = "",
    val status: String = MyEnum.OTHER.Name(),
    val idOrder: String = "",
    val total: String = "",
    val product: CartData = CartData()
)