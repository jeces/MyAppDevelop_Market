package com.example.applicationjeces.product

import android.app.Application
import android.app.blob.BlobStoreManager
import android.content.Intent
import android.util.Log
import androidx.lifecycle.*
import com.example.applicationjeces.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/* 뷰모델은 DB에 직접 접근하지 않아야함. Repository 에서 데이터 통신 */
class ProductViewModel(application: Application): AndroidViewModel(application) {

    val getAll: LiveData<List<Product>>
    val liveTodoData = MutableLiveData<List<DocumentSnapshot>>()
    private val repository : ProductRepository
    var jecesfirestore : FirebaseFirestore? = null
    var thisUser : String? = null

    init {
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

        /* 유저정보들 가져오기 */
        jecesfirestore!!.collection("UserInfo").get()
            .addOnSuccessListener {
            for(document in it) {
                /* 각 유저들의 Product 가져오기 */
//                jecesfirestore!!.collection("User").document(document.id).collection("Product").get()
//                    .addOnSuccessListener {
//                        for (products in it) {
//                            Log.d("가져온데이터22", products.id.toString())
//                            /* 가져온 결과 라이브데이터 안에 넣기 */
//                            liveTodoData.value = products?.data
//                        }
//                    }
//                    .addOnFailureListener { exception ->
//                        /* 실패 */
//                        Log.w("가져온데이터2", "Error getting documents: $exception")
//                    }

                /* */


//                jecesfirestore!!.collection("User").document(document.id).collection("Product")
//                    .addSnapshotListener { products, e ->
//                        if(e != null) {
//                            return@addSnapshotListener
//                        }
//                        Log.d("라이브데이터", products?.documents.toString())
//                        liveTodoData.value = products?.documents
//                    }
//                Log.d("라이브데이터", liveTodoData.value.toString())
            }
        }


        val productDao = ProductDatabase.getInstance(application).productDao()
        /* 이니셜라이즈(초기화) 해줌 */
        repository = ProductRepository(productDao)
        /* getall은 repository에서 만들어줬던 livedata */
        getAll = repository.readAllproducts.asLiveData()
        Log.d("검색뷰모델어뎁터init", getAll.toString())
    }




    /* 파라미터에 만든 데이터클래스가 들어감 */
    fun addProduct(product: Product) {
        Log.d("뷰모델add", product.toString())
        /* 코루틴 활성화 dispatcherIO는 백그라운드에서 실행*/
        viewModelScope.launch(Dispatchers.IO) {
            /* repository에 addproduct함수 불러옴 */
            repository.insert(product)
        }
    }

    /* firebase Product 전체 가져오기 */
    fun allProduct() {
        jecesfirestore!!.collection("Product").addSnapshotListener { products, e ->
            if(e != null) {
                return@addSnapshotListener
            }
            Log.d("라이브데이터", products?.documents.toString())
            liveTodoData.value = products?.documents
        }
        Log.d("라이브데이터", liveTodoData.value.toString())
    }

    /* firebase Product 입력 */
    fun addProducts(product : Product) {
        val products = hashMapOf(
            "productName" to product.product_name,
            "productPrice" to product.product_price
        )

        Log.d("프로덕트", product.toString())
        thisUser?.let {
            jecesfirestore?.collection("User")
                ?.document(it)
                ?.collection("Product")
                ?.document(product.product_name)?.set(products)
        }
    }

    /* 검색 */
    fun searchProduct(searchName: String): LiveData<List<Product>> {
        repository.search(searchName).asLiveData()
        Log.d("검색3ㄴ", getAll.toString())
        return repository.search(searchName).asLiveData()
    }
}
