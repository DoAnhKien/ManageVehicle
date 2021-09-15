package com.example.vehiclessale.chat

import java.util.*

data class MessData(val idProduct: String = "", val idSender: String = "", val nameSender: String = "", val idReceiver: String = "", val nameReceiver: String = "", val content: String = "", val time: Date = Date())