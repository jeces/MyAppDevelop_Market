package com.example.applicationjeces.product

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

/* 뷰모델은 DB에 직접 접근하지 않아야함. Repository 에서 데이터 통신 */
class ProductViewModel(application: Application): AndroidViewModel(application) {

    var liveTodoData = MutableLiveData<List<DocumentSnapshot>>()
    private val repository: ProductRepository
    var jecesfirestore: FirebaseFirestore? = null
    var thisUser: String? = null
    var position: Int = 0
    var productArrayList: MutableList<Product> = ArrayList()
    lateinit var imgList: ArrayList<String>

    init {

        /* firebase 연동 */
        jecesfirestore = FirebaseFirestore.getInstance()

        /* 현재 로그인 아이디 */
        thisUser = FirebaseAuth.getInstance().currentUser?.email.toString()
        Log.d("로그인", thisUser.toString())

        /* firebase product 전체 가져오기 */
        /* https://velog.io/@nagosooo/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-TodoList%EC%95%B1-%EB%A7%8C%EB%93%A4%EA%B8%B0 */
        allProduct()

        val productDao = ProductDatabase.getInstance(application).productDao()
        /* 이니셜라이즈(초기화) 해줌 */
        repository = ProductRepository(productDao)

    }

    /* firebase storage에서 이미지 가져오기 */
    fun getImage(productName:String, productCount: Int): MutableList<String> {
        imgList.clear()
        /* 글자 나누기 */
        /* 카운트는 가져와야함 product에 저장해놓고 */
        /* User이름, 상품이름, 사진갯수몇가지인지[product에 추가할것], 사진idx값 가져오기 */
        for(i: Int in 0 until productCount) {
            /* 워드를 가져와서 돌림 */
            var word: String = thisUser + "_" + productName + "_" + i + "_IMAGE_.png"
            Log.d("워드", word)
            imgList.add(word)
        }
        return imgList
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

    /* firebase Product 입력 */
    fun addProducts(product: Product) {
        val products = hashMapOf(
            "ID" to thisUser,
            "productName" to product.product_name,
            "productPrice" to product.product_price,
            "productDescription" to product.product_description,
            "productCount" to product.product_count
        )
        jecesfirestore!!.collection("Product").add(products)
            .addOnSuccessListener {
                // 성공할 경우
                Log.w("데이터 입력 성공", "Error getting documents")
            }
            .addOnFailureListener { exception ->
                // 실패할 경우
                Log.w("데이터 입력 실패", "Error getting documents")
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

    /* 디테일 데이터를 가지고 있는 데이터 */
    fun setProductDetail(productName: String, productPrice: String, productDescription: String, getPosition: Int) {
        productArrayList.clear()
        val productDetail = Product(0, productName, productPrice, productDescription, 0)
        position = getPosition
        productArrayList.add(productDetail)
    }

    //    fun test(searchName: String) : MutableLiveData<List<DocumentSnapshot>> {
//        Log.d("테스트2", "1")
//        jecesfirestore!!.collection("Product").addSnapshotListener { products, e ->
//            liveTodoData = MutableLiveData<List<DocumentSnapshot>>()
//            if (e != null) {
//                return@addSnapshotListener
//            }
//            if(searchName == "") {
//                return@addSnapshotListener
//            }
//            for (snapshot in products!!.documents) {
//                if (snapshot.getString("productName")!!.contains(searchName)) {
//                    Log.d("라이브데이터11", snapshot.toString())
//                    liveTodoData += snapshot
//                    Log.d("라이브데이터1", liveTodoData.value.toString())
//                }
//            }
//            Log.d("라이브데이터2", liveTodoData.value.toString())
//            Log.d("테스트3", "1")
//        }
//        Log.d("테스트4", "1")
//        return liveTodoData
//    }

//    /* LiveData 추가 만들기 */
//    operator fun <T> MutableLiveData<List<T>>.plusAssign(item: T) {
//        val value = this.value ?: emptyList()
//        this.value = value + listOf(item)
//    }
}