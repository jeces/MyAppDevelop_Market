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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/* 뷰모델은 DB에 직접 접근하지 않아야함. Repository 에서 데이터 통신 */
class ProductViewModel_bak(application: Application): AndroidViewModel(application) {

    /* 각종 라이브데이터 DocumentSnapshot은 firestore와 연결되어있어서 firestore가 변경되면 변경됨 하지만 다른것들은 바꿔줘야함. Snapshot으로 최대한 뽑아보자 */
    var liveTodoData = MutableLiveData<List<DocumentSnapshot>>()
    var productArrayList: MutableList<Product> = ArrayList()

    /* 나의 판매목록 */
    var myProductLiveTodoData = MutableLiveData<List<DocumentSnapshot>>()
    /* 나의 구매목록 */
    var myProductFvLiveTodoData = MutableLiveData<List<DocumentSnapshot>>()

    /* 전체 채팅룸 카운트 */
    var liveTodoChatroomDataCount : Int = 0

    var jecesfirestore: FirebaseFirestore? = null
    var thisUser: String? = null
    var position: Int = 0

    /* firestore 문서 id를 저장하는 곳 */
    var documentId : String? = null

    /* 이미지를 담는 리스트 */
    var imgList: ArrayList<String> = arrayListOf()

    /* adver 이미지를 담는 리스트 */
    var adverImgList: ArrayList<String> = arrayListOf()

    /* adver 개수 카운트 */
    var adverCount: Int = 0

    private var currentPage = 0
    private val itemsPerPage = 15  // 이는 한 페이지에 표시될 항목 수입니다.

    init {
        /* firebase 연동 */
        jecesfirestore = FirebaseFirestore.getInstance()
        /* 현재 로그인 아이디 */
        thisUser = FirebaseAuth.getInstance().currentUser?.email.toString()

        /* firebase product 전체 가져오기 */
        /* https://velog.io/@nagosooo/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-TodoList%EC%95%B1-%EB%A7%8C%EB%93%A4%EA%B8%B0 */
        allProduct()
        Log.d("전체상품", liveTodoData.value.toString())
    }

    /**
     * 페이징 메서드
     */
    fun fetchNextPage(): LiveData<List<DocumentSnapshot>> {
        val nextPageLiveData = MutableLiveData<List<DocumentSnapshot>>()

        // 현재 페이지에 따라 데이터를 가져오는 쿼리를 구성합니다.
        jecesfirestore!!.collection("Product")
            .orderBy("someField")  // 여기서 "someField"는 데이터를 정렬하는 필드를 나타냅니다.
            .startAfter(currentPage * itemsPerPage)
            .limit(itemsPerPage.toLong())
            .get()
            .addOnSuccessListener { documents ->
                nextPageLiveData.value = documents.documents
                currentPage++
            }
            .addOnFailureListener { exception ->
                // 오류 처리를 해야 합니다.
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
                println("Number of files in the folder: $filesCount")
                result.value = filesCount
            }
            .addOnFailureListener { e ->
                // handle any errors here
                println("Error occurred: ${e.message}")
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
        return if(productCount <= 0) {
            var word = "basic_img.png"
            imgList.add(word)
            imgList
        } else {
            for(i: Int in 0 until productCount) {
                /* 워드를 가져와서 돌림 */
                var word: String = productId + "_" + productName + "_" + i + "_IMAGE_.png"
                imgList.add(word)
            }
            imgList
        }
    }

    /**
     *  firebase storage에서 adver 이미지 가져오기
     * */
    fun getAdverImage(adverCount: Int): MutableList<String>? {
        adverImgList.clear()
        /* 글자 나누기 */
        /* 카운트는 가져와야함 product에 저장해놓고 */
        /* User이름, 상품이름, 사진갯수몇가지인지[product에 추가할것], 사진idx값 가져오기 */
        return if(adverCount <= 0) {
            var word = "basic_img.png"
            adverImgList.add(word)
            adverImgList
        } else {
            for(i: Int in 0 until adverCount) {
                /* 워드를 가져와서 돌림 */
                var word: String = "adver_" + i + ".jpeg"
                adverImgList.add(word)
            }
            adverImgList
        }
    }

    /**
     * 전체 채팅 수 가져오기
     * */
    fun getAllChatroomCount() {
        /* 어떻게 가져올껀지 찾아야한다. */
        jecesfirestore!!.collection("Chatroom").addSnapshotListener { chatrooms, e->
            if (e != null) {
                return@addSnapshotListener
            }
            var count = 0
            for(snapshot in chatrooms!!.documents) {
                count++
            }
            liveTodoChatroomDataCount = count
        }
    }

    /**
     *  Product 전체 가져오기
     *  1. 가입된 아이디 전체 검색
     *  2. 가입된 아이디 전체로 상품 전체 가져오기
     **/
    fun allProduct() {
        jecesfirestore!!.collection("Product").addSnapshotListener { products, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            liveTodoData.value = products?.documents
        }
    }

    /**
     * 나의 판매 상품 가져오기
     */
    fun mySetProduct() {
        jecesfirestore!!.collection("Product").whereEqualTo("ID", thisUser).addSnapshotListener { product, e ->
            if (e != null) {
                return@addSnapshotListener
            }
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

    /* 시간변환 */
    fun changeTime(timestamp: Timestamp): String {
        val mils = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
//        val sf = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA)
        val sf = SimpleDateFormat("aa hh:mm", Locale.KOREA)
        val nDate = Date(mils)
        val date = sf.format(nDate).toString()
        return date
    }

    /* firebase Product 입력 */
    fun addProducts(product: Product) {
        val currentTime = Timestamp.now()
        val products = hashMapOf(
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
        jecesfirestore!!.collection("Product").add(products)
            .addOnSuccessListener {
                // 성공할 경우
                Log.w("PRODUCT 데이터 입력 성공", "Error getting documents")
            }
            .addOnFailureListener { exception ->
                // 실패할 경우
                Log.w("PRODUCT 데이터 입력 실패", "Error getting documents")
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
        jecesfirestore!!.collection("Product").get().addOnCompleteListener  { productSearch ->
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
    fun setProductDetail(productId : String, productName: String, productPrice: Int, productDescription: String, productCount: String, pChatCount: Int, pViewCount: Int, pHearCount: Int, pBidPrice: String, insertTime: String, getPosition: Int) {
//        productArrayList.clear()
//        val productDetail = Product(productId, productName, productPrice, productDescription, productCount.toInt(), thisUser + "_" + productName + "_0_IMAGE_.png", pChatCount, pViewCount, pHearCount, pBidPrice, insertTime)
//        position = getPosition
//        productArrayList.add(productDetail)
//        Log.d("aa11", productArrayList.toString() + "aaaaa")
    }

    /**
     * adver 사진을 가지고
     */


    /* 나의 위치 이동 */
    fun whereMyUser(where : String) {
        val dbRef = jecesfirestore!!.collection("UserInfo")
        dbRef.whereEqualTo("id", thisUser).get().addOnCompleteListener { chat ->
            for(document in chat.result) {
                val update: MutableMap<String, Any> = HashMap()
                update["whereUser"] = where
                dbRef.document(document.id).set(update, SetOptions.merge())
            }
        }
    }

    /**
     * 채팅목록 찾기
     */
    fun searchChat(yourId: String) : MutableLiveData<Response> {
        val chatSearchLiveTodoData = MutableLiveData<Response>()
        /* 전체 채팅 수 가져오기 */
        Log.d("ㅁㅁㅁㅁ", "ㅁㅁㄴㅇ")
        getAllChatroomCount()
        jecesfirestore!!.collection("Chatroom").get().addOnCompleteListener { chat ->
            val response = Response()
            var flags = false
            for(document in chat.result) {
                /* 목록 찾기 */
                if(document.getString("id")!!.contains(thisUser.toString()) && document.getString("id")!!.contains(yourId)) {
                    /* 리턴을 해주기 */
                    document?.let {
                        if(response.searchChat == null) {
                            response.searchChat = listOf(it)
                        } else {
                            response.searchChat = response.searchChat?.plus(listOf(it))
                        }
                    }
                    chatSearchLiveTodoData.value = response
                    flags = true
                    break
                }
            }
            if(flags == false) {
                /* 목록 없으면 null 값 넣기 */
                response.searchChat = null
                chatSearchLiveTodoData.value = response
            }
        }
        return chatSearchLiveTodoData
    }

    /**
     * 판매자 프로필 이미지
     **/
//    fun yourProductProfilImg(yourId: String, chatroomUserImg: ImageView) {
//        var db = FirebaseStorage.getInstance()
//        db.reference.child("${yourId}/${yourId}_profil.png").downloadUrl.addOnCompleteListener {
//            if(it.isSuccessful) {
//                Glide.with(contexts)
//                    .load(it.result)
//                    .override(70, 70)
//                    .fitCenter()
//                    .circleCrop() // 또는 .transform(RoundedCorners(radius)) 를 사용하여 모서리의 반경을 설정
//                    .into(chatroomUserImg)
//            } else {
//                /* 없으면 기본 이미지 들고와라 */
//                db.reference.child("basic_user.png").downloadUrl.addOnCompleteListener { its->
//                    Glide.with(contexts)
//                        .load(its.result)
//                        .override(70, 70)
//                        .fitCenter()
//                        .circleCrop() // 또는 .transform(RoundedCorners(radius)) 를 사용하여 모서리의 반경을 설정
//                        .into(chatroomUserImg)
//                }
//            }
//        }
//    }

    /**
     * 채팅방 생성
     */
    fun createChatroom(chatroom : ChatroomData) {
        jecesfirestore!!.collection("Chatroom").add(chatroom)
            .addOnSuccessListener {
                /* 성공 */
                Log.d("데이터 입력 성공", "ㅇㅇ")
            }.addOnFailureListener { exception ->
                /* 실패 */
                Log.w("CHAT 데이터 입력 실패", "Error getting documents")
            }
    }

    /**
     * ViewCount++
     */
    fun viewCountUp(pId: String, pName: String) {
        var dbRef = jecesfirestore!!.collection("Product")
        dbRef.whereEqualTo("ID", pId).whereEqualTo("productName", pName).get().addOnCompleteListener { product ->
            if(product.isSuccessful) {
                for(document in product.result) {
                    var viewCount = document.getString("pViewCount").toString()
                    val update: MutableMap<String, Any> = HashMap()
                    update["pViewCount"] = (viewCount.toInt() + 1).toString()
                    dbRef.document(document.id).set(update, SetOptions.merge())
                }
            }
        }
    }

    /**
     * 입찰하기
     * */
    fun bidchange(pId: String, pName: String, bidPrice: String) {
        var dbRef = jecesfirestore!!.collection("Product")
        dbRef.whereEqualTo("ID", pId).whereEqualTo("productName", pName).get()
            .addOnCompleteListener { product ->
                if (product.isSuccessful) {
                    for (document in product.result) {
                        val update: MutableMap<String, Any> = HashMap()
                        update["productBidPrice"] = bidPrice
                        dbRef.document(document.id).set(update, SetOptions.merge())
                    }
                }
            }
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
        var dbRef = jecesfirestore!!.collection("Product")
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
        val dbRef = jecesfirestore!!.collection("Product")
        dbRef.whereEqualTo("ID", pId).whereEqualTo("productName", pName).get()
            .addOnCompleteListener { product ->
                if (product.isSuccessful) {
                    for (document in product.result) {
                        // 현재 시간으로 설정
                        val currentTime = Timestamp.now()

                        val update: MutableMap<String, Any> = HashMap()
                        update["insertTime"] = currentTime

                        dbRef.document(document.id).set(update, SetOptions.merge())
                            .addOnSuccessListener {
                                Log.d("Firestore", "Document time successfully updated!")
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error updating document time", e)
                            }
                    }
                } else {
                    Log.e("Firestore", "Error getting product for update", product.exception)
                }
            }
    }

    fun setMyFavorit(pIdx: String) {
        val favorites = listOf("${pIdx}")
        Log.d("aaaaa123", "${pIdx}")
        val dbRef = jecesfirestore!!.collection("UserInfo").whereEqualTo("id", "${thisUser}")
        dbRef.get().addOnSuccessListener { result ->
            for (document in result) {
                document.reference.update("favorit", FieldValue.arrayUnion(pIdx))
            }
        }
    }
}