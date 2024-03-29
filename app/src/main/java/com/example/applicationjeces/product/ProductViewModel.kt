package com.example.applicationjeces.product

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.example.applicationjeces.chat.ChatroomData
import com.example.applicationjeces.search.FilterCriteria
import com.example.applicationjeces.user.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Flow

class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProductRepository = ProductRepository()

    // 초기 데이터 로드 플래그
    private var isDataInitialized = false

    val previousFavorites = mutableListOf<String>()
    val previousProductPrices = mutableMapOf<String, Double>()

    private val _unreadChatCount = MutableLiveData<Int>()
    val unreadChatCount: LiveData<Int> get() = _unreadChatCount

    val productNameExists = MutableLiveData<Boolean>()

    var isFirstRun = true  // ViewModel의 전역 변수로 추가

    val newNotification: MutableLiveData<Notification> = MutableLiveData()

    // LiveDatas
    var liveTodoData: MutableLiveData<List<DocumentSnapshot>> = MutableLiveData(emptyList())
    var myProductLiveTodoData: MutableLiveData<List<DocumentSnapshot>> = MutableLiveData()
    var myProductFvLiveTodoData: MutableLiveData<List<DocumentSnapshot>> = MutableLiveData()
    var liveTodoChatroomDataCount: MutableLiveData<Int> = MutableLiveData(0)
    var productArrayList: MutableList<Product> = mutableListOf()

    private val _reviews = MutableLiveData<List<DocumentSnapshot>>()
    val reviews: LiveData<List<DocumentSnapshot>> get() = _reviews

    private val _recentProducts = MutableLiveData<List<DocumentSnapshot>>()
    val recentProducts: LiveData<List<DocumentSnapshot>> get() = _recentProducts

    private val _productsSortedByHeartCount = MutableLiveData<List<DocumentSnapshot>>()
    val productsSortedByHeartCount: LiveData<List<DocumentSnapshot>> get() = _productsSortedByHeartCount

    private val _productsSortedByViewCount = MutableLiveData<List<DocumentSnapshot>>()
    val productsSortedByViewCount: LiveData<List<DocumentSnapshot>> get() = _productsSortedByViewCount

    private val _productRecentByHeartCount = MutableLiveData<List<DocumentSnapshot>>()
    val productRecentByHeartCount: LiveData<List<DocumentSnapshot>> get() = _productRecentByHeartCount

    private val _productRecentByViewCount = MutableLiveData<List<DocumentSnapshot>>()
    val productRecentByViewCount: LiveData<List<DocumentSnapshot>> get() = _productRecentByViewCount

    private val _searchResponse = MutableLiveData<Response>()
    val searchResponse: LiveData<Response> get() = _searchResponse

    private val _productMyCellCount = MutableLiveData<Int>()
    val productMyCellCount: LiveData<Int> get() = _productMyCellCount

    private val _productMyHeartCount = MutableLiveData<Int>()
    val productMyHeartCount: LiveData<Int> get() = _productMyHeartCount

    private val _adverImages = MutableLiveData<List<String>>()
    val adverImages: LiveData<List<String>> get() = _adverImages

    val bidPrice = MutableLiveData<String>()

    val thisUser: String by lazy { FirebaseAuth.getInstance().currentUser?.email ?: "" }

    private val _nickName = MutableLiveData<String>()
    val nickName: LiveData<String> get() = _nickName

    private val _productNickName = MutableLiveData<String>()
    val productNickName: LiveData<String> get() = _productNickName

    private var currentPage = 0
    private val itemsPerPage = 15

    var position: Int = 0

    private val _adverCounts = MutableLiveData<Int>()
    val adverCounts: LiveData<Int>
        get() = _adverCounts

    init {
        Log.d("thisuser", thisUser)
        fetchAllProducts()
        fetchAdverCountsAndImages()
//        initializeAndObserveNotifications(thisUser)
    }


    fun fetchUnreadMessagesCount(userId: String) {
        viewModelScope.launch {
            repository.getUnreadMessagesCount(userId).collect { count ->
                _unreadChatCount.value = count
            }
        }
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

    fun fetchAndSetYourImage(pId: String, callback: (String?) -> Unit) {
        viewModelScope.launch {
            val imageUrl = repository.fetchSellerImage(pId)
            Log.d("asda123123", imageUrl.toString())
            callback(imageUrl)
        }
    }

    suspend fun uploadImage(index: Int, productName: String, myId: String, uri: Uri): Boolean {
        return repository.uploadImage(index, productName, myId, uri)
    }

    fun checkProductName(myId: String, productName: String) = viewModelScope.launch {
        productNameExists.value = repository.checkProductNameExists(myId, productName)
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

    fun mySetProduct(id: String) {
        viewModelScope.launch {
            try {
                myProductLiveTodoData.value = repository.mySetProduct(id)
                Log.d("adadadada1", myProductLiveTodoData.value.toString())
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }

    fun mySetProductFv( ) {
        viewModelScope.launch {
            try {
                myProductFvLiveTodoData.value = repository.mySetProductFv()
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }

    fun addProducts(product: Product, addressSet: String) {
        viewModelScope.launch {
            try {
                repository.addProducts(product, addressSet)
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }

    fun updateProducts(product: UpdateProduct, oldProductName: String) {
        viewModelScope.launch {
            try {
                repository.updateProducts(product, oldProductName)
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }

    fun searchProducts(searchName: String? = null, filterCriteria: FilterCriteria? = null) {
        viewModelScope.launch {
            try {
                val response = repository.searchProducts(searchName, filterCriteria)
                _searchResponse.value = response
            } catch (e: Exception) {
                // Handle exceptions, for example, by updating a LiveData that the UI observes
            }
        }
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

    /**
     * processBid 함수에서 bidchange를 호출하기 전에 Deferred 객체를 반환받아 프래그먼트가 종료되기 전에 코루틴이 완료될 때까지 기다
     */
    fun bidchange(pId: String, pName: String, bidPrice: String): Deferred<Unit> {
        return viewModelScope.async {
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
        productPrice: Int,
        productDescription: String,
        productCount: Int,
        pChatCount: Int,
        pViewCount: Int,
        pHearCount: Int,
        pBidPrice: String,
        uploadTime: String,
        getPosition: Int,
        tags: List<String>,
        category: String,
        state: String
    ) {
        val imageUrl = "${repository.thisUser}_${productName}_0_IMAGE_.png"
        val productDetail = Product(productId, productName, productPrice, productDescription, productCount, imageUrl, pChatCount, pViewCount, pHearCount, pBidPrice, uploadTime, tags, category, state)

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

    fun isCheckProduct(myId: String, pidx: String): LiveData<Boolean> {
        val productExistLiveData = MutableLiveData<Boolean>()

        viewModelScope.launch {
            try {
                val doesExist = repository.isProductExistForUser(myId, pidx)
                productExistLiveData.value = doesExist
            } catch (e: Exception) {
                // Handle the exception
            }
        }
        return productExistLiveData
    }

    fun removeMyFavorit(pIdx: String) {
        viewModelScope.launch {
            try {
                repository.removeMyFavorit(pIdx)
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }

    /**
     * 최신 10개
     */
    fun fetchRecentProducts() {
        viewModelScope.launch {
            try {
                _recentProducts.value = repository.fetchRecentTenProducts()
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }

    /**
     * 가장 많은 Heart
     */
    fun fetchProductsSortedByHeartCount() {
        viewModelScope.launch {
            try {
                _productsSortedByHeartCount.value = repository.getProductsSortedByHeartCount()
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }

    /**
     * 가장 많은 View
     */
    fun fetchProductsSortedByViewCount() {
        viewModelScope.launch {
            try {
                _productsSortedByViewCount.value = repository.getProductsSortedByViewCount()
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }

    /**
     * 최근 많은 Heart
     */
    fun fetRecentProductHeartCount() {
        viewModelScope.launch {
            try {
                Log.d("agagagag", repository.getRecentProductHeartCount().toString())
                _productRecentByHeartCount.value = repository.getRecentProductHeartCount()
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }

    /**
     * 최근 많은 View
     */
    fun fetRecentProductViewCount() {
        viewModelScope.launch {
            try {
                _productRecentByViewCount.value = repository.getRecentProductViewCount()
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }

    /**
     * 내 판매 물품 수
     */
    fun fetMyProductCellCount() {
        viewModelScope.launch {
            try {
                _productMyCellCount.value = repository.getMyProductCellCount()
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }

    /**
     * 내 찜한 물품 수
     */
    fun fetMyProductHeartCount() {
        viewModelScope.launch {
            try {
                _productMyHeartCount.value = repository.getMyProductHeartCount()
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }

    /**
     * 닉네임 가져오기
     */
    fun fetchUserName(id: String) {
        viewModelScope.launch {
            try {
                _nickName.value = repository.fetchUserName(id)
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error fetching user name", e)
            }
        }
    }

    /**
     * 상품 판매자 닉네임 가져오기
     */
    fun fetchProductNickName(pId: String) {
        viewModelScope.launch {
            try {
                _productNickName.value = repository.fetchProductNickName(pId)
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error fetching user name", e)
            }
        }
    }

    /**
     * 입찰가 업데이트
     */
    fun startListeningForBidUpdates(pId: String, pName: String) {
        viewModelScope.launch {
            try {
                repository.listenForBidUpdates(pId, pName).collect { newBidPrice ->
                    bidPrice.postValue(newBidPrice)
                }
            } catch (e: Exception) {
                // Handle exception, e.g., show an error message to the user
            }
        }
    }

    fun addReview(review: Review) {
        viewModelScope.launch {
            try {
                repository.addReviews(review)
            } catch (e: Exception) {
                // Handle exception, e.g., show an error message to the user
            }
        }
    }

    fun fetchReviews(pId: String) {
        viewModelScope.launch {
            try {
                _reviews.value = repository.getReviewsForProduct(pId)
            } catch (e: Exception) {

            }

        }
    }

    /**
     * 찜 알림, 찜 상품 가격변동 알림
     */
    fun notificationsProduct(myId: String, context: Context) {
        // 사용자의 UserInfo로부터 찜 목록 가져오기
        val db = FirebaseFirestore.getInstance()
        val userInfoRef = db.collection("UserInfo").whereEqualTo("id", myId)
        val productsRef = db.collection("product")
        val notificationDatabase = NotificationDatabase.getDatabase(context) // context는 현재 활동 또는 프래그먼트의 컨텍스트입니다.
        val notificationDao = notificationDatabase.notificationDao()

        userInfoRef.addSnapshotListener { userInfoSnapshot, error ->
            if (error != null) {
                // 오류 처리
                return@addSnapshotListener
            }

            val currentFavorites = userInfoSnapshot?.documents?.firstOrNull()?.get("favorit") as? List<String> ?: emptyList()

            if(!isDataInitialized) {
                // 룸데이터 저장
                for (productId in currentFavorites) {
                    if (!previousFavorites.contains(productId)) {
                        val newFavoriteNotification = Notification(
                            id = 0,  // AutoGenerate로 자동 할당됨
                            title = "새로운 찜!",
                            message = "${productId} 상품을 찜한 사람이 있습니다.",
                            timestamp = SimpleDateFormat(
                                "yyyy-MM-dd HH:mm:ss",
                                Locale.getDefault()
                            ).format(Date())
                        )

                        // Room DB에 알림 저장
                        CoroutineScope(Dispatchers.IO).launch {
                            notificationDao.addNotification(newFavoriteNotification)
                        }
                    }
                }
                previousFavorites.addAll(currentFavorites)
                isDataInitialized = true
            }

            // 새로 찜한 상품이 있는 경우
            for (productId in currentFavorites) {
                if (!previousFavorites.contains(productId)) {
                    val newFavoriteNotification = Notification(
                        id = 0,  // AutoGenerate로 자동 할당됨
                        title = "새로운 찜!",
                        message = "${productId} 상품을 찜한 사람이 있습니다.",
                        timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    )

                    // Room DB에 알림 저장
                    CoroutineScope(Dispatchers.IO).launch {
                        notificationDao.addNotification(newFavoriteNotification)
                        newNotification.postValue(newFavoriteNotification) // LiveData에 새로운 알림
                    }
                }
            }
            previousFavorites.clear()
            previousFavorites.addAll(currentFavorites)

            // 찜한 상품의 가격 변동 감지
            for (productId in currentFavorites) {
                productsRef.document(productId).addSnapshotListener { productSnapshot, productError ->
                    if (productError != null) {
                        // 오류 처리
                        return@addSnapshotListener
                    }

                    val currentProductPrice = productSnapshot?.getDouble("productprice") ?: return@addSnapshotListener

                    if (previousProductPrices[productId] != null && currentProductPrice < previousProductPrices[productId]!!) {
                        val priceDropNotification = Notification(
                            id = 0,  // AutoGenerate로 자동 할당됨
                            title = "가격 하락!",
                            message = "${productSnapshot.getString("productName")} 상품의 가격이 떨어졌습니다!",
                            timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                        )

                        // Room DB에 알림 저장
                        CoroutineScope(Dispatchers.IO).launch {
                            notificationDao.addNotification(priceDropNotification)
                            newNotification.postValue(priceDropNotification) // LiveData에 새로운 알림
                        }
                    }

                    // 상품의 현재 가격을 기록
                    previousProductPrices[productId] = currentProductPrice
                }
            }
        }
    }
}
