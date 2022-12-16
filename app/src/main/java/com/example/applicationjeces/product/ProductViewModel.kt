package com.example.applicationjeces.product

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.applicationjeces.chat.ChatData
import com.example.applicationjeces.chat.ChatroomData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/* 뷰모델은 DB에 직접 접근하지 않아야함. Repository 에서 데이터 통신 */
class ProductViewModel(application: Application): AndroidViewModel(application) {


    /* 각종 라이브데이터 DocumentSnapshot은 firestore와 연결되어있어서 firestore가 변경되면 변경됨 하지만 다른것들은 바꿔줘야함. Snapshot으로 최대한 뽑아보자 */
    var liveTodoData = MutableLiveData<List<DocumentSnapshot>>()
    var productArrayList: MutableList<Product> = ArrayList()
    var chatArrayList: MutableList<ChatroomData> = ArrayList()
    var liveTodoChatData = MutableLiveData<List<DocumentSnapshot>>()
    var liveTodoChatroomData = MutableLiveData<List<DocumentSnapshot>?>()

    var jecesfirestore: FirebaseFirestore? = null
    var thisUser: String? = null
    var position: Int = 0

    var imgList: ArrayList<String> = arrayListOf()

    init {
        /* firebase 연동 */
        jecesfirestore = FirebaseFirestore.getInstance()

        /* 현재 로그인 아이디 */
        thisUser = FirebaseAuth.getInstance().currentUser?.email.toString()

        /* firebase product 전체 가져오기 */
        /* https://velog.io/@nagosooo/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-TodoList%EC%95%B1-%EB%A7%8C%EB%93%A4%EA%B8%B0 */
        allProduct()
        allChatroom()

        Log.d("뷰모델초기화0", "ㅇㅇ")
    }

    /* firebase storage에서 이미지 가져오기 */
    fun getImage(productName:String, productCount: Int): MutableList<String>? {
        imgList.clear()
        /* 글자 나누기 */
        /* 카운트는 가져와야함 product에 저장해놓고 */
        /* User이름, 상품이름, 사진갯수몇가지인지[product에 추가할것], 사진idx값 가져오기 */
        return if(productCount <= 0) {
            var word: String = "basic_img.png"
            imgList.add(word)
            imgList
        } else {
            for(i: Int in 0 until productCount) {
                /* 워드를 가져와서 돌림 */
                var word: String = thisUser + "_" + productName + "_" + i + "_IMAGE_.png"
                Log.d("워드", word)
                imgList.add(word)
            }
            imgList
        }
    }

    /* 자신의 채팅목록 전체 가져오기 */
    fun allChatroom() {
        /* 어떻게 가져올껀지 찾아야한다. */
        val response = Response()
        jecesfirestore!!.collection("/Chatroom").addSnapshotListener { chatrooms, e->
            if (e != null) {
                return@addSnapshotListener
            }
            for(snapshot in chatrooms!!.documents) {
                Log.d("아이디", thisUser.toString())
                if(snapshot.getString("id")!!.contains(thisUser.toString())) {
                    snapshot?.let {
                        if(response.products == null) {
                            response.products = listOf(it)

                        } else {
                            response.products = response.products?.plus(listOf(it))
                        }
                    }
                }
            }
            Log.d("뭐지", response.products.toString())
            liveTodoChatroomData.value = response.products
            Log.d("뭐지2", response.products.toString())
        }

//        val response = Response()
//        jecesfirestore!!.collection("/Chatroom").get().addOnCompleteListener() { chatrooms ->
//            for(snapshot in chatrooms.result) {
//                /* 포함된 id가 있으면 */
//                if(snapshot.getString("id")!!.contains(thisUser.toString())) {
//                    snapshot?.let {
//                        if(response.products == null) {
//                            response.products = listOf(it)
//                        } else {
//                            response.products = response.products?.plus(listOf(it))
//                        }
//                    }
//                }
//            }
//            liveTodoChatroomData.value = response
//            Log.d("라이브데이터1", liveTodoChatroomData.value.toString())
//        }
    }

    /* firebase Product 전체 가져오기 */
    fun allProduct() {
        liveTodoData.value?.isEmpty()
        jecesfirestore!!.collection("Product").addSnapshotListener { products, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            liveTodoData.value = products?.documents
        }
    }

    /* Chat comment 생성 */
    fun addChat(chat: ChatData) {
        jecesfirestore!!.collection("Chat").add(chat)
            .addOnSuccessListener {
                // 성공할 경우
                Log.w("CHAT 데이터 입력 성공", "Error getting documents")
            }.addOnFailureListener { exception ->
                // 실패할 경우
                Log.w("CHAT 데이터 입력 실패", "Error getting documents")
            }
    }

    /* Chat 가져오기 */
    fun getChat(idx: String) {
        /* 데이터베이스 담기 */
        jecesfirestore!!.collection("Chat").whereEqualTo("chatroomidx", idx).orderBy("time", Query.Direction.DESCENDING).addSnapshotListener { chat, e ->
            if(e != null) {
                return@addSnapshotListener
            }
            liveTodoChatData.value = chat?.documents
            Log.d("ㅁㄴㅇㄻㄴㅇㄹ", liveTodoChatData.value.toString())
        }
    }

    /* firebase Product 입력 */
    fun addProducts(product: Product) {
        val products = hashMapOf(
            "ID" to thisUser,
            "productName" to product.product_name,
            "productPrice" to product.product_price,
            "productDescription" to product.product_description,
            "productCount" to product.product_count,
            "productImgUrl" to product.product_img_url
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

    /* 채팅 디테일 데이터를 가지고 있는 데이터 */
    fun setChatDetail(chatidx: String, lastcomment: String, myid: String, yourid: String, getPosition: Int) {
        chatArrayList.clear()
        val chatDetail = ChatroomData(chatidx, lastcomment, myid, yourid)
        position = getPosition
        Log.d("눌렀닌?", chatidx)
        chatArrayList.add(chatDetail)
    }

    /* 제품 디테일 데이터를 가지고 있는 데이터 */
    fun setProductDetail(productName: String, productPrice: String, productDescription: String, productCount: String, getPosition: Int) {
        productArrayList.clear()
        val productDetail = Product(0, productName, productPrice, productDescription, productCount.toInt(), thisUser + "_" + productName + "_0_IMAGE_.png")
        position = getPosition
        productArrayList.add(productDetail)
    }
}




