package com.example.doctoronline

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doctoronline.adapter.CartItemAdapter
import com.example.doctoronline.adapter.MedicineAdapter
import com.example.doctoronline.commons.FireBaseCommon
import com.example.doctoronline.databinding.ActivityCartScreenBinding
import com.example.doctoronline.databinding.DialogProgressBinding
import com.example.doctoronline.databinding.PaymentBottomsheetBinding
import com.example.doctoronline.model.CartItemModel
import com.example.doctoronline.model.MedicineModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class CartScreen : AppCompatActivity() {
    private val binding by lazy { ActivityCartScreenBinding.inflate(layoutInflater) }

    private lateinit var cartItemList: ArrayList<CartItemModel>
    private lateinit var adapter: CartItemAdapter
    private val firestore by lazy { Firebase.firestore }

    private lateinit var auth : FirebaseAuth
    private lateinit var progressDialog: AlertDialog

    private lateinit var firebaseCommon :FireBaseCommon

    private val TAG = "CartScreen"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firebaseCommon = FireBaseCommon(firestore,auth)

        binding.backBtn.setOnClickListener { onBackPressed() }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(R.color.white, theme)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        progressDialog = AlertDialog.Builder(this, R.style.CustomAlertDialog).create()
        val progressDialogLay = DialogProgressBinding.inflate(LayoutInflater.from(this))
        progressDialog.setView(progressDialogLay.root)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()


        setUpRecyclerView()

        getCartListFromFirebase()

        adapter.addCartRemoveBtnClick = { cartItem, position ->
            progressDialog.show()
            if (position != -1) {
                firestore.collection("CartsList")
                    .document(auth.currentUser!!.uid)
                    .collection("UserCart")
                    .whereEqualTo("medicineRef",cartItem.medicineRef)
                    .get()
                    .addOnSuccessListener {
                        it.documents.let {
                                val documentId = it.first().id
                            Log.i(TAG, "onCreate: $documentId")
                            if (documentId != null) {
                                firestore.collection("CartsList")
                                    .document(auth.currentUser!!.uid)
                                    .collection("UserCart")
                                    .document(documentId)
                                    .delete()
                                    .addOnSuccessListener {
                                        //  update the RecyclerView after successful deletion
//                            val position = cartItems.indexOf(cartItem)
                                        cartItemList.removeAt(position)
                                        adapter.notifyItemRemoved(position)
                                        progressDialog.dismiss()
                                        //Calculate total price
                                        calculateTotalPrice(cartItemList) {totalPrice->
                                            val t = String.format("%.2f", totalPrice)
                                            binding.totalPricetxt.text = "₹ ${t}"
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        // Handle failure to delete document
                                        progressDialog.dismiss()
                                        Log.e(TAG, "onCreate: "+e.message.toString())
                                        Toast.makeText(this, "Not Deleted", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    }
            }

        }

        adapter.updateCartItemOnClick = {cartItem->
            progressDialog.show()
            val position = cartItemList.indexOf(cartItem)
            if(position!=-1){
                firestore.collection("CartsList")
                    .document(auth.currentUser!!.uid)
                    .collection("UserCart")
                    .whereEqualTo("medicineRef",cartItem.medicineRef)
                    .get()
                    .addOnSuccessListener {
                        it.documents.let {
                            val documentId = it.first().id
                            updateCartItem(documentId, cartItem)

                            Log.i(TAG, "onCreate: $documentId")
                        }
                    }
                    .addOnFailureListener {
                        progressDialog.dismiss()
                        Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()

                    }
            }

        }

        binding.payementBtn.setOnClickListener {
            showPaymentSheet()
        }
        
    }

    private fun updateCartItem(documentId: String, cartItem: CartItemModel) {
        firebaseCommon.updateCartItem(documentId,cartItem){ updateditem,e->
            if(e==null){ ///Success
                progressDialog.dismiss()
                Toast.makeText(this, "Updated item", Toast.LENGTH_SHORT).show()
                //Calculate total price
                calculateTotalPrice(cartItemList) {totalPrice->
                    val t = String.format("%.2f", totalPrice)
                    binding.totalPricetxt.text = "₹ ${t}"
                }
            }else{
                progressDialog.dismiss()
                Log.e(TAG, "updateCartItem: "+e.message.toString())
                Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()

            }
        }
    }

    private fun getCartListFromFirebase() {
        firestore.collection("CartsList")
            .document(auth.currentUser!!.uid)
            .collection("UserCart")
            .get()
            .addOnSuccessListener {
                cartItemList.clear()

                for (item in it.documents){
                    val itemObj = item.toObject<CartItemModel>()
                    if (itemObj != null) {
                        cartItemList.add(itemObj)
                    }
                }
                adapter.notifyDataSetChanged()
                progressDialog.dismiss()

                //Calculate total price
                calculateTotalPrice(cartItemList) {totalPrice->
                   val t = String.format("%.2f", totalPrice)
                    binding.totalPricetxt.text = "₹ ${t}"
                }
            }
            .addOnFailureListener{
                progressDialog.dismiss()
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }

    }

    private fun setUpRecyclerView() {
        cartItemList = ArrayList()
        adapter = CartItemAdapter(this, cartItemList)
        binding.recyclerView.layoutManager= LinearLayoutManager(this@CartScreen)
        binding.recyclerView.adapter = adapter
    }

    private fun calculateTotalPrice(cartItemList: List<CartItemModel>, onTotalPriceCalculated: (Double) -> Unit) {
        var totalPrice = 0.0
        val fetchMedicineTasks = mutableListOf<Task<DocumentSnapshot>>()

        for (cartItem in cartItemList) {
            val fetchTask = cartItem.medicineRef?.get()
            fetchMedicineTasks.add(fetchTask!!)
        }

        Tasks.whenAllComplete(fetchMedicineTasks)
            .addOnSuccessListener { taskList ->
                for (i in taskList.indices) {
                    val task = taskList[i]
                    if (task.isSuccessful) {
                        val medicineDocument = task.result as DocumentSnapshot
                        if (medicineDocument.exists()) {
                            val medicine = medicineDocument.toObject<MedicineModel>()
                            if (medicine != null) {
                                // Now you have the Medicine object
                                val currentPrice = medicine.price.times(cartItemList[i].quantity)
                                totalPrice += currentPrice
                                //Log.i(TAG, "calculateTotalPrice: $totalPrice")
                            }
                        }
                    }
                }

            }
            .addOnCompleteListener {
                // Callback to notify the total price is calculated
                onTotalPriceCalculated(totalPrice)
            }
    }


        private fun showPaymentSheet() {
            val dialog = BottomSheetDialog(this)

            val bottomSheetLayout = PaymentBottomsheetBinding.inflate(layoutInflater)
            bottomSheetLayout.buttonClose.setOnClickListener {
                dialog.dismiss()
            }

            bottomSheetLayout.googlepayLay.setOnClickListener {
                goPayment()
            }

            bottomSheetLayout.phonepeLay.setOnClickListener {
                goPayment()
            }

            bottomSheetLayout.paytmLay.setOnClickListener {
                goPayment()
            }

            dialog.setCancelable(true)
            dialog.setContentView(bottomSheetLayout.root)
            dialog.show()
        }

    private fun goPayment() {
        val intent = Intent(this, PaymentScreen::class.java)
        val t = binding.totalPricetxt.text.toString()
        intent.putExtra("totalPrice",t)
        startActivity(intent)
    }




}