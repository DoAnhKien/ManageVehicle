package com.example.vehiclessale.home

import android.os.Bundle
import android.os.Handler
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.example.vehiclessale.*
import com.example.vehiclessale.R
import com.example.vehiclessale.addProduct.Model.ImageData
import com.example.vehiclessale.addProduct.Model.Owner
import com.google.firebase.database.*
import com.google.gson.Gson

class HomeFragment : BaseFragment(), View.OnClickListener {

    lateinit var unbinder: Unbinder

    @BindView(R.id.rvVehicles)
    lateinit var rvVehicles: RecyclerView

    @BindView(R.id.imgMess)
    lateinit var imgMess: AppCompatImageView

    @BindView(R.id.searchView)
    lateinit var searchView: SearchView


    private var itemAdapter = ItemAdapter()

    override fun initUI() {
        showDialogLoading()
        actionTransition()
        (activity as MainActivity).apply {
            enableBottom(true)
            notificationOrder()
        }
        var countCart = UserPrefs.getNumberCartData(requireContext())
        countCart?.let {
            (activity as MainActivity).createBadge(it.toInt())
        }
        var countNotify = UserPrefs.getNumberNotify(requireContext())
        countNotify?.let {
            (activity as MainActivity).createBadgeNotification(it.toInt())
        }
        getListProduct(){ Handler().postDelayed({ hideDialogLoading() }, 1000)}
        itemAdapter = ItemAdapter()
        rvVehicles.apply {
            layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
            adapter = itemAdapter
        }
    }

    override fun initControl() {
        imgMess.setOnClickListener(this)
        itemAdapter.onClickCallback = {
            findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToProductDetailFragment(
                            Gson().toJson(it)
                    )
            )
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                val list = itemAdapter.list.filter {
                    it.title.toLowerCase().contains(query.toString().toLowerCase())
                }
                itemAdapter.addAll(list.toMutableList())
                itemAdapter.notifyDataSetChanged()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(newText?.isEmpty() == true){
                    getListProduct()
                }
               return false
            }

        })

        searchView.setOnCloseListener {
            getListProduct()
            true
        }
    }

    private val reference: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("listProduct")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        unbinder = ButterKnife.bind(this, view)
        return view
    }



    private fun actionTransition(){
        val inflater = TransitionInflater.from(requireContext())
        exitTransition = inflater.inflateTransition(R.transition.fade)
        //enterTransition = inflater.inflateTransition(R.transition.fade)
    }
    private fun getListProduct(onCallback: () -> Unit = {}){
        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                itemAdapter.list.clear()
                mapToList(snapshot)
                itemAdapter.notifyDataSetChanged()
                onCallback.invoke()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgMess -> {
                findNavController().navigate(R.id.action_homeFragment_to_listChatsFragment)
            }
        }
    }

    private fun mapToList(dataSnapshot: DataSnapshot) {
        val genericTypeIndicator: GenericTypeIndicator<List<ImageData>> =
                object : GenericTypeIndicator<List<ImageData>>() {}

        val generic: GenericTypeIndicator<Owner> =
                object : GenericTypeIndicator<Owner>() {}

        for(data in dataSnapshot.children){
            var status = data.child("status").value.toString()
            var id = data.child("idProduct").value.toString()
            var name = data.child("name product").value.toString()
            var des = data.child("description").value.toString()
            var price = data.child("price").value.toString()
            var img = data.child("images").getValue(genericTypeIndicator)
            var contact = data.child("contact").value.toString()
            var createBy = data.child("create by").getValue(generic)
            img?.let {imgs ->
                createBy?.let {
                    if(status != MyEnum.COMPLETE.Name() && status != MyEnum.DELIVERING.Name())
                    itemAdapter.list.add(VehicleData(status = status, id = id, imgs = imgs, name, des, price = price, phone = contact, createBy = createBy))
                }
            }
        }
    }

}