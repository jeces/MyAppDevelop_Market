package com.example.applicationjeces.product

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.Flow

/* Repository는 깔끔한 코드 구성과  MVVM아키텍처를 위해서 사용, 데이터들을 접근하는 코드들을 모아 둘 때 사용함 */
class ProductRepository(private val productDao: ProductDao) {

    /* Database, Dao, Products 초기화 */

    /* Dao에서 데이터를 불로올 때 LiveData로 불러옴 */
    val readAllproducts: Flow<List<Product>> = productDao.getAll()

    /* ViewModel에서 DB에 접근을 요청할 때 수행할 함수 */
    fun getAll(): Flow<List<Product>> {
        return readAllproducts
    }

    /* suspend - coroutine을 사용하기 위해 */
    /* Flow는 suspend 를 적을 필요가 없으며 코루틴에서만 실행 */
    fun insert(product: Product) {
        Log.d("뷰모델4", readAllproducts.toString())
        /* Dao에서 만들었던 insert 실행 */
        productDao.insert(product)
    }

    /* 검색 */
    fun search(searchName : String): Flow<List<Product>> {
        Log.d("뷰모델검색", productDao.search(searchName).toString())
        return productDao.search(searchName)
    }

    fun delete(product: Product) {
        try {
            val thread = Thread(Runnable {
                productDao.delete(product) })
            thread.start()
        } catch (e: java.lang.Exception) {}
    }


}