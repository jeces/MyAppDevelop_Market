package com.example.applicationjeces.product

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/* 뷰모델은 DB에 직접 접근하지 않아야함. Repository 에서 데이터 통신 */
class ProductViewModel(application: Application): AndroidViewModel(application) {

    val getAll: LiveData<List<Product>>
    var liveTodoData = MutableLiveData<List<DocumentSnapshot>>()
    var searchLiveTodoData = MutableLiveData<List<DocumentSnapshot>>()
    private val repository: ProductRepository
    var jecesfirestore: FirebaseFirestore? = null
    var thisUser: String? = null
    lateinit var ProductRecyclerViewAdapter: ProductRecyclerViewAdapter

    init {
        /* 초기화 */
        ProductRecyclerViewAdapter = ProductRecyclerViewAdapter(emptyList())
        /* firebase 연동 */
        jecesfirestore = FirebaseFirestore.getInstance()
        /* firebase Auth */
        var authStateListener: FirebaseAuth.AuthStateListener? = null

        /* 현재 로그인 아이디 */
        thisUser = FirebaseAuth.getInstance().currentUser?.email.toString()
        Log.d("로그인", thisUser.toString())

        /* firebase product 전체 가져오기 */
        /* https://velog.io/@nagosooo/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-TodoList%EC%95%B1-%EB%A7%8C%EB%93%A4%EA%B8%B0 */
        allProduct()
        Log.d("테스트1", "1")
        searchProductsCall("1")
        Log.d("테스트5", "1")


        val productDao = ProductDatabase.getInstance(application).productDao()
        /* 이니셜라이즈(초기화) 해줌 */
        repository = ProductRepository(productDao)
        /* getall은 repository에서 만들어줬던 livedata */
        getAll = repository.readAllproducts.asLiveData()
        Log.d("검색뷰모델어뎁터init", getAll.toString())
    }

    /* firebase Product 전체 가져오기 */
    fun allProduct() {
        liveTodoData.value?.isEmpty()
        jecesfirestore!!.collection("Product").addSnapshotListener { products, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            Log.d("라이브데이터", products?.documents.toString())
            liveTodoData.value = products?.documents
        }
        Log.d("라이브데이터", liveTodoData.value.toString())
    }

    /* firebase Product 입력 */
    fun addProducts(product: Product) {
        val products = hashMapOf(
            "ID" to thisUser,
            "productName" to product.product_name,
            "productPrice" to product.product_price
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

    /* 검색 */
    fun searchProduct(searchName: String): LiveData<List<Product>> {
        repository.search(searchName).asLiveData()
        Log.d("검색3ㄴ", getAll.toString())
        return repository.search(searchName).asLiveData()
    }

    /* firebase 검색 */
    /* firestore에서는 like를 사용못함 */
    /* 비동기 앱의 문제점 */
    /* observer 기능을 사용하기 위해 데이터 하나를 수정해줌 */
    /* firesotre가 제일 늦게 반응해 그다음 검색 때 바뀜 */
    /* 서치뷰에 suspend를 쓸수가없음 override 고정되어있어서 await 못씀 */
    fun searchProductsCall(searchName: String) : MutableLiveData<List<DocumentSnapshot>> {
        Log.d("테스트2", "1")
        jecesfirestore!!.collection("Product").addSnapshotListener { products, e ->
            liveTodoData = MutableLiveData<List<DocumentSnapshot>>()
            if (e != null) {
                return@addSnapshotListener
            }
            if(searchName == "") {
                return@addSnapshotListener
            }
            for (snapshot in products!!.documents) {
                if (snapshot.getString("productName")!!.contains(searchName)) {
                    Log.d("라이브데이터11", snapshot.toString())
                    liveTodoData += snapshot
                    Log.d("라이브데이터1", liveTodoData.value.toString())
                }
            }
            Log.d("라이브데이터2", liveTodoData.value.toString())
            Log.d("테스트3", "1")
            ProductRecyclerViewAdapter.notifyDataSetChanged()
        }
        Log.d("테스트4", "1")
        return liveTodoData
    }

    fun test(searchName: String) : MutableLiveData<List<DocumentSnapshot>> {
        searchLiveTodoData.value?.isEmpty()
        jecesfirestore!!.collection("Product").get().addOnSuccessListener { products ->
            searchLiveTodoData = MutableLiveData<List<DocumentSnapshot>>()
            for (snapshot in products!!.documents) {
                if (snapshot.getString("productName")!!.contains(searchName)) {
                    Log.d("테스티1", snapshot.toString())
                    searchLiveTodoData += snapshot
                    Log.d("테스티2", searchLiveTodoData.value.toString())
                }
            }
            Log.d("라이브데이터2", searchLiveTodoData.value.toString())
        }
        return searchLiveTodoData
    }



    fun searchProducts(searchName: String): MutableLiveData<List<DocumentSnapshot>> {
//        searchProductsCall(searchName) {
//            /* 콜백함수 실행 */
//            it ->
//        }
        test(searchName)
        Log.d("라이브데이터2.3", liveTodoData.value.toString())
        return liveTodoData
    }

    /* LiveData 추가 만들기 */
    operator fun <T> MutableLiveData<List<T>>.plusAssign(item: T) {
        val value = this.value ?: emptyList()
        this.value = value + listOf(item)
    }
}