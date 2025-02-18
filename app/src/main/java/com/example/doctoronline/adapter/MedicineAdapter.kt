package com.example.doctoronline.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.doctoronline.databinding.ItemCartBinding
import com.example.doctoronline.model.CartItemModel
import com.example.doctoronline.model.MedicineModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.UUID

class MedicineAdapter(var context: Context,var medicineList :List<MedicineModel>): RecyclerView.Adapter<MedicineAdapter.ViewHolder>() {


    private val TAG: String = "MedicineAdapter"
    private val firestore =Firebase.firestore

    var addCartRemoveBtnClick: ((CartItemModel) -> Unit)? = null

    inner class ViewHolder(var binding:ItemCartBinding ): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return  medicineList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var medicine = medicineList[position]
        var selectedQty:Int = 1
        holder.binding.nameTxt.text = medicine.name
        holder.binding.descTxt.text = medicine.description
        holder.binding.priceTxt.text = "₹ ${medicine.price}"

        val quantityList = getSpinnerItemFromFirebase()

        //default value to qty
        holder.binding.dropdowntextview.setText("Qty: $selectedQty", false)
        val adapter: ArrayAdapter<Int> = ArrayAdapter(context, android.R.layout.simple_spinner_item, quantityList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.binding.dropdowntextview.setAdapter(adapter)

        holder.binding.dropdowntextview.setOnItemClickListener { parent, view, position, id ->
            // Handle item selection
             selectedQty = parent?.getItemAtPosition(position) as Int
            holder.binding.dropdowntextview.setText("Qty: $selectedQty", false)

        }


        holder.binding.addCartRemoveBtn.setOnClickListener {
            val cartItem = CartItemModel(
                medicineRef = firestore.collection("MedicineList").document(medicine.id),
                quantity = selectedQty
            )

            addCartRemoveBtnClick?.invoke(cartItem)


        }



    }

    private fun getSpinnerItemFromFirebase() : ArrayList<Int>{
        val numbersList = ArrayList<Int>()

        firestore.collection("MedicineQty")
            .document("f5ykIPHrK3KuPCuTPzuN")
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val numbersArray = document.get("Qty") as ArrayList<Long>?

                    if (numbersArray != null) {
                        for (number in numbersArray) {
                            numbersList.add(number.toInt())
                        }


                    }
                }
            }
            .addOnFailureListener { exception ->
                // Handle failures
                Log.e(TAG, "Error getting documents: ", exception)
                return@addOnFailureListener
            }
        return numbersList

    }


}