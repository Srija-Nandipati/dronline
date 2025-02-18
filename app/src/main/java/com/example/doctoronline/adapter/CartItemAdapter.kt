package com.example.doctoronline.adapter

import android.R
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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class CartItemAdapter(var context: Context, var cartList: ArrayList<CartItemModel>): RecyclerView.Adapter<CartItemAdapter.ViewHolder>() {


    private val TAG: String = "CartItemAdapter"
    private val firestore =Firebase.firestore

    var addCartRemoveBtnClick: ((CartItemModel, Int) -> Unit)? = null
    var updateCartItemOnClick: ((CartItemModel)-> Unit)? = null
    inner class ViewHolder(var binding:ItemCartBinding ): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return  cartList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cartItem = cartList[position]
        holder.binding.addCartRemoveBtn.text = "Remove"
        val quantityList = getSpinnerItemFromFirebase()
        var medicine :MedicineModel? = null

        val adapter: ArrayAdapter<Int> = ArrayAdapter(context, R.layout.simple_spinner_item, quantityList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.binding.dropdowntextview.setAdapter(adapter)

        cartItem.medicineRef?.get()
            ?.addOnSuccessListener { medicineDocument ->
                if (medicineDocument.exists()) {
                    medicine = medicineDocument.toObject<MedicineModel>()
                    medicine?.id  =medicineDocument.id
                    if (medicine != null) {
                        // Now you have the Medicine object
                        val cartQuantity = cartItem.quantity
                        holder.binding.dropdowntextview.setText("Qty: $cartQuantity", false)
                        holder.binding.nameTxt.text = medicine!!.name
                        holder.binding.descTxt.text = medicine!!.description
                        holder.binding.priceTxt.text = "₹ ${medicine!!.price * cartQuantity}"
                    }
                }
            }
            ?.addOnFailureListener {
                Log.e(TAG, "onBindViewHolder: $it" )
            }



        holder.binding.dropdowntextview.setOnItemClickListener { parent, view, pos, id ->
            // Handle item selection
            val selectedQty = parent?.getItemAtPosition(pos) as Int
            holder.binding.dropdowntextview.setText("Qty: $selectedQty", false)
            cartItem.quantity = selectedQty
            var price = formatpriceTotwodp(medicine!!.price * selectedQty)
            holder.binding.priceTxt.text = "₹ ${price}"

            updateCartItemOnClick?.invoke(cartItem)

        }

        holder.binding.addCartRemoveBtn.setOnClickListener {
            addCartRemoveBtnClick?.invoke(cartItem , position)
        }


    }


   private fun formatpriceTotwodp(value: Double): String {
        return String.format("%.2f", value)
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