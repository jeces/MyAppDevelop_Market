package com.example.applicationjeces.product

import android.app.Application
import android.app.blob.BlobStoreManager
import android.content.Intent
import android.util.Log
import androidx.lifecycle.*
import com.example.applicationjeces.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/* 뷰모델은 DB에 직접 접근하지 않아야함. Repository 에서 데이터 통신 */
class ProductViewModel(application: Application): AndroidViewModel(application) {

    val getAll: LiveData<List<Product>>
    val liveTodoData = MutableLiveData<List<Product>>()
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

//        /* firestore 모든 데이터 가져오기 */
//        jecesfirestore.collection("Product").get().addOnSuccessListener { result ->
//            for(document in result) {
//
//            }
//        }

        //실시간으로 데이터가져오기
        //데이터를 실시간으로 가져오기 때문에
        //데이터 삭제, 수정, 추가 한 후 데이터를 다시 받아오지 않아도 됨
//        jecesfirestore!!.collection("mepion1234@naver.com").get().addOnSuccessListener { result ->
//            Log.d("가져온데이터", result.documents.toString())
//            for(snapshot in result) {
//                Log.d("가져온데이터", snapshot["productName"].toString())
//            }
//        }


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

    /* firebase 데이터 */
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