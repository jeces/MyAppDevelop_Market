package com.example.applicationjeces

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.applicationjeces.chat.ChatData
import com.example.applicationjeces.chat.ChatroomData
import com.example.applicationjeces.product.Product
import com.example.applicationjeces.product.Response
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/* 뷰모델은 DB에 직접 접근하지 않아야함. Repository 에서 데이터 통신 */
class JecesViewModel(application: Application): AndroidViewModel(application) {

    /* 각종 라이브데이터 DocumentSnapshot은 firestore와 연결되어있어서 firestore가 변경되면 변경됨 하지만 다른것들은 바꿔줘야함. Snapshot으로 최대한 뽑아보자 */
    var liveTodoData = MutableLiveData<List<DocumentSnapshot>>()
    var productArrayList: MutableList<Product> = ArrayList()

    /* 채팅룸 담을 리스트*/
    var listChatroom : MutableList<ChatroomData> = mutableListOf()
    /* 채팅룸 실시간 라이브 데이터 */
    var liveTodoChatroomData = MutableLiveData<List<ChatroomData>?>()

    /* 채팅 담을 리스트 */
    val listChat : MutableList<ChatData> = mutableListOf()
    /* 채팅 실시간 라이브 데이터 */
    var liveTodoChatDataList = MutableLiveData<List<ChatData>?>()

    /* 전체 채팅룸 카운트 */
    var liveTodoChatroomDataCount : Int = 0

    var jecesfirestore: FirebaseFirestore? = null
    var thisUser: String? = null
    var position: Int = 0

    /* firestore 문서 id를 저장하는 곳 */
    var documentId : String? = null

    /* 이미지를 담는 리스트 */
    var imgList: ArrayList<String> = arrayListOf()

    /* 유저의 현재 위치 */
    var whereUser: String? = null

    /* ishead 변경을 위한 flag*/
    var isheadFlag: String? = null

    init {
        /* firebase 연동 */
        jecesfirestore = FirebaseFirestore.getInstance()
        /* 현재 로그인 아이디 */
        thisUser = FirebaseAuth.getInstance().currentUser?.email.toString()

        /* firebase product 전체 가져오기 */
        /* https://velog.io/@nagosooo/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-TodoList%EC%95%B1-%EB%A7%8C%EB%93%A4%EA%B8%B0 */
        allProduct()
    }

    /* firebase storage에서 이미지 가져오기 */
    fun getImage(productName:String, productCount: Int): MutableList<String>? {
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
                var word: String = thisUser + "_" + productName + "_" + i + "_IMAGE_.png"
                imgList.add(word)
            }
            imgList
        }
    }
    /* 전체 채팅 수 가져오기 n0, n1 가져와야함 */
    /* 채팅창을 칠때마다 실행하는 함수
    *  내채팅칠때 카운트도 해줘야함
    *  1. [채팅을 보낼 때 상대방이 채팅창에 있으면 readcount를 0으로 바꿔주고 없으면 계속 보낼때마다 카운트를 수정해주면 됨]
    *  2. 채팅창에 들어오는 순간 다 읽어짐 => 모두 isread true로 바꾸고 readcount 0으로 해주면 됨
    *
    *  아래 다시 수정할 것
    * 
    *  1. 상대방이 들어오면서 처리 -> readCount와 isread를 전부 읽음처리
    *  2. 상대방이 들어와있을 때 처리(내가 방에서 채팅을 칠 때) -> readCount는 isread는 전부 읽음처리 되어있으므로 다음 들어오는 데이터를 읽음처리
    *
    * * */
    fun updateChatCount(idx: String, yourId: String) {
        /* 스냅샷없애야 함 */
        jecesfirestore!!.collection("UserInfo").whereEqualTo("id", yourId).get()
            .addOnSuccessListener { chat ->
                var dbRef = jecesfirestore!!.collection("Chatroom")
                for (document in chat!!.documents) {
                    whereUser = document.getString("whereUser").toString()
                }
                /* 상대방이 채팅공간에 있으면 readcount = 0 으로 */
                if (whereUser == "chat") {
                    /* 맨마지막 데이터 수정 하기 */
                    dbRef.whereEqualTo("chatidx", idx).get().addOnCompleteListener { chat ->
                        /* 전체 읽음처리 하기[후에 마지막 하나로 바꿔야함] */
                        if (chat.isSuccessful) {
                            for (document in chat.result) {
                                /* 아이디/0 아이디[나]가 상대방의 말을 안읽은 카운트임 */
                                var n0 = document.getString("n0").toString().split("/")
                                var n1 = document.getString("n1").toString().split("/")
                                val update: MutableMap<String, Any> = HashMap()
                                if(n0[0] == thisUser) {
                                    update["n1"] = "${n1[0]}/0"
                                } else {
                                    update["n0"] = "${n0[0]}/0"
                                }
                                dbRef.document(document.id).set(update, SetOptions.merge())
                            }
                        }
                    }
                }
                /* 채팅방에 없으면*/
                else {
                    dbRef.whereEqualTo("chatidx", idx).get().addOnCompleteListener { chat ->
                        if(chat.isSuccessful) {
                            /* 아이디/0 아이디[나]가 상대방의 말을 안읽은 카운트임 */
                            for(document in chat.result) {
                                var n0 = document.getString("n0").toString().split("/")
                                var n1 = document.getString("n1").toString().split("/")
                                val update: MutableMap<String, Any> = HashMap()
                                if(n0[0] == thisUser.toString()) {
                                    update["n1"] = "${n1[0]}/${n1[1].toInt().plus(1)}"
                                } else {
                                    update["n0"] = "${n0[0]}/${n0[1].toInt().plus(1)}"
                                }
                                dbRef.document(document.id).set(update, SetOptions.merge())
                            }
                        }
                    }
                }
            }
    }

    /**
     *  자신의 채팅목록 전체 가져오기
     *  라스트코멘트가 있는 것만 리스트로 가져오기
     *  */
    fun getAllChatroom() {
        /* 어떻게 가져올껀지 찾아야한다. */
        jecesfirestore!!.collection("Chatroom").orderBy("time", Query.Direction.DESCENDING).addSnapshotListener { chatrooms, e->
            if (e != null) {
                return@addSnapshotListener
            }
            listChatroom.clear()
            for(snapshot in chatrooms!!.documents) {
                if(snapshot.getString("id")!!.contains(thisUser.toString()) && snapshot.getString("lastcomment").toString() != "") {
                    snapshot?.let { chatroom ->
                        val chatroomDatas = ChatroomData (
                            chatroom.getString("chatidx").toString(),
                            chatroom.getString("id").toString(),
                            chatroom.getString("lastcomment").toString(),
                            chatroom.getString("n0").toString(),
                            chatroom.getString("n1").toString(),
                            chatroom.getTimestamp("time") as Timestamp
                        )
                        listChatroom.add(chatroomDatas)
                    }
                }
            }
            liveTodoChatroomData.value = listChatroom
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
                Log.d("gfggggggg0-0", count.toString())
            }
            liveTodoChatroomDataCount = count

            Log.d("gfggggggg0", liveTodoChatroomDataCount.toString())
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

    /* Chat comment 생성 */
    fun addChat(chat: ChatData, idx: String, yourId: String, isheadFlag: String) {
        chat.ishead = isheadFlag
        jecesfirestore!!.collection("Chat").document(idx).collection(idx).add(chat)
            .addOnSuccessListener {
                /* 성공 */
                Log.d("데이터 입력 성공", "ㅇㅇ")
                updateChatCount(idx, yourId)
                
            }.addOnFailureListener { exception ->
                /* 실패 */
                Log.w("CHAT 데이터 입력 실패", "Error getting documents")
            }

        /* 채팅방 리스트의 내용을 다시 보여주려면 수정 */
        val dbRef = jecesfirestore!!.collection("Chatroom")
        dbRef.whereEqualTo("chatidx", chat.chatroomidx).get().addOnCompleteListener {
            if(it.isSuccessful) {
                for(document in it.result) {
                    val update: MutableMap<String, Any> = HashMap()
                    update["lastcomment"] = chat.content
                    update["time"] = chat.time
                    dbRef.document(document.id).set(update, SetOptions.merge())
                }
            }
        }
    }

    /* 제일 마지막 데이터 가져오기 */
    fun lastChat(chat : ChatData, idx: String, yourId: String) {
        /* 헤더인지 보기, 헤더라면 */

        /* 비어있다면 비교할 필요 X */
        if(listChat.isEmpty()) {
            addChat(chat, idx, yourId, "true")
            return
        }
        /* 데이터가 하나라도 있다면 */
        val dbRef = jecesfirestore!!.collection("Chat")
        dbRef.document(chat.chatroomidx).collection(chat.chatroomidx).orderBy("time", Query.Direction.DESCENDING).limit(1).get().addOnCompleteListener {
            isheadFlag = ""
            if(it.isSuccessful) {
                for(document in it.result) {
                    documentId = document.id
                    if(changeTime(listChat.last().time) == changeTime(chat.time) && listChat.last().myid == thisUser && listChat.last().chatroomidx == chat.chatroomidx) {
                        if ((document.getString("myid").toString() == thisUser) && (document.id == documentId)){
                            val update: MutableMap<String, Any> = HashMap()
                            update["fronttimesame"] = "true"
                            dbRef.document(idx).collection(idx).document(document.id).set(update, SetOptions.merge())
                        }
                        isheadFlag = "false"
                    }
                    else {
                        isheadFlag = "true"
                    }
                }
            }
            Log.d("asdfasd10", isheadFlag.toString())
            addChat(chat, idx, yourId, isheadFlag!!)
        }
        
        /* 헤더변경 */
        val dbRefs = jecesfirestore!!.collection("Chat")
        dbRefs.document(chat.chatroomidx).collection(chat.chatroomidx).orderBy("time", Query.Direction.DESCENDING).limit(1).get().addOnCompleteListener {
            if(it.isSuccessful) {
                for(document in it.result) {

                }
            }
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

    /* Chat 가져오기 */
    fun getChat(idx: String) {
        /* 데이터베이스 담기 */
        jecesfirestore!!.collection("Chat").document(idx).collection(idx).orderBy("time", Query.Direction.ASCENDING).addSnapshotListener { it, e ->
            if(e != null) {
                return@addSnapshotListener
            }
            listChat.clear()
            for(document in it!!.documents) {
                val chatDatas = ChatData(
                    document.getString("chatroomidx").toString(),
                    document.getString("content").toString(),
                    document.getString("myid").toString(),
                    document.getTimestamp("time") as Timestamp,
                    document.getString("fronttimesame").toString(),
                    document.getString("isread").toString(),
                    document.getString("ishead").toString()
                )
                listChat.add(chatDatas)
            }
            liveTodoChatDataList.value = listChat
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
            "productImgUrl" to product.product_img_url,
            "pChatCount" to product.chatCount,
            "pViewCount" to product.viewCount,
            "pHeartCount" to product.heartCount
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
            Log.d("서치1", searchLiveTodoData.value.toString())
            searchLiveTodoData.value = response
        }
        Log.d("서치2", searchLiveTodoData.value.toString())
        return searchLiveTodoData
    }

    /* 제품 디테일 데이터를 가지고 있는 데이터 */
    fun setProductDetail(productId : String, productName: String, productPrice: String, productDescription: String, productCount: String, pChatCount: String, pViewCount: String, pHearCount: String, getPosition: Int) {
        productArrayList.clear()
        val productDetail = Product(productId, productName, productPrice, productDescription, productCount.toInt(), thisUser + "_" + productName + "_0_IMAGE_.png", pChatCount, pViewCount, pHearCount)
        position = getPosition
        productArrayList.add(productDetail)
    }

    /* 보냈을 때 상대방이 채팅창에 있을 때 나타냄 */
    fun isRead(yourId: String, idx: String) {
        jecesfirestore!!.collection("UserInfo").whereEqualTo("id", yourId).addSnapshotListener { chat, e ->
            if(e != null) {
                return@addSnapshotListener
            }
            for(document in chat!!.documents) {
                var checkRead: String = document.getString("whereUser").toString()
                if(checkRead == "chat") {
                    updateIsRead(idx)
                }
            }
        }
    }

    /* 읽었다고 업데이트 해줌 */
    private fun updateIsRead(idx: String) {
        val dbRef = jecesfirestore!!.collection("Chat").document(idx).collection(idx)
        dbRef.get().addOnCompleteListener() { chat ->
            for(document in chat.result) {
                val update: MutableMap<String, Any> = HashMap()
                update["isread"] = "true"
                dbRef.document(document.id).set(update, SetOptions.merge())
            }
        }
    }

    /* 상대방 채팅 모두 읽음(내가 채팅방에 들어감) */
    fun readChatAll(idx: String, yourId: String) {
        /* 채팅읽음표시를 바꿔 줌 */
        val dbRef = jecesfirestore!!.collection("Chat").document(idx).collection(idx)

        dbRef.whereEqualTo("myid", yourId).get().addOnCompleteListener { chat ->
            if(chat.result.isEmpty) {
            } else {
                for(document in chat.result) {
                    val update: MutableMap<String, Any> = HashMap()
                    update["isread"] = "true"
                    dbRef.document(document.id).set(update, SetOptions.merge())
                }

            }
        }
        /* 채팅룸 카운트를 0으로 바꿔 줌 */
        val dbRefs = jecesfirestore!!.collection("Chatroom")
        dbRefs.whereEqualTo("chatidx", idx).get().addOnCompleteListener { chatroom ->
            if(chatroom.result.isEmpty) {
            } else {
                for(document in chatroom.result) {
                    /* 나의 아이디 찾기 */
                    val n0 = document.getString("n0")!!.split("/")
                    val n1 = document.getString("n1")!!.split("/")
                    val update: MutableMap<String, Any> = HashMap()
                    if(n0[0] == yourId) {
                        update["n1"] = "${n1[0]}/0"
                    }
                    else {
                        update["n0"] = "${n0[0]}/0"
                    }
                    dbRefs.document(document.id).set(update, SetOptions.merge())
                }
            }
        }
    }

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
                    Log.d("asdfasdf1", chatSearchLiveTodoData.value.toString())
                    flags = true
                    break
                }
            }
            if(flags == false) {
                /* 목록 없으면 null 값 넣기 */
                response.searchChat = null
                chatSearchLiveTodoData.value = response
                Log.d("asdfffff0", chat.result.documents.toString())
            }
            Log.d("asdfffff", chat.result.documents.toString())
        }
        Log.d("asdfasdf2", chatSearchLiveTodoData.value.toString())
        return chatSearchLiveTodoData
    }

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
}