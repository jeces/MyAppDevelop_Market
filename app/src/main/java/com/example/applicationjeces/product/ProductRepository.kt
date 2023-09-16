package com.example.applicationjeces.product

import android.net.Uri
import android.util.Log
import com.example.applicationjeces.chat.ChatroomData
import com.example.applicationjeces.search.FilterCriteria
import com.example.applicationjeces.user.Review
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class ProductRepository {

    private var jecesfirestore: FirebaseFirestore? = null
    val thisUser: String by lazy { FirebaseAuth.getInstance().currentUser?.email ?: "" }
    private val storageDB = FirebaseStorage.getInstance().reference
    private val storageRef = storageDB.child("adverhome/")

    // 데이터베이스 접근
    private fun getFirestore(): FirebaseFirestore {
        return jecesfirestore ?: FirebaseFirestore.getInstance().also {
            jecesfirestore = it
        }
    }

    /**
     * 채팅 수 카운트(아이콘 바꾸기)
     */
    fun getUnreadMessagesCount(userId: String): Flow<Int> = callbackFlow {
        val listener = getFirestore().collection("Chatroom")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    close(e) // 에러 발생 시 Flow 종료
                    return@addSnapshotListener
                }

                val hasUnreadMessages = snapshots?.documents?.any {
                    val n0Value = it.getString("n0") ?: ""
                    val n1Value = it.getString("n1") ?: ""

                    val n0Number = n0Value.split("/").lastOrNull()?.toIntOrNull() ?: 0
                    val n1Number = n1Value.split("/").lastOrNull()?.toIntOrNull() ?: 0
                    Log.d("111111111", "${n0Value}/${userId}")
                    Log.d("1111111112", "${n1Value}/${n0Value.startsWith(userId)}")
                    Log.d("1111111113", "${n0Number}/${n1Number}")
                    (n0Value.startsWith(userId) && n0Number > 0) || (n1Value.startsWith(userId) && n1Number > 0)
                } ?: false

                val count = if (hasUnreadMessages) 1 else 0

                Log.d("111111111", "${count}")
                trySend(count) // Flow에 count 전달
            }

        awaitClose { listener.remove() } // Flow 종료 시 listener 제거
    }

    /**
     * 이미지 업로드
     */
    suspend fun uploadImage(index: Int, productName: String, myId: String, uri: Uri): Boolean {
        val imgFileName = "${myId}_${index}_IMAGE_.png"
        val storageRef = storageDB.child("${myId}/${productName}/").child(imgFileName)
        return try {
            storageRef.putFile(uri).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 판매자 이미지
     */
    suspend fun fetchSellerImage(pId: String): String? {
        return try {
            storageDB.child("${pId}/profil/${pId}_Profil_IMAGE_.png").downloadUrl.await().toString()
        } catch (e: Exception) {
            try {
                storageDB.child("basic_user.png").downloadUrl.await().toString()
            } catch (innerException: Exception) {
                null
            }
        }
    }


    /**
     * 상품이름 중복검사
     */
    suspend fun checkProductNameExists(myId: String, productName: String): Boolean {
        Log.d("asdasdasd", "${myId}/${productName}")
        val querySnapshot = getFirestore().collection("Product")
            .whereEqualTo("ID", myId)
            .whereEqualTo("productName", productName)
            .get().await()

        return !querySnapshot.isEmpty
    }



    fun getImageUrl(itemId: String, productName: String, productImgUrl: String, productCount: String): Task<Uri> {
        val imageUrl = if (productCount == "0") {
            "basic_img.png"
        } else {
            "${itemId}/${productName}/${productImgUrl}"
        }
        return storageDB.child(imageUrl).downloadUrl
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

    suspend fun allProduct(): List<DocumentSnapshot> {
        return getCollection("Product").get().await().documents
    }

    suspend fun mySetProduct(id: String): List<DocumentSnapshot> {
        val query = if (id.isBlank()) {
            getFirestore().collection("Product").whereEqualTo("ID", thisUser)
        } else {
            getFirestore().collection("Product").whereEqualTo("ID", id)
        }

        return query.get().await().documents
    }

    suspend fun mySetProductFv(): List<DocumentSnapshot> {
        val userDocuments = jecesfirestore!!.collection("UserInfo").whereEqualTo("id", thisUser).get().await()
        val favoritList = userDocuments.documents[0].get("favorit") as List<String>
        return jecesfirestore!!.collection("Product").whereIn("IDX", favoritList).get().await().documents
    }

    suspend fun addProducts(product: Product, addressSet: String) {
        val currentTime = Timestamp.now()
        val userSnapshot = getFirestore().collection("UserInfo").whereEqualTo("id", product.product_id).get().await()
//        val userName = userSnapshot.documents.firstOrNull()?.getString("name") ?: ""
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
            "insertTime" to currentTime,
            "tags" to product.tags,
            "category" to product.category,
            "product_state" to product.state,
            "address" to addressSet
            // ... (remaining fields as earlier)
        )
        getFirestore().collection("Product").add(productMap).await()
    }

    suspend fun updateProducts(product: UpdateProduct, oldProductName: String) {
        // 여기서 제품 ID를 얻습니다. 이 ID는 문서를 정확히 지정하는 데 사용됩니다.
        // 이 예제에서는 "IDX" 필드를 사용했는데, 실제 데이터 구조에 따라 적절히 조정해야 합니다.
        val productID = "${product.product_id}_${product.product_name}"
        val productOldId = "${product.product_id}_${oldProductName}"
        Log.d("ahgahrahrah", productOldId)
        // 업데이트할 필드만 지정합니다.
        val productMap = mapOf(
            "IDX" to productID,
            "productName" to product.product_name,
            "productPrice" to product.product_price,
            "productDescription" to product.product_description,
            "productCount" to product.product_count,
            "productImgUrl" to product.product_img_url,
            "tags" to product.tags,
            "category" to product.category
            // ... (다른 업데이트할 필드들)
        )

        val productSnapshot = getCollection("Product")
            .whereEqualTo("IDX", productOldId)
            .get()
            .await()

        productSnapshot.documents.forEach { document ->
            document.reference.update(productMap).await()
        }
    }


//    suspend fun searchProducts(searchName: String? = null, filterCriteria: FilterCriteria? = null): Response {
//        return try {
//            val productSearch = getCollection("Product").get().await()
//            val response = Response()
//
//            for (snapshot in productSearch) {
//                val productName = snapshot.getString("productName")
//                val productPrice = snapshot.getLong("price")?.toInt() ?: 0 // 예시로 가격을 사용. 필요에 따라 수정
//                Log.d("afafafaf", "${productPrice.toString()}/${filterCriteria?.minPrice}/${filterCriteria?.maxPrice}")
//                if (productName?.contains(searchName ?: "") == true &&
//                    (filterCriteria?.minPrice == null || productPrice >= filterCriteria.minPrice) &&
//                    (filterCriteria?.maxPrice == null || productPrice <= filterCriteria.maxPrice)
//                // 필요한 다른 필터 조건들도 여기에 추가...
//                ) {
//                    if (response.products == null) {
//                        response.products = listOf(snapshot)
//                    } else {
//                        response.products = response.products?.plus(listOf(snapshot))
//                    }
//                }
//            }
//
//            response
//        } catch (e: Exception) {
//            throw CancellationException("Task was cancelled")
//        }
//    }

    suspend fun searchProducts(searchName: String? = null, filterCriteria: FilterCriteria? = null): Response {
        return try {
            val productSearch = getCollection("Product").get().await()
            val response = Response()

            for (snapshot in productSearch) {
                val productName = snapshot.getString("productName")
                val productPrice = snapshot.getLong("productPrice")?.toInt() ?: 0
                Log.d("afafafaf", "${productName.toString()}/${productPrice.toString()}/${filterCriteria?.minPrice}/${filterCriteria?.maxPrice}")
                if (productName?.contains(searchName ?: "") == true &&
                    (filterCriteria?.minPrice == null || productPrice >= filterCriteria.minPrice) &&
                    (filterCriteria?.maxPrice == null || productPrice <= filterCriteria.maxPrice)
                // 필요한 다른 필터 조건들도 여기에 추가...
                ) {
                    if (response.products == null) {
                        response.products = listOf(snapshot)
                    } else {
                        response.products = response.products?.plus(listOf(snapshot))
                    }
                }
            }

            response
        } catch (e: Exception) {
            throw CancellationException("Task was cancelled")
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
        try {
            val productSnapshot = getCollection("Product")
                .whereEqualTo("ID", pId)
                .whereEqualTo("productName", pName)
                .get()
                .await()

            productSnapshot.documents.forEach { document ->
                document.reference.update(fieldMap).await()
            }
        } catch (e: Exception) {
            // Log the error or print it
            Log.e("BID_CHANGE_ERROR", "Failed to change bid: ", e)
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
        val productSnapshot = getFirestore().collection("Product").whereEqualTo("IDX", pIdx).get().await()
        productSnapshot.documents.forEach { document ->
            // 기존의 pHeartCount 값
            val currentHeartCount = document.getLong("pHeartCount")?.toInt() ?: 0
            // pHeartCount 값을 증가시킨 다음 업데이트
            document.reference.update("pHeartCount", currentHeartCount + 1).await()
        }
    }

    suspend fun getImage(productId: String, productName:String, productCount: Int): List<String> {
        val imgList = mutableListOf<String>()
        if (productCount <= 0) {
            imgList.add("basic_img.png")
        } else {
            for (i in 0 until productCount) {
                val word = "${productId}_${i}_IMAGE_.png"
                imgList.add(word)
            }
        }
        return imgList
    }

    suspend fun fetchAdverCounts(): Int {
        return try {
            val listResult = storageRef.listAll().await()
            listResult.items.size
        } catch (e: Exception) {
            0 // 또는 다른 오류 처리
        }
    }

    suspend fun getAdverImage(adverCount: Int): List<String> {
        val adverImgList: MutableList<String> = mutableListOf()

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

    suspend fun isProductExistForUser(myId: String, pidx: String): Boolean {
        val productSnapshot = getFirestore().collection("UserInfo").whereEqualTo("id", myId).get().await()

        // 일치하는 문서가 있다면
        if (productSnapshot.documents.isNotEmpty()) {
            Log.d("adadadadad", pidx)
            val userDocument = productSnapshot.documents[0]  // 첫 번째 문서 선택
            val favoritList = userDocument.get("favorit") as? List<String> ?: listOf()  // 'favorit' 리스트 가져오기

            // 가져온 'favorit' 리스트에서 pId가 있는지 확인
            return pidx in favoritList
        }

        // 일치하는 문서가 없을 경우
        return false
    }

    suspend fun removeMyFavorit(pIdx: String) {
        val userSnapshot = getFirestore().collection("UserInfo").whereEqualTo("id", thisUser).get().await()
        userSnapshot.documents.forEach { document ->
            document.reference.update("favorit", FieldValue.arrayRemove(pIdx)).await()
        }
        val productSnapshot = getFirestore().collection("Product").whereEqualTo("IDX", pIdx).get().await()
        productSnapshot.documents.forEach { document ->
            // 기존의 pHeartCount 값을 가져옵니다.
            val currentHeartCount = document.getLong("pHeartCount")?.toInt() ?: 0

            // pHeartCount 값이 0보다 큰 경우에만 감소시키고 업데이트합니다.
            if (currentHeartCount > 0) {
                document.reference.update("pHeartCount", currentHeartCount - 1).await()
            }
        }
    }

    /**
     * 최신 10개 Product 가져오기
     */
    suspend fun fetchRecentTenProducts(): List<DocumentSnapshot> {
        return getCollection("Product")
            .orderBy("insertTime", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .await()
            .documents
    }

    /**
     * Manay heartProduct 가져오기
     */
    suspend fun getProductsSortedByHeartCount(): List<DocumentSnapshot> {
        return getCollection("Product")
            .orderBy("pHeartCount", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
    }

    /**
     * Manay viewProduct 가져오기
     */
    suspend fun getProductsSortedByViewCount(): List<DocumentSnapshot> {
        return getCollection("Product")
            .orderBy("pViewCount", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
    }

    /**
     * 최근 hearProduct 가져오기
     */
    suspend fun getRecentProductHeartCount(): List<DocumentSnapshot> {
        val oneWeekAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
        }.time
        return getCollection("Product")
            .whereGreaterThanOrEqualTo("insertTime", Timestamp(oneWeekAgo))
            .orderBy("insertTime")
            .orderBy("pHeartCount", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
    }

    /**
     * 최근 viewProduct 가져오기
     */
    suspend fun getRecentProductViewCount(): List<DocumentSnapshot> {
        val oneWeekAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
        }.time
        return getCollection("Product")
            .whereGreaterThanOrEqualTo("insertTime", Timestamp(oneWeekAgo))
            .orderBy("insertTime")
            .orderBy("pViewCount", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents

    }

    /**
     * 나의 판매 상품 수
     */
    suspend fun getMyProductCellCount(): Int {
        return try {
            val listResult = getCollection("Product").whereEqualTo("ID", thisUser).get().await()
            listResult.size()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * 나의 찜한 상품 수
     */
    suspend fun getMyProductHeartCount(): Int {
        return try {
            val listResult = getCollection("UserInfo").whereEqualTo("id", thisUser).get().await()
            val firstDocument = listResult.documents.firstOrNull() ?: return 0
            val favoritList = firstDocument.get("favorit") as? List<*>
            favoritList?.size ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * 나의 닉네임
     */
    suspend fun fetchUserName(id: String): String? {
        return try {
            val query = if (id.isBlank()) {
                getCollection("UserInfo").whereEqualTo("id", thisUser)
            } else {
                getCollection("UserInfo").whereEqualTo("id", id)
            }
            val userQuery = query.get().await()
            val firstDocument = userQuery.documents.firstOrNull() ?: return null
            firstDocument.getString("name")
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 상품 상대 닉네임
     */
    suspend fun fetchProductNickName(pId: String): String? {
        return try {
            val userQuery = getCollection("UserInfo").whereEqualTo("id", pId).get().await()
            val firstDocument = userQuery.documents.firstOrNull() ?: return null
            firstDocument.getString("name")
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 입찰가
     */
    fun listenForBidUpdates(pId: String, pName: String): Flow<String> = callbackFlow {
        val dbRef = getCollection("Product")
        val listenerRegistration = dbRef.whereEqualTo("ID", pId).whereEqualTo("productName", pName)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    // Handle error or close the flow if necessary
                    close(exception)
                    return@addSnapshotListener
                }
                val product = snapshot?.documents?.firstOrNull()
                val bidPrice = product?.getString("productBidPrice") ?: ""
                trySend(bidPrice).isSuccess // send value to the flow
            }

        // Clean up
        awaitClose {
            listenerRegistration.remove()
        }
    }

    suspend fun addReviews(review: Review) {
        try {
            val dbRef = getCollection("Review")
            dbRef.add(review).await()
        } catch (e: Exception) {

        }
    }

    suspend fun getReviewsForProduct(pId: String): List<DocumentSnapshot>? {
        return getCollection("Review")
            .whereEqualTo("to", pId)
            .get()
            .await()
            .documents
    }
}
