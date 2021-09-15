package com.example.vehiclessale.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.example.vehiclessale.R

class DialogAddProduct {

  companion object{
      var onCallbackCancel: () -> Unit = {}
      fun show(message: String?,  context: Context) {
          val factory = LayoutInflater.from(context)
          val view: View = factory.inflate(R.layout.layout_notify_dialog, null)
          val dialog = AlertDialog.Builder(context).create()
          val back = ColorDrawable(Color.TRANSPARENT)
//          val inset = InsetDrawable(back, 64)
//          dialog.window?.setBackgroundDrawable(inset)
          dialog.setView(view)
          val txtMessage = view.findViewById<TextView>(R.id.tvContent)
          txtMessage.text = message
          val btnClose = view.findViewById<Button>(R.id.btnClose)
          val btnActive = view.findViewById<Button>(R.id.btnActive)
          btnClose.isVisible = false
          btnActive.setOnClickListener { dialog.dismiss()
          onCallbackCancel.invoke()}
          dialog.show()
      }
  }

}