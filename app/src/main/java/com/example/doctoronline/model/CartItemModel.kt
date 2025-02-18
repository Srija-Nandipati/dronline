package com.example.doctoronline.model

import com.google.firebase.firestore.DocumentReference

data class CartItemModel(
    val medicineRef: DocumentReference? = null,
    var quantity: Int = 0

)
