package com.example.doctoronline

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doctoronline.adapter.MedicineAdapter
import com.example.doctoronline.commons.FireBaseCommon
import com.example.doctoronline.databinding.ActivityBuyMedicineScreenBinding
import com.example.doctoronline.databinding.DialogProgressBinding
import com.example.doctoronline.model.CartItemModel
import com.example.doctoronline.model.MedicineModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class BuyMedicineScreen : AppCompatActivity() {
    private val binding by lazy { ActivityBuyMedicineScreenBinding.inflate(layoutInflater) }

    private lateinit var medicineList: ArrayList<MedicineModel>
    private lateinit var adapter: MedicineAdapter
    private val firestore by lazy { Firebase.firestore }
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: AlertDialog

    private lateinit var firebaseCommon: FireBaseCommon

    private val TAG = "BuyMedicineScreen"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.cartImg.setOnClickListener {
            startActivity(Intent(this@BuyMedicineScreen, CartScreen::class.java))
            finish()
        }
        auth = FirebaseAuth.getInstance()
        firebaseCommon = FireBaseCommon(firestore, auth)
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

        getMedicinesFromFirebase()

        binding.gotocartBtn.setOnClickListener {
            startActivity(Intent(this, CartScreen::class.java))
        }


        adapter.addCartRemoveBtnClick = { cartItem ->
            progressDialog.show()
            firestore.collection("CartsList")
                .document(auth.currentUser!!.uid)
                .collection("UserCart")
                .whereEqualTo("medicineRef", cartItem.medicineRef)
                .get()
                .addOnSuccessListener {
                    it.documents.let {
                        if (it.isEmpty()) { //Add new product
                            addNewCartItem(cartItem)
                        } else {
                            val documentId = it.first().id
                            updateCartItem(documentId, cartItem)
                        }

                    }
                }
                .addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()

                }


        }


    }

    private fun updateCartItem(documentId: String, cartItem: CartItemModel) {
        firebaseCommon.updateCartItem(documentId, cartItem) { _, e ->
            if (e == null) { ///Success
                progressDialog.dismiss()
                Toast.makeText(this, "Updated item", Toast.LENGTH_SHORT).show()
            } else {
                progressDialog.dismiss()
                Log.e(TAG, "updateCartItem: " + e.message.toString())
                Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()

            }
        }
    }

    private fun addNewCartItem(cartItem: CartItemModel) {
        firebaseCommon.addNewCartItem(cartItem) { addedItem, e ->
            if (e == null) {// Success
                progressDialog.dismiss()
                Toast.makeText(this, "Added item", Toast.LENGTH_SHORT).show()
            } else {  // Failure
                progressDialog.dismiss()
                Log.e(TAG, "addNewCartItem: " + e.message.toString())
                Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun getMedicinesFromFirebase() {
        firestore.collection("MedicineList").get()
            .addOnSuccessListener {
                medicineList.clear()

                for (medicine in it.documents) {
                    val tempMedicineModel = medicine.toObject<MedicineModel>()
                    tempMedicineModel!!.id = medicine.id
                    medicineList.add(tempMedicineModel)
                }

                adapter.notifyDataSetChanged()
                progressDialog.dismiss()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }

    }

    private fun setUpRecyclerView() {
        medicineList = ArrayList()
        adapter = MedicineAdapter(this, medicineList)
        binding.recMedicine.layoutManager = LinearLayoutManager(this@BuyMedicineScreen)
        binding.recMedicine.adapter = adapter
    }

}