package com.example.applicationjeces.user

import ReviewAdapter
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.applicationjeces.databinding.ActivityYourBinding
import com.example.applicationjeces.product.ProductViewModel
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage

/**
 * A simple [Fragment] subclass.
 * Use the [InfoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

/* https://greensky0026.tistory.com/224 */
/* viewpager2 이미지 슬라이더 사용하기 */
/* 디테일 */
class YourActivity : AppCompatActivity()  {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var productViewModel: ProductViewModel
    private var _binding: ActivityYourBinding? = null
    private val binding get() = _binding!!

    /* 이미지 리스트 */
    var imagelist = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * view 바인딩
         */
        _binding = ActivityYourBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**
         * 뷰모델 초기화 생성자
         **/
        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        /**
         * 나의 프로필 이미지 및 이름
         */
        val pidValue = intent.getStringExtra("pid_key").toString()
        setYourImage(pidValue)

        /**
         * 상대방 닉네임 가져오기
         */
        productViewModel.fetchProductNickName(pidValue)
        productViewModel.productNickName.observe(this, Observer { productNick ->
//            binding.productBidPrice.text = "₩ " + addCommasToNumberString(productBidPrices.toString()) + "원"
            binding.yourProfileName.text = productNick
        })

        /**
         * 판매목록 상품
         */
//        val adapter = MyProductRecyclerViewAdapter(this@MyFragment, myId, emptyList(), "myProduct")
        val adapter = YourInfoAdapter(this, pidValue, emptyList())
        val recyclerView = binding.yourProductSaleV
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        // Change from LinearLayoutManager to GridLayoutManager
        recyclerView.layoutManager = GridLayoutManager(this, 3)

        /**
         * 옵져브
         **/
        productViewModel.mySetProduct(pidValue)
        productViewModel.myProductLiveTodoData.observe(this, Observer { product ->
            /* ViewModel에 Observe를 활용하여 productViewModel에 ReadAllData 라이브 데이터가 바뀌었을때 캐치하여, adapter에서 만들어준 setData함수를 통해 바뀐데이터를 UI에 업데이트 해줌 */
            adapter.setData(product)
        })

        /**
         * 판매목록 버튼
         */
        binding.yourProductSale.setOnClickListener {
            if (recyclerView.visibility == View.VISIBLE) {
                recyclerView.visibility = View.GONE
            } else {
                recyclerView.visibility = View.VISIBLE
            }
        }

        /**
         * 리뷰
         */
        val reviewAdapter = ReviewAdapter(emptyList())
        val reviewRecyclerView = binding.yourReviewRecyclerView
        reviewRecyclerView.adapter = reviewAdapter

        /**
         * 리뷰목록 버튼
         */
        binding.yourReviewText.setOnClickListener {
            if (reviewRecyclerView.visibility == View.VISIBLE) {
                reviewRecyclerView.visibility = View.GONE
            } else {
                reviewRecyclerView.visibility = View.VISIBLE
            }
        }

        /**
         * review 옵져브
         **/
        productViewModel.fetchReviews(pidValue)
        productViewModel.reviews.observe(this, Observer { reviews ->
            /* ViewModel에 Observe를 활용하여 productViewModel에 ReadAllData 라이브 데이터가 바뀌었을때 캐치하여, adapter에서 만들어준 setData함수를 통해 바뀐데이터를 UI에 업데이트 해줌 */
            reviewAdapter.setData(reviews)
        })

        val myid = productViewModel.thisUser
        binding.submitReviewButton.setOnClickListener {
            submitReview(pidValue, myid)
            reviewRecyclerView.visibility = View.VISIBLE
        }

//        /**
//         * 화면 폭 계산
//         * GridLayout 계산
//         */
//        val displayMetrics = resources.displayMetrics
//        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
//        val numberOfColumns = (dpWidth / 100).toInt()
//
//        /**
//         * 프로필 이름 옵저버
//         */
//        productViewModel.fetchUserName()
//        productViewModel.nickName.observe(viewLifecycleOwner, Observer { nick->
//            binding.profileName.text = nick
//        })
//
//        /**
//         * 나의 판매목록 상품
//         */
////        val adapter = MyProductRecyclerViewAdapter(this@MyFragment, myId, emptyList(), "myProduct")
//        val adapter = ProductViewPagerAdapter(this@YourActivity, myId, emptyList())
//        val recyclerView = binding.myProductSaleV
//        recyclerView.adapter = adapter
//        recyclerView.setHasFixedSize(true)
//        // Change from LinearLayoutManager to GridLayoutManager
//        recyclerView.layoutManager = GridLayoutManager(requireContext(), numberOfColumns)
//
//        /**
//         * 나의 판매 개수
//         */
//        productViewModel.fetMyProductCellCount()
//        productViewModel.productMyCellCount.observe(viewLifecycleOwner, Observer { count ->
//            binding.ProductCellCount.text = count.toString()
//        })
//
//        /**
//         * 나의 찜한 개수
//         */
//        productViewModel.fetMyProductHeartCount()
//        productViewModel.productMyHeartCount.observe(viewLifecycleOwner, Observer { count ->
//            binding.ProductHeartCount.text = count.toString()
//        })
//
//        /**
//         * 옵져브
//         **/
//        productViewModel.mySetProduct()
//        productViewModel.myProductLiveTodoData.observe(viewLifecycleOwner, Observer { product ->
//            /* ViewModel에 Observe를 활용하여 productViewModel에 ReadAllData 라이브 데이터가 바뀌었을때 캐치하여, adapter에서 만들어준 setData함수를 통해 바뀐데이터를 UI에 업데이트 해줌 */
//            adapter.setData(product)
//        })
//
//        /**
//         * 나의 판매목록 버튼
//         */
//        binding.myProductSale.setOnClickListener {
//            if (recyclerView.visibility == View.VISIBLE) {
//                recyclerView.visibility = View.GONE
//            } else {
//                recyclerView.visibility = View.VISIBLE
//            }
//        }
//
//        /**
//         * 나의 구매 목록
//         */
////        val adapterPc = MyProductRecyclerViewAdapter(this@MyFragment, myId, emptyList(), "myProductPc")
//        val adapterPc = ProductViewPagerAdapter(this@YourActivity, myId, emptyList())
//        val recyclerViewPc = binding.myProductPc
//        recyclerViewPc.adapter = adapterPc
//        recyclerViewPc.setHasFixedSize(true)
//        // Change from LinearLayoutManager to GridLayoutManager
//        recyclerViewPc.layoutManager = GridLayoutManager(requireContext(), numberOfColumns)
//
//
//        binding.editProfile.setOnClickListener {
//            val intent = Intent(activity, EditProfileActivity::class.java)
//            startActivity(intent)
//        }
//
//        /**
//         * 나의 구매목록 버튼
//         */
//        binding.ProductPc.setOnClickListener {
//            if (recyclerViewPc.visibility == View.VISIBLE) {
//                recyclerViewPc.visibility = View.GONE
//            } else {
//                recyclerViewPc.visibility = View.VISIBLE
//            }
//        }
//
//        /**
//         * 나의 관심 목록
//         */
//        val adapterFv = ProductViewPagerAdapter(this@YourActivity, myId, emptyList())
//        val recyclerViewFv = binding.myProductFv
//        recyclerViewFv.adapter = adapterFv
//        recyclerViewFv.setHasFixedSize(true)
//        // Change from LinearLayoutManager to GridLayoutManager
//        recyclerViewFv.layoutManager = GridLayoutManager(requireContext(), numberOfColumns)
//
//        /**
//         * 옵져브
//         **/
//        productViewModel.mySetProductFv()
//        productViewModel.myProductFvLiveTodoData.observe(viewLifecycleOwner, Observer { product ->
//            /* ViewModel에 Observe를 활용하여 productViewModel에 ReadAllData 라이브 데이터가 바뀌었을때 캐치하여, adapter에서 만들어준 setData함수를 통해 바뀐데이터를 UI에 업데이트 해줌 */
//            Log.d("aaaaddd", "aadada")
//            adapterFv.setData(product)
//        })
//
//        /**
//         * 나의 관심목록 버튼
//         */
//        binding.ProductFv.setOnClickListener {
//            if (recyclerViewFv.visibility == View.VISIBLE) {
//                recyclerViewFv.visibility = View.GONE
//            } else {
//                recyclerViewFv.visibility = View.VISIBLE
//            }
//        }
//
//        /**
//         * 버튼 클릭
//         */
//        binding.ReviewText.setOnClickListener {
//            if (reviewRecyclerView.visibility == View.VISIBLE) {
//                reviewRecyclerView.visibility = View.GONE
//            } else {
//                reviewRecyclerView.visibility = View.VISIBLE
//            }
//        }
//
//        val reviews = listOf(
//            Review("John Doe", "", "Great seller!", 0, "d", ""),
//            Review("Jane Smith", "", "Very responsive and friendly.", 0, "", "")
//            // ... 추가 리뷰
//        )
//
//        val reviewAdapter = ReviewAdapter(reviews)
//        val reviewRecyclerView = binding.reviewRecyclerView
//        reviewRecyclerView.adapter = reviewAdapter
//
//        binding.editProfile.setOnClickListener {
//            val intent = Intent(activity, EditProfileActivity::class.java)
//            startActivity(intent)
//        }
//
//        setupItemClickListener(adapter)
//        setupItemClickListener(adapterFv)
//        setupItemClickListener(adapterPc)

    }

//
//    // 리스너 설정 함수
//    fun setupItemClickListener(adapter: ProductViewPagerAdapter) {
//        adapter.setItemClickListener(object : ProductViewPagerAdapter.OnItemClickListener {
//            override fun onClick(v: View, position: Int) {
//                val product = adapter.producFiretList[position].data as HashMap<String, Any>
//                onProductClicked(product, position)
//            }
//        })
//    }
//
//    fun onProductClicked(product: HashMap<String, Any>, position: Int) {
//        val gson = Gson()
//        val tags = listOf(product["tags"]) as List<String>
//        productViewModel.setProductDetail(product["ID"].toString(), product["productName"].toString(), product["productPrice"].toString().toInt(),
//            product["productDescription"].toString(), product["productCount"].toString().toInt(), product["pChatCount"].toString().toInt(),
//            product["pViewCount"].toString().toInt(), product["pHeartCount"].toString().toInt(), product["productBidPrice"].toString(), product["insertTime"].toString(), position,
//            tags, product["category"].toString(), product["state"].toString()
//        )
//        val tagsJson = gson.toJson(tags)
//        val intent = Intent(getActivity(), InfoActivity::class.java)
//        intent.apply {
//            putExtra("ID", product["ID"].toString())
//            putExtra("IDX", product["IDX"].toString())
//            putExtra("productName", product["productName"].toString())
//            putExtra("productPrice", product["productPrice"].toString())
//            putExtra("productDescription", product["productDescription"].toString())
//            putExtra("productCount", product["productCount"].toString())
//            putExtra("pChatCount", product["pChatCount"].toString())
//            putExtra("pViewCount", product["pViewCount"].toString())
//            putExtra("pHeartCount", product["pHeartCount"].toString())
//            putExtra("productBidPrice", product["productBidPrice"].toString())
//            putExtra("insertTime", product["insertTime"].toString())
//            putExtra("position", position)
//            putExtra("tags", tagsJson)
//        }
//
//        startActivity(intent)
//        activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
//    }
//

    private fun submitReview(pId: String, myId: String) {
        val to = pId   // 대상 유저의 ID나 이름을 설정하세요.
        val from = myId // 현재 유저의 ID나 이름을 설정하세요.
        val content = binding.reviewContentEditText.text.toString()
        val rating = binding.ratingBar.rating
        val product = "상품 ID or 이름"  // 해당 리뷰가 연결된 상품의 ID나 이름을 설정하세요.
        val time = Timestamp.now().toDate().toString()  // 현재 시간을 문자열로 변환
        val review = Review(to, from, content, rating, product, time)

        productViewModel.addReview(review)

    }
    /**
     * 나의 프로필 이미지
     */
    fun setYourImage(yId: String) {
        var db = FirebaseStorage.getInstance()
        db.reference.child("${yId}/profil/${yId}_Profil_IMAGE_.png").downloadUrl.addOnCompleteListener {
            if(it.isSuccessful) {
                Glide.with(this@YourActivity)
                    .load(it.result)
                    .override(70, 70)
                    .fitCenter()
                    .circleCrop() // 또는 .transform(RoundedCorners(radius)) 를 사용하여 모서리의 반경을 설정
                    .into(binding.profileImage)
            } else {
                db.reference.child("basic_user.png").downloadUrl.addOnCompleteListener { its ->
                    Glide.with(this@YourActivity)
                        .load(it.result)
                        .override(70, 70)
                        .fitCenter()
                        .circleCrop() // 또는 .transform(RoundedCorners(radius)) 를 사용하여 모서리의 반경을 설정
                        .into(binding.profileImage)
                }
            }
        }.addOnFailureListener {

        }
    }
}