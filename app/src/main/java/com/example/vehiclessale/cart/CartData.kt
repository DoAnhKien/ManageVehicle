package com.example.vehiclessale.cart

import com.example.vehiclessale.home.VehicleData

data class CartData (
    val idCart: String = "",
    val idUser:String = "",
    val product: VehicleData = VehicleData())