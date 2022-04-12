package com.example.vehiclessale.notification

import com.example.vehiclessale.cart.PlaceOrderData

data class NotificationData(
    val idNotify: String = "",
    val seen: String = "",
    val content: String,
    val idReceiver: String = "",
    val placeOrderData: PlaceOrderData = PlaceOrderData())