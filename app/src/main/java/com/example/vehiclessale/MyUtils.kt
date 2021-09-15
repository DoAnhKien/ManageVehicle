package com.example.vehiclessale

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.text.Editable
import android.util.Base64
import android.util.Patterns
import androidx.core.content.ContextCompat
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


class MyUtils {

    companion object {
        const val IMAGE_CAPTURE_CODE = 100
        const val PERMISSION_CODE = 101
        const val OPEN_FILE = 99

        fun decodeBase64(value: String): ByteArray {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                java.util.Base64.getDecoder().decode(value)
            } else {
                Base64.decode(value, Base64.DEFAULT)
            }
        }

        fun checkInvalidEmail(email: String): Boolean {
            val pattern = Pattern.compile("^.+@.+\\..+$")
            val matcher = pattern.matcher(email)
            return matcher.find()
        }

        fun checkInvalidPass(pass: String): Boolean {
            return !(pass.isEmpty() || pass.length < 6)
        }

        fun getDate(): String{
            val c = Calendar.getInstance().time
            val df = SimpleDateFormat("dd/MM/yyyy")
            val formattedDate = df.format(c)
            val df1 = SimpleDateFormat("HH:mm")
            val formattedDate1 = df1.format(c)
            return "$formattedDate1 ngày $formattedDate"
        }

        fun formatPrice(value: Double): String{
            val formatter: NumberFormat = DecimalFormat("#,###")
            return formatter.format(value)
        }

        open fun hasPermission(context: Context, vararg permissions: String) = permissions.all {
            ContextCompat.checkSelfPermission(
                    context,
                    it
            ) == PackageManager.PERMISSION_GRANTED
        }
        fun String.isValidPhoneNumber() : Boolean {
            val patterns =  "^\\s*(?:\\+?(\\d{1,3}))?[-. (]*(\\d{3})[-. )]*(\\d{3})[-. ]*(\\d{4})(?: *x(\\d+))?\\s*$"
            return Pattern.compile(patterns).matcher(this).matches()
        }
        fun String.toEditable(): Editable {
            return Editable.Factory.getInstance().newEditable(this)
        }
    }
}