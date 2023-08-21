package com.example.applicationjeces.product

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.applicationjeces.chat.ChatroomData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlin.collections.ArrayList

/* 뷰모델은 DB에 직접 접근하지 않아야함. Repository 에서 데이터 통신 */
class ProductViewModel_bak_2(application: Application): AndroidViewModel(application) {

    /* 각종 라이브데이터 DocumentSnapshot은 firestore와 연결되어있어서 firestore가 변경되면 변경됨 하지만 다른것들은 바꿔줘야함. Snapshot으로 최대한 뽑아보자 */
    var liveTodoData: MutableLiveData<List<DocumentSnapshot>> = MutableLiveData(emptyList())
    var productArrayList: MutableList<Product> = mutableListOf()

    /* 나의 판매, 관심 목록 */
    var myProductLiveTodoData = MutableLiveData<List<DocumentSnapshot>>()
    var myProductFvLiveTodoData = MutableLiveData<List<DocumentSnapshot>>()

    /* 전체 채팅룸 카운트 */
    var liveTodoChatroomDataCount : Int = 0

    /* firebase 연동 */
    private var jecesfirestore: FirebaseFirestore? = null
    /* 현재 로그인 아이디 */
    val thisUser: String by lazy { FirebaseAuth.getInstance().currentUser?.email ?: "" }
    private var position: Int = 0

    /* firestore 문서 id를 저장하는 곳 */
    private var documentId : String? = null

    /* 이미지를 담는 리스트 */
    private var imgList: ArrayList<String> = arrayListOf()

    /* adver 이미지를 담는 리스트 */
    private var adverImgList: ArrayList<String> = arrayListOf()

    /* adver 개수 카운트 */
    private var adverCount: Int = 0

    private var currentPage = 0
    private val itemsPerPage = 15  // 이는 한 페이지에 표시될 항목 수입니다.

    init {
        /* firebase product 전체 가져오기 */
        /* https://velog.io/@nagosooo/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-TodoList%EC%95%B1-%EB%A7%8C%EB%93%A4%EA%B8%B0 */
        allProduct()
    }

    /**
     * 데이터베이스
     */
    private fun getFirestore(): FirebaseFirestore {
        return jecesfirestore ?: FirebaseFirestore.getInstance().also {
            jecesfirestore = it
        }
    }

    /**
     * 나의 아이디
     */
    private fun getCurrentUserEmail(): String {
        return thisUser ?: FirebaseAuth.getInstance().currentUser?.email ?: ""
    }

    /**
     * 중복 컬렉션
     */
    private fun getCollection(collectionName: String) = getFirestore().collection(collectionName)

    /**
     * 중복 다큐먼트
     */
    private fun updateDocument(collection: String, fieldMap: Map<String, Any>, pId: String, pName: String) {
        getCollection(collection)
            .whereEqualTo("ID", pId)
            .whereEqualTo("productName", pName)
            .get()
            .addOnSuccessListener { result ->
                result.forEach { document ->
                    document.reference.update(fieldMap)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Document successfully updated!")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error updating document", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error getting document for update", e)
            }
    }

    /**
     * 페이징 메서드
     */
    fun fetchNextPage(): LiveData<List<DocumentSnapshot>> {
        val nextPageLiveData = MutableLiveData<List<DocumentSnapshot>>()

        getCollection("Product")
            .orderBy("someField")
            .startAfter(currentPage * itemsPerPage)
            .limit(itemsPerPage.toLong())
            .get()
            .addOnSuccessListener { documents ->
                nextPageLiveData.value = documents.documents
                currentPage++
            }
            .addOnFailureListener { exception ->
                Log.e("ProductViewModel", "Error fetching next page", exception)
            }

        return nextPageLiveData
    }

    /**
     * adver Count
     */
    fun getAdverCounts(): LiveData<Int> {
        val result = MutableLiveData<Int>()
        val storageRef = FirebaseStorage.getInstance().reference.child("adverhome/")

        storageRef.listAll()
            .addOnSuccessListener { listResult ->
                val filesCount = listResult.items.size
                Log.d("AdverCounts", "Number of files in the folder: $filesCount")
                result.value = filesCount
            }
            .addOnFailureListener { e ->
                Log.e("AdverCounts", "Error occurred: ${e.message}", e)
            }
        return result
    }

    /**
     *  firebase storage에서 상품 이미지 가져오기
     * */
    fun getImage(productId: String, productName:String, productCount: Int): MutableList<String>? {
        imgList.clear()
        /* 글자 나누기 */
        /* 카운트는 가져와야함 product에 저장해놓고 */
        /* User이름, 상품이름, 사진갯수몇가지인지[product에 추가할것], 사진idx값 가져오기 */
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

    /**
     *  firebase storage에서 adver 이미지 가져오기
     * */
    fun getAdverImage(adverCount: Int): MutableList<String>? {
        adverImgList.clear()
        /* 글자 나누기 */
        /* 카운트는 가져와야함 product에 저장해놓고 */
        /* User이름, 상품이름, 사진갯수몇가지인지[product에 추가할것], 사진idx값 가져오기 */
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

    /**
     * 전체 채팅 수 가져오기
     * */
    fun getAllChatroomCount() {
        jecesfirestore!!.collection("Chatroom").addSnapshotListener { chatrooms, e ->
            if (e != null) {
                return@addSnapshotListener
            }

            liveTodoChatroomDataCount = chatrooms?.documents?.size ?: 0
        }
    }

    /**
     *  Product 전체 가져오기
     *  1. 가입된 아이디 전체 검색
     *  2. 가입된 아이디 전체로 상품 전체 가져오기
     **/
    fun allProduct() {
        getCollection("Product").addSnapshotListener { products, e ->
            e?.let { return@addSnapshotListener }
            liveTodoData.value = products?.documents
        }
    }

    /**
     * 나의 판매 상품 가져오기
     */
    fun mySetProduct() {
        getFirestore().collection("Product").whereEqualTo("ID", getCurrentUserEmail())
            .addSnapshotListener { product, e ->
                e?.let { return@addSnapshotListener }
                myProductLiveTodoData.value = product?.documents
            }
    }

    /**
     * 나의 관심 상품 가져오기
     */
    fun mySetProductFv() {
        // 1. 현재 사용자의 'favorit' 목록 가져오기
        jecesfirestore!!.collection("UserInfo").whereEqualTo("id", thisUser).get()
            .addOnSuccessListener { userDocuments ->
                if (userDocuments.isEmpty) {
                    return@addOnSuccessListener
                }
                val favoritList = userDocuments.documents[0].get("favorit") as List<String>

                // 2. 해당 'favorit' 목록을 사용하여 'Product'에서 제품 가져오기
                jecesfirestore!!.collection("Product").whereIn("IDX", favoritList).addSnapshotListener { productSnapshot, e ->
                    if (e != null) {
                        return@addSnapshotListener
                    }
                    Log.d("aaaa123123", "aaasdasdasd")
                    myProductFvLiveTodoData.value = productSnapshot?.documents
                }
            }
            .addOnFailureListener {
                // 처리할 오류가 있을 때 이곳에 코드를 추가
            }
    }

    /**
     * firebase Product 입력
     * */
    fun addProducts(product: Product) {
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
        getFirestore().collection("Product").add(productMap)
            .addOnSuccessListener {
                Log.d("Firestore", "PRODUCT 데이터 입력 성공")
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "PRODUCT 데이터 입력 실패", exception)
            }
    }

    /* firebase 검색 */
    /* firestore에서는 like를 사용못함 */
    /* 비동기 앱의 문제점 */
    /* observer 기능을 사용하기 위해 데이터 하나를 수정해줌 */
    /* firesotre가 제일 늦게 반응해 그다음 검색 때 바뀜 */
    /* 서치뷰에 suspend를 쓸수가없음 override 고정되어있어서 await 못씀 */
    /* 아래 서치뷰 검색을 씀 */
    /* 서치뷰 검색어로 검색 */
    fun searchProductsCall(searchName: String)  : MutableLiveData<Response> {
        val searchLiveTodoData = MutableLiveData<Response>()
        getFirestore().collection("Product").get().addOnCompleteListener  { productSearch ->
            val response = Response()
            for (snapshot in productSearch.result) {
                /* 검색했을 때 있다면 리스트 넣기 */
                if (snapshot.getString("productName")!!.contains(searchName)) {
                    snapshot?.let {
                        if(response.products == null) {
                            response.products = listOf(it)
                        } else {
                            response.products = response.products?.plus(listOf(it))
                        }
                    }
                }
            }
            searchLiveTodoData.value = response
        }
        return searchLiveTodoData
    }

    /**
     *  제품 디테일 데이터를 가지고 있는 데이터
     **/
    fun setProductDetail(
        productId: String,
        productName: String,
        productPrice: String,
        productDescription: String,
        productCount: String,
        pChatCount: String,
        pViewCount: String,
        pHearCount: String,
        pBidPrice: String,
        getPosition: Int
    ) {
        productArrayList.clear()

        val imageUrl = "${thisUser}_${productName}_0_IMAGE_.png"
//        val productDetail = Product(productId, productName, productPrice, productDescription, productCount.toInt(), imageUrl, pChatCount, pViewCount, pHearCount, pBidPrice)
//        position = getPosition
//        productArrayList.add(productDetail)
    }

    /**
     * adver 사진을 가지고
     */


    /**
     *  나의 위치 이동
     **/
    fun whereMyUser(location: String) {
        val dbRef = getFirestore().collection("UserInfo")
        dbRef.whereEqualTo("id", thisUser).get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot.documents) {
                val update = mapOf("whereUser" to location)
                dbRef.document(document.id).set(update, SetOptions.merge())
            }
        }.addOnFailureListener { exception ->
            Log.e("whereMyUser", "Error updating user location", exception)
        }
    }

    /**
     * 채팅목록 찾기
     */
    fun searchChat(yourId: String) : MutableLiveData<Response> {
        val chatSearchLiveData = MutableLiveData<Response>()
        val response = Response()

        getFirestore().collection("Chatroom").get()
            .addOnSuccessListener { chatSnapshot ->
                for (document in chatSnapshot.documents) {
                    val id = document.getString("id") ?: continue

                    if (id.contains(thisUser.toString()) && id.contains(yourId)) {
                        response.searchChat = response.searchChat?.plus(document) ?: listOf(document)
                        chatSearchLiveData.value = response
                        return@addOnSuccessListener
                    }
                }
                // No matches found
                response.searchChat = null
                chatSearchLiveData.value = response
            }
            .addOnFailureListener { exception ->
                Log.e("searchChat", "Error searching chat", exception)
            }

        return chatSearchLiveData
    }

    /**
     * 채팅방 생성
     */
    fun createChatroom(chatroom: ChatroomData) {
        getFirestore().collection("Chatroom").add(chatroom)
            .addOnSuccessListener {
                Log.d("createChatroom", "Successfully added chatroom")
            }
            .addOnFailureListener { exception ->
                Log.w("createChatroom", "Failed to add chatroom", exception)
            }
    }


    /**
     * ViewCount++
     */
    fun viewCountUp(pId: String, pName: String) {
        getCollection("Product")
            .whereEqualTo("ID", pId)
            .whereEqualTo("productName", pName)
            .get()
            .addOnSuccessListener { productSnapshot ->
                productSnapshot.documents.forEach { document ->
                    val viewCount = document.getString("pViewCount")?.toIntOrNull() ?: 0
                    val update = mapOf("pViewCount" to (viewCount + 1).toString())
                    document.reference.set(update, SetOptions.merge())
                }
            }
    }


    /**
     * 입찰하기
     * */
    fun bidchange(pId: String, pName: String, bidPrice: String) {
        val fieldMap = mapOf("productBidPrice" to bidPrice)
        updateDocument("Product", fieldMap, pId, pName)
    }

    /**
     * 체크되어있는지 확인하기
     * 1. pCheckId 만들어서 아이디만 넣고 해당 아이디 검색 후 체크되어있는거 확인
     * 2. 체크되어있으면 checkbox 바꿔줘야함 리턴. response
     **/
    fun isCheckProduct(myId: String) {
        jecesfirestore!!.collection("Product").get().addOnCompleteListener {

        }
    }

    /**
     * 상품 삭제
     */
    fun deleteProduct(pId: String, pName: String) {
        var dbRef = getFirestore().collection("Product")
        dbRef.whereEqualTo("ID", pId).whereEqualTo("productName", pName).get()
            .addOnCompleteListener { product ->
                if (product.isSuccessful) {
                    for (document in product.result) {
                        dbRef.document(document.id).delete()
                            .addOnSuccessListener {
                                Log.d("Firestore", "Document successfully deleted!")
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error deleting document", e)
                            }
                    }
                }
            }
    }

    /**
     * 상품의 시간을 최신으로 업데이트
     */
    fun updateProduct(pId: String, pName: String) {
        val currentTime = Timestamp.now()
        val fieldMap = mapOf("insertTime" to currentTime)
        updateDocument("Product", fieldMap, pId, pName)
    }

    /**
     * 나의 관심목록 Set
     */
    fun setMyFavorit(pIdx: String) {
        getFirestore().collection("UserInfo").whereEqualTo("id", getCurrentUserEmail())
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    document.reference.update("favorit", FieldValue.arrayUnion(pIdx))
                }
            }
    }
}