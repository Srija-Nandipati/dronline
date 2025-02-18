package com.example.doctoronline.commons

import android.util.Log
import android.widget.Toast
import com.example.doctoronline.model.CartItemModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class FireBaseCommon(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {


    private val cartCollection =
        firestore.collection("CartsList")
            .document(auth.currentUser!!.uid)
            .collection("UserCart")

     fun addNewCartItem(cartItem: CartItemModel, onResult: (CartItemModel?,Exception?) -> Unit) {
         cartCollection
            .document().set(cartItem)
            .addOnSuccessListener {
                onResult(cartItem,null)
            }
            .addOnFailureListener {
               onResult(null,it)
            }
    }

     fun updateCartItem(documentId: String, cartItem: CartItemModel,onResult: (CartItemModel?,Exception?) -> Unit) {
        firestore.runTransaction { transition ->
            val documentRef = firestore.collection("CartsList")
                .document(auth.currentUser!!.uid)
                .collection("UserCart")
                .document(documentId)
            val document = transition.get(documentRef)
            val cartItemObject = document.toObject<CartItemModel>()
            cartItemObject?.let { cartProduct ->
                val newQuantity = cartItem.quantity
                val newProductObject = cartProduct.copy(quantity = newQuantity)
                transition.set(documentRef, newProductObject)
            }
        }
            .addOnSuccessListener {
                onResult(cartItem,null)
            }
            .addOnFailureListener {
                onResult(null,it)
            }
    }
}
