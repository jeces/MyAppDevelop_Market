package com.example.applicationjeces.product

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.applicationjeces.chat.ChatroomData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProductRepository = ProductRepository()

    // LiveDatas
    var liveTodoData: MutableLiveData<List<DocumentSnapshot>> = MutableLiveData(emptyList())
    var myProductLiveTodoData: MutableLiveData<List<DocumentSnapshot>> = MutableLiveData()
    var myProductFvLiveTodoData: MutableLiveData<List<DocumentSnapshot>> = MutableLiveData()
    var liveTodoChatroomDataCount: MutableLiveData<Int> = MutableLiveData(0)
    var productArrayList: MutableList<Product> = mutableListOf()

    private val _adverImages = MutableLiveData<List<String>>()
    val adverImages: LiveData<List<String>> get() = _adverImages

    val thisUser: String by lazy { FirebaseAuth.getInstance().currentUser?.email ?: "" }

    private var currentPage = 0
    private val itemsPerPage = 15

    var position: Int = 0

    private val _adverCounts = MutableLiveData<Int>()
    val adverCounts: LiveData<Int>
        get() = _adverCounts

    init {
        fetchAllProducts()
        fetchAdverCountsAndImages()
    }

    private fun fetchAllProducts() {
        viewModelScope.launch {
            try {
                Log.d("a1123123", repository.allProduct().toString())
                liveTodoData.value = repository.allProduct()
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }

    fun fetchNextPage(): LiveData<List<DocumentSnapshot>> {
        val nextPageLiveData = MutableLiveData<List<DocumentSnapshot>>()

        repository.fetchNextPageData(currentPage, itemsPerPage)
            .addOnSuccessListener { documents ->
                nextPageLiveData.value = documents.documents
                currentPage++
            }
            .addOnFailureListener { exception ->
                Log.e("ProductViewModel", "Error fetching next page", exception)
            }

        return nextPageLiveData
    }

    fun fetchAdverCountsAndImages() {
        viewModelScope.launch {
            try {
                val count = repository.fetchAdverCounts()
                _adverCounts.value = count
                Log.d("AdverCounts", "Number of files in the folder: $count")

                // Count를 기반으로 이미지를 가져옵니다.
                val images = repository.getAdverImage(count)
                _adverImages.value = images

            } catch (e: Exception) {
                Log.e("AdverDataFetch", "Error occurred: ${e.message}", e)
            }
        }
    }

    fun getAdverCounts() {
        viewModelScope.launch {
            try {
                val count = repository.fetchAdverCounts()
                _adverCounts.value = count
                Log.d("AdverCounts", "Number of files in the folder: $count")
            } catch (e: Exception) {
                Log.e("AdverCounts", "Error occurred: ${e.message}", e)
            }
        }
    }

    fun mySetProduct() {
        viewModelScope.launch {
            try {
                myProductLiveTodoData.value = repository.mySetProduct()
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }

    fun mySetProductFv() {
        viewModelScope.launch {
            try {
                myProductFvLiveTodoData.value = repository.mySetProductFv()
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }

    fun addProducts(product: Product) {
        viewModelScope.launch {
            try {
                repository.addProducts(product)
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }

    fun searchProductsCall(searchName: String): LiveData<List<DocumentSnapshot>> {
        val searchLiveData = MutableLiveData<List<DocumentSnapshot>>()
        viewModelScope.launch {
            try {
                searchLiveData.value = repository.searchProductsCall(searchName)
            } catch (e: Exception) {
                // Handle the exception
            }
        }
        return searchLiveData
    }

    fun viewCountUp(pId: String, pName: String) {
        viewModelScope.launch {
            try {
                repository.viewCountUp(pId, pName)
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }

    fun bidchange(pId: String, pName: String, bidPrice: String) {
        viewModelScope.launch {
            try {
                repository.bidchange(pId, pName, bidPrice)
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }

    fun deleteProduct(pId: String, pName: String) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(pId, pName)
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }

    fun updateProduct(pId: String, pName: String) {
        viewModelScope.launch {
            try {
                repository.updateProduct(pId, pName)
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }

    fun setMyFavorit(pIdx: String) {
        viewModelScope.launch {
            try {
                repository.setMyFavorit(pIdx)
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }

    // 다른 필요한 메소드들이 추가될 수 있습니다.

    fun getImage(productId: String, productName:String, productCount: Int): LiveData<List<String>> {
        val liveImageList = MutableLiveData<List<String>>()
        viewModelScope.launch {
            try {
                liveImageList.value = repository.getImage(productId, productName, productCount)
            } catch (e: Exception) {
                // Handle the exception
            }
        }
        return liveImageList
    }

    fun getAdverImage(adverCount: Int): LiveData<List<String>> {
        val liveAdverImageList = MutableLiveData<List<String>>()
        viewModelScope.launch {
            try {
                val imgNames = repository.getAdverImage(adverCount)
                liveAdverImageList.value = imgNames
            } catch (e: Exception) {
                // Handle the exception
            }
        }
        return liveAdverImageList
    }

    fun getAllChatroomCount(): LiveData<Int> {
        val liveChatroomCount = MutableLiveData<Int>()
        viewModelScope.launch {
            try {
                liveChatroomCount.value = repository.getAllChatroomCount()
            } catch (e: Exception) {
                // Handle the exception
            }
        }
        return liveChatroomCount
    }

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
        val imageUrl = "${repository.thisUser}_${productName}_0_IMAGE_.png"
        val productDetail = Product(productId, productName, productPrice, productDescription, productCount.toInt(), imageUrl, pChatCount, pViewCount, pHearCount, pBidPrice)

        position = getPosition
        productArrayList.add(productDetail)
    }

    fun searchChat(yourId: String): LiveData<Response> {
        val chatSearchLiveData = MutableLiveData<Response>()
        viewModelScope.launch {
            try {
                val response = repository.searchChat(yourId)
                chatSearchLiveData.value = response
            } catch (e: Exception) {
                // Handle the exception
            }
        }
        return chatSearchLiveData
    }

    fun createChatroom(chatroom: ChatroomData) {
        viewModelScope.launch {
            try {
                repository.createChatroom(chatroom)
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }

    fun whereMyUser(location: String) {
        viewModelScope.launch {
            try {
                repository.whereMyUser(location, repository.thisUser)
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }

    fun isCheckProduct(myId: String): LiveData<Boolean> {
        val productExistLiveData = MutableLiveData<Boolean>()

        viewModelScope.launch {
            try {
                val doesExist = repository.isProductExistForUser(myId)
                productExistLiveData.value = doesExist
            } catch (e: Exception) {
                // Handle the exception
            }
        }

        return productExistLiveData
    }
}
