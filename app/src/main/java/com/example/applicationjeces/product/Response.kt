package com.example.applicationjeces.product

import com.google.firebase.firestore.DocumentSnapshot

data class Response(
    var products: List<DocumentSnapshot>? = null,
    var searchChat: List<DocumentSnapshot>? = null,
    var exception: Exception? = null
)
