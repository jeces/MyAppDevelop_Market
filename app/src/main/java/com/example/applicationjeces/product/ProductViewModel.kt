package com.example.applicationjeces.product

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/* 뷰모델은 DB에 직접 접근하지 않아야함. Repository 에서 데이터 통신 */
class ProductViewModel(application: Application): AndroidViewModel(application) {

    val getAll: LiveData<List<Product>>
    private val repository : ProductRepository
    var jecesfirestore : FirebaseFirestore? = null

    init {
        /* firebase 연동 */
        jecesfirestore = FirebaseFirestore.getInstance()

//        /* firestore 모든 데이터 가져오기 */
//        jecesfirestore.collection("Product").get().addOnSuccessListener { result ->
//            for(document in result) {
//
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
        jecesfirestore?.collection("User")?.document("id:1")?.collection("Product")?.document(product.product_name)?.set(products)
    }

    /* 검색 */
    fun searchProduct(searchName: String): LiveData<List<Product>> {
        repository.search(searchName).asLiveData()
        Log.d("검색3ㄴ", getAll.toString())
        return repository.search(searchName).asLiveData()
    }
}