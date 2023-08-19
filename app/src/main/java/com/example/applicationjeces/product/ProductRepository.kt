package com.example.applicationjeces.product

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*

class ProductRepository {

    private var jecesfirestore: FirebaseFirestore? = null

    // 데이터베이스 접근
    private fun getFirestore(): FirebaseFirestore {
        return jecesfirestore ?: FirebaseFirestore.getInstance().also {
            jecesfirestore = it
        }
    }

    // 중복 컬렉션
    fun getCollection(collectionName: String) = getFirestore().collection(collectionName)


    // 중복 다큐먼트
    fun updateDocument(collection: String, fieldMap: Map<String, Any>, pId: String, pName: String): Task<Void> {
        val ref = getCollection(collection)
            .whereEqualTo("ID", pId)
            .whereEqualTo("productName", pName)
            .get()

        var task: Task<Void>? = null
        ref.addOnSuccessListener { result ->
            result.forEach { document ->
                task = document.reference.update(fieldMap)
            }
        }
        return task!!
    }

    fun fetchNextPage(currentPage: Int, itemsPerPage: Int): Task<QuerySnapshot> {
        return getCollection("Product")
            .orderBy("someField")
            .startAfter(currentPage * itemsPerPage)
            .limit(itemsPerPage.toLong())
            .get()
    }

    fun allProduct(): Task<QuerySnapshot> {
        return getCollection("Product").get()
    }

    fun mySetProduct(email: String): Task<QuerySnapshot> {
        return getCollection("Product").whereEqualTo("ID", email).get()
    }

    fun mySetProductFv(email: String): Task<QuerySnapshot> {
        return getCollection("UserInfo").whereEqualTo("id", email).get()
    }

    fun addProducts(product: Product): Task<DocumentReference> {
        val currentTime = Timestamp.now()
        val productMap = mapOf(
            "IDX" to "${product.product_id}_${product.product_name}",
            "ID" to product.product_id,
            "productName" to product.product_name,
            "productPrice" to product.product_price,
            "productDescription" to product.product_description,
            "productCount" to product.product_count,
            "productImgUrl" to product.product_img_url,
            "pChatCount" to product.chatCount,
            "pViewCount" to product.viewCount,
            "pHeartCount" to product.heartCount,
            "productBidPrice" to product.product_bid_price,
            "insertTime" to currentTime
        )
        return getCollection("Product").add(productMap)
    }

    fun searchProductsCall(searchName: String): Task<QuerySnapshot> {
        return getCollection("Product").get()
    }

    fun viewCountUp(pId: String, pName: String): Task<QuerySnapshot> {
        return getCollection("Product")
            .whereEqualTo("ID", pId)
            .whereEqualTo("productName", pName)
            .get()
    }

    fun deleteProduct(pId: String, pName: String): Task<QuerySnapshot> {
        return getCollection("Product")
            .whereEqualTo("ID", pId)
            .whereEqualTo("productName", pName)
            .get()
    }

    fun updateProduct(pId: String, pName: String): Task<Void> {
        val currentTime = Timestamp.now()
        val fieldMap = mapOf("insertTime" to currentTime)
        return updateDocument("Product", fieldMap, pId, pName)
    }

    fun setMyFavorit(email: String, pIdx: String): Task<QuerySnapshot> {
        return getCollection("UserInfo").whereEqualTo("id", email).get()
    }
}
