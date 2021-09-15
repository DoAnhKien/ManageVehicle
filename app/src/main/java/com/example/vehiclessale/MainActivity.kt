package com.example.vehiclessale

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import butterknife.BindView
import butterknife.ButterKnife
import com.example.vehiclessale.login.LoginData
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    @BindView(R.id.navBottom)
    lateinit var navBottom: BottomNavigationView

    @BindView(R.id.navHost)
    lateinit var navHost: FragmentContainerView

    private lateinit var navController: NavController

    private val reference: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference
    }

    private val refNotification: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("notification")
    }

    private var count = 0
    private var check = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        navController = navHostFragment.navController
        NavigationUI.setupWithNavController(navBottom, navController)
        initBottomNav()
        navBottom.itemIconTintList = null
        var countCart = UserPrefs.getNumberCartData(this)
        countCart?.let {
            createBadge(it.toInt())
        }
        var countNotify = UserPrefs.getNumberNotify(this)
        countNotify?.let {
            createBadgeNotification(it.toInt())
        }
    }

    private fun initBottomNav() {
        navBottom.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.homeFragment -> {
                    val navOptions =
                        NavOptions.Builder().setPopUpTo(R.id.homeFragment, true).build()
                    navController.navigate(R.id.homeFragment, null, navOptions)
                    it.isChecked = true
//                    navBottom.menu.findItem(R.id.notification)
//                        .setIcon(R.drawable.ic_baseline_notifications_24_1)
//                    navBottom.menu.findItem(R.id.homeFragment)
//                            .setIcon(R.drawable.ic_baseline_home_24)
                }
                R.id.cartFragment -> {
//                    navBottom.menu.findItem(R.id.notification)
//                        .setIcon(R.drawable.ic_baseline_notifications_24_1)
                    it.isChecked = true
                    navController.navigateUp()
                    navController.navigate(R.id.cartFragment)
                }
                R.id.notificationFragment -> {
                    it.isChecked = true
                    //it.setIcon(R.drawable.ic_baseline_notifications_24)
                    navController.navigateUp()
                    navController.navigate(R.id.notificationFragment)
//                    navBottom.menu.findItem(R.id.homeFragment)
//                            .setIcon(R.drawable.ic_home_grey)
                }
                R.id.profileFragment -> {
//                    navBottom.menu.findItem(R.id.notification)
//                        .setIcon(R.drawable.ic_baseline_notifications_24_1)
                    navController.navigateUp()
                    var user = Gson().fromJson(UserPrefs.getLocalData(this), LoginData::class.java)
                    user?.let {
                        if (user.id.isNotEmpty()) {
                            navController.navigate(R.id.profileFragment2)
                        } else {
                            navController.navigate(R.id.loginFragment)
                        }
                    } ?: kotlin.run { navController.navigate(R.id.loginFragment) }

                    it.isChecked = true
                }
            }
            false
        }
        navBottom.setOnNavigationItemReselectedListener {

        }
    }

    fun enableBottom(value: Boolean) {
        navBottom.isVisible = value
    }

    fun createBadge(number: Int) {
        val badge = navBottom.getOrCreateBadge(R.id.cartFragment)
        badge.number = number
        UserPrefs.setNumberCartData(this, number.toString())

        badge.badgeTextColor = resources.getColor(R.color.text)
        badge.backgroundColor = resources.getColor(R.color.badge)
        if (!badge.hasNumber()) {
            badge.clearNumber()
        }
        val count = badge.number
        navBottom.getBadge(R.id.cartFragment)?.isVisible = count > 0
    }

    fun createBadgeNotification(number: Int) {
        val badge = navBottom.getOrCreateBadge(R.id.notificationFragment)
        badge.number = number
        UserPrefs.setNumberNotify(this, number.toString())
        badge.badgeTextColor = resources.getColor(R.color.text)
        badge.backgroundColor = resources.getColor(R.color.badge)
        if (!badge.hasNumber()) {
            badge.clearNumber()
        }
        val count = badge.number
        navBottom.getBadge(R.id.notificationFragment)?.isVisible = count > 0
    }

    fun notificationOrder() {
        reference.child("notification").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("AAAAAA", "ok")
                snapshot.children.forEach { data ->
                    var user = Gson().fromJson(
                        UserPrefs.getLocalData(this@MainActivity),
                        LoginData::class.java
                    )
                    user?.let {
                        var idNotify = data.child("idNotify").value.toString()
                        if (data.child("idReceiver").value.toString() == it.id) {
                           if(data.child("seen").value.toString() == MyEnum.NO_SEEN.Name()){
                               val content = data.child("content").value.toString()
                               count++
                               if(count == 1) {
                                   showAlertDialog(
                                           "Thông Báo",
                                           content = "Ban co thong bao moi",
                                           confirmButtonTitle = getString(R.string.txt_see_new_order),
                                           cancelButtonTitle = getString(R.string.txt_Cancel),
                                           confirmCallback = {
                                               //updateStatus()
                                               navController.navigate(R.id.notificationFragment)
                                           },
                                           cancelCallback = {
                                           }
                                   )
                               }
                           }

                        }
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun updateStatus(){
        refNotification.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val idNotify = it.child("idNotify").value.toString()
                    if (it.child("seen").value.toString() == MyEnum.NO_SEEN.Name()) {
                        refNotification.child(idNotify).child("seen").setValue(MyEnum.SEEN.Name())
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    open fun showAlertDialog(
        title: String,
        content: String,
        confirmButtonTitle: String,
        cancelButtonTitle: String,
        confirmCallback: () -> Unit,
        cancelCallback: () -> Unit,
        cancelable: Boolean = false
    ) {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(this)

        val dialog = builder
            .setTitle(title)
            .setMessage(content)
            .setPositiveButton(confirmButtonTitle) { dialog, _ ->
                confirmCallback.invoke()
            }
            .setCancelable(cancelable)
            .show()

        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
       // val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

        positiveButton.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.red
            )
        )
//        negativeButton.setTextColor(
//            ContextCompat.getColor(
//                this,
//                R.color.red
//            )
//        )
    }
}