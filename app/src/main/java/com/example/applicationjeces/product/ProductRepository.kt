package com.example.applicationjeces.product

import android.util.Log
import com.example.applicationjeces.chat.ChatroomData
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProductRepository {

    private var jecesfirestore: FirebaseFirestore? = null
    val thisUser: String by lazy { FirebaseAuth.getInstance().currentUser?.email ?: "" }
    private val storageRef = FirebaseStorage.getInstance().reference.child("adverhome/")

    // 데이터베이스 접근
    private fun getFirestore(): FirebaseFirestore {
        return jecesfirestore ?: FirebaseFirestore.getInstance().also {
            jecesfirestore = it
        }
    }

    // 중복 컬렉션
    private fun getCollection(collectionName: String) = getFirestore().collection(collectionName)

    fun fetchNextPageData(currentPage: Int, itemsPerPage: Int): Task<QuerySnapshot> {
        return getCollection("Product")
            .orderBy("someField")
            .startAfter(currentPage * itemsPerPage)
            .limit(itemsPerPage.toLong())
            .get()
    }

    suspend fun fetchAdverCounts(): Int {
        return withContext(Dispatchers.IO) {
            try {
                val listResult = Tasks.await(storageRef.listAll())
                return@withContext listResult.items.size
            } catch (e: Exception) {
                Log.e("AdverCounts", "Error occurred: ${e.message}", e)
                throw e
            }
        }
    }

    suspend fun allProduct(): List<DocumentSnapshot> {
        return getCollection("Product").get().await().documents
    }

    suspend fun mySetProduct(): List<DocumentSnapshot> {
        return getFirestore().collection("Product").whereEqualTo("ID", thisUser).get().await().documents
    }

    suspend fun mySetProductFv(): List<DocumentSnapshot> {
        val userDocuments = jecesfirestore!!.collection("UserInfo").whereEqualTo("id", thisUser).get().await()
        val favoritList = userDocuments.documents[0].get("favorit") as List<String>
        return jecesfirestore!!.collection("Product").whereIn("IDX", favoritList).get().await().documents
    }

    suspend fun addProducts(product: Product) {
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
            // ... (remaining fields as earlier)
        )
        getFirestore().collection("Product").add(productMap).await()
    }

    suspend fun searchProductsCall(searchName: String): List<DocumentSnapshot> {
        val productSearch = getFirestore().collection("Product").get().await()
        return productSearch.filter {
            it.getString("productName")?.contains(searchName) == true
        }
    }

    suspend fun viewCountUp(pId: String, pName: String) {
        val productSnapshot = getCollection("Product")
            .whereEqualTo("ID", pId)
            .whereEqualTo("productName", pName)
            .get()
            .await()

        productSnapshot.documents.forEach { document ->
            val viewCount = document.getString("pViewCount")?.toIntOrNull() ?: 0
            val update = mapOf("pViewCount" to (viewCount + 1).toString())
            document.reference.set(update, SetOptions.merge()).await()
        }
    }

    suspend fun bidchange(pId: String, pName: String, bidPrice: String) {
        val fieldMap = mapOf("productBidPrice" to bidPrice)
        val productSnapshot = getCollection("Product")
            .whereEqualTo("ID", pId)
            .whereEqualTo("productName", pName)
            .get()
            .await()

        productSnapshot.documents.forEach { document ->
            document.reference.update(fieldMap).await()
        }
    }

    suspend fun deleteProduct(pId: String, pName: String) {
        val product = getCollection("Product")
            .whereEqualTo("ID", pId)
            .whereEqualTo("productName", pName)
            .get()
            .await()

        product.documents.forEach { document ->
            document.reference.delete().await()
        }
    }

    suspend fun updateProduct(pId: String, pName: String) {
        val currentTime = Timestamp.now()
        val fieldMap = mapOf("insertTime" to currentTime)
        val productSnapshot = getCollection("Product")
            .whereEqualTo("ID", pId)
            .whereEqualTo("productName", pName)
            .get()
            .await()

        productSnapshot.documents.forEach { document ->
            document.reference.update(fieldMap).await()
        }
    }

    suspend fun setMyFavorit(pIdx: String) {
        val userSnapshot = getFirestore().collection("UserInfo").whereEqualTo("id", thisUser).get().await()
        userSnapshot.documents.forEach { document ->
            document.reference.update("favorit", FieldValue.arrayUnion(pIdx)).await()
        }
    }

    suspend fun getImage(productId: String, productName:String, productCount: Int): List<String> {
        val imgList = mutableListOf<String>()
        if (productCount <= 0) {
            imgList.add("basic_img.png")
        } else {
            for (i in 0 until productCount) {
                val word = "${productId}_${productName}_${i}_IMAGE_.png"
                imgList.add(word)
            }
        }
        return imgList
    }

    suspend fun getAdverImage(adverCount: Int): List<String> {
        val adverImgList = mutableListOf<String>()
        if (adverCount <= 0) {
            adverImgList.add("basic_img.png")
        } else {
            for (i in 0 until adverCount) {
                val word = "adver_${i}.jpeg"
                adverImgList.add(word)
            }
        }
        return adverImgList
    }

    suspend fun getAllChatroomCount(): Int {
        val chatrooms = getFirestore().collection("Chatroom").get().await()
        return chatrooms?.documents?.size ?: 0
    }

    suspend fun searchChat(yourId: String): Response {
        val response = Response()
        val chatSnapshot = getFirestore().collection("Chatroom").get().await() // using Kotlin coroutines' await() function

        for (document in chatSnapshot.documents) {
            val id = document.getString("id") ?: continue

            if (id.contains(yourId)) { // Assuming you want to match with the given yourId
                response.searchChat = response.searchChat?.plus(document) ?: listOf(document)
            }
        }
        if (response.searchChat == null) {
            // No matches found
        }
        return response
    }

    suspend fun createChatroom(chatroom: ChatroomData) {
        getFirestore().collection("Chatroom").add(chatroom).await()
    }

    suspend fun whereMyUser(location: String, userId: String) {
        val dbRef = getFirestore().collection("UserInfo")
        val documents = dbRef.whereEqualTo("id", userId).get().await()
        for (document in documents) {
            val update = mapOf("whereUser" to location)
            dbRef.document(document.id).set(update, SetOptions.merge()).await()
        }
    }

    suspend fun isProductExistForUser(myId: String): Boolean {
        val productSnapshot = getFirestore().collection("Product").whereEqualTo("ID", myId).get().await()

        return productSnapshot.documents.isNotEmpty()
    }
}
