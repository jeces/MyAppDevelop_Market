package com.example.applicationjeces.product

import SettingCustomBottomSheetDialogFragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.ScrollView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.applicationjeces.R
import com.example.applicationjeces.databinding.FragmentHomeBinding
import com.example.applicationjeces.product.*
import com.example.applicationjeces.user.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment(), AdverRecyclerViewAdapter.OnImageClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var productViewModel: ProductViewModel
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    /* adver 이미지 리스트 */
    var adverImagelist = ArrayList<String>()

    /* adver 이미지 개수 */
    var adverPageCount = 0

    override fun onAttach(context: Context) {
        Log.d("jecesAddFragment", "onAttach")
        super.onAttach(context)
    }

    // 공통으로 사용할 항목 클릭 메서드
    fun onProductClicked(product: HashMap<String, Any>, position: Int) {
        val gson = Gson()
        val tags = listOf(product["tags"]) as List<String>
        productViewModel.setProductDetail(product["ID"].toString(), product["productName"].toString(), product["productPrice"].toString().toInt(),
            product["productDescription"].toString(), product["productCount"].toString().toInt(), product["pChatCount"].toString().toInt(),
            product["pViewCount"].toString().toInt(), product["pHeartCount"].toString().toInt(), product["productBidPrice"].toString(), product["insertTime"].toString(), position,
            tags, product["category"].toString(), product["state"].toString()
        )
        val tagsJson = gson.toJson(tags)
        val intent = Intent(getActivity(), InfoActivity::class.java)
        intent.apply {
            putExtra("ID", product["ID"].toString())
            putExtra("IDX", product["IDX"].toString())
            putExtra("productName", product["productName"].toString())
            putExtra("productPrice", product["productPrice"].toString())
            putExtra("productDescription", product["productDescription"].toString())
            putExtra("productCount", product["productCount"].toString())
            putExtra("pChatCount", product["pChatCount"].toString())
            putExtra("pViewCount", product["pViewCount"].toString())
            putExtra("pHeartCount", product["pHeartCount"].toString())
            putExtra("productBidPrice", product["productBidPrice"].toString())
            putExtra("insertTime", product["insertTime"].toString())
            putExtra("position", position)
            putExtra("tags", tagsJson)
        }

        startActivity(intent)
        activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    // 리스너 설정 함수
    fun setupItemClickListener(adapter: ProductViewPagerAdapter) {
        adapter.setItemClickListener(object : ProductViewPagerAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                val product = adapter.producFiretList[position].data as HashMap<String, Any>
                onProductClicked(product, position)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        /**
         * view 바인딩
         */
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        /**
         * 뷰모델 초기화 생성자
         **/
        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        /**
         *  나의 아이디
         **/
        var myId: String = productViewModel.thisUser.toString()

        /**
         * 자신의 위치 이동 저장
         */
        productViewModel.whereMyUser("home")

        /**
         * 맨 위 광고 페이지
         * viewpager2 adapter 장착
         */
        val viewPager = binding.viewPagerHomeProduce
        val adapterVp = AdverRecyclerViewAdapter(adverImagelist, this@HomeFragment, this)
        viewPager.adapter = adapterVp

        /**
         * adver 이미지 수 가져오기
         */
        productViewModel.adverCounts.observe(viewLifecycleOwner, Observer { count ->
            adverPageCount = count
            /**
             * adverCount 값을 기반으로 adver 이미지 가져오기
             */
            productViewModel.getAdverImage(adverPageCount).observe(viewLifecycleOwner, Observer { images ->
//                adverImagelist.clear()
//                adverImagelist.addAll(images)
                // 이미지 리스트가 변경되었음을 어댑터에 알림
                adapterVp.updateData(images)
            })
        })

        /**
         * indicator 장착
         */
        val dotsIndicator: DotsIndicator = binding.dotsIndicator
        val viewPager2: ViewPager2 = binding.viewPagerHomeProduce
        dotsIndicator.setViewPager2(viewPager2)

        /**
         * 자동 슬라이드
         */
        val delayMillis = 3000L  // 3초에 한 번씩 슬라이드 변경
        val handler = Handler(Looper.getMainLooper())
        val slideRunnable = object : Runnable {
            override fun run() {
                if (adverPageCount != 0) {
                    val nextItem = (viewPager2.currentItem + 1) % adverPageCount
                    viewPager2.setCurrentItem(nextItem, true)
                    handler.postDelayed(this, delayMillis)
                }
            }
        }
        handler.postDelayed(slideRunnable, delayMillis)

        /**
         * 상단바 메뉴 클릭
         */
        val toolbarImage: ImageButton = view.findViewById(R.id.toolbarImageButton)
        toolbarImage.setOnClickListener {
            /**
             * 밑에서 올라오는 슬라이드
             */
            val bottomSheetFragment = SettingCustomBottomSheetDialogFragment()
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }


        /**
         * 최근 10개 등록 상품
         */
        // Call this after initializing your viewModel
        productViewModel.fetchRecentProducts()
        val adapterTen = ProductViewPagerAdapter(this@HomeFragment, myId, emptyList())
        val recyclerViewTen = binding.productRecentTen
        recyclerViewTen.adapter = adapterTen
        recyclerViewTen.setHasFixedSize(true)
        // 어댑터에 조건을 추가해서 넣어서 해보기
        recyclerViewTen.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        productViewModel.recentProducts.observe(viewLifecycleOwner, Observer { products ->
            // Here you can update your RecyclerView Adapter
            adapterTen.setData(products)
        })

        /**
         * 가장 많은 하트 상품
         */
        productViewModel.fetchProductsSortedByHeartCount()
        val adapterHt = ProductViewPagerAdapter(this@HomeFragment, myId, emptyList())
        val recyclerViewHt = binding.productHeart
        recyclerViewHt.adapter = adapterHt
        recyclerViewHt.setHasFixedSize(true)
        // 어댑터에 조건을 추가해서 넣어서 해보기
        recyclerViewHt.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        productViewModel.productsSortedByHeartCount.observe(viewLifecycleOwner, Observer { products ->
            Log.d("13131313134", products.toString())
            adapterHt.setData(products)
        })

        /**
         * 가장 많은 View 상품
         */
        productViewModel.fetchProductsSortedByViewCount()
        val adapterView = ProductViewPagerAdapter(this@HomeFragment, myId, emptyList())
        val recyclerViewView = binding.productViewCount
        recyclerViewView.adapter = adapterView
        recyclerViewView.setHasFixedSize(true)
        // Change from LinearLayoutManager to GridLayoutManager
//        recyclerViewView.layoutManager = GridLayoutManager(requireContext(), 5)
        recyclerViewView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        productViewModel.productsSortedByViewCount.observe(viewLifecycleOwner, Observer { products ->
            adapterView.setData(products)
        })

        /**
         * 일주일 내에 가장 많은 Heart 상품
         */
        productViewModel.fetRecentProductHeartCount()
        val adapterSevenHeart = ProductViewPagerAdapter(this@HomeFragment, myId, emptyList())
        val recyclerViewSevenHeart = binding.productRecentHeart
        recyclerViewSevenHeart.adapter = adapterSevenHeart
        recyclerViewSevenHeart.setHasFixedSize(true)
        // Change from LinearLayoutManager to GridLayoutManager
//        recyclerViewSevenHeart.layoutManager = GridLayoutManager(requireContext(), 5)
        recyclerViewSevenHeart.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        productViewModel.productRecentByHeartCount.observe(viewLifecycleOwner, Observer { products ->
            adapterSevenHeart.setData(products)
        })

        /**
         * 일주일 내에 가장 많은 View 상품
         */
        productViewModel.fetRecentProductViewCount()
        val adapterSevenView = ProductViewPagerAdapter(this@HomeFragment, myId, emptyList())
        val recyclerViewSevenView = binding.productRecentView
        recyclerViewSevenView.adapter = adapterSevenView
        recyclerViewSevenView.setHasFixedSize(true)
        // Change from LinearLayoutManager to GridLayoutManager
//        recyclerViewSevenView.layoutManager = GridLayoutManager(requireContext(), 5)
        recyclerViewSevenView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        productViewModel.productRecentByViewCount.observe(viewLifecycleOwner, Observer { products ->
            adapterSevenView.setData(products)
        })

        /**
         * 이건 뷰페이저로 만들꺼임
         * 뷰모델 연결, 뷰모델을 불러옴
         * 이건 전체 상품
         * 따라서 나눠야 함
         * 1. adapter를 나누고 표현되어야 함
         **/
//        productViewModel.liveTodoData.observe(viewLifecycleOwner, Observer { product ->
//            /* ViewModel에 Observe를 활용하여 productViewModel에 ReadAllData 라이브 데이터가 바뀌었을때 캐치하여, adapter에서 만들어준 setData함수를 통해 바뀐데이터를 UI에 업데이트 해줌 */
//            adapterHt.setData(product)
//            adapterView.setData(product)
//            adapterSevenHeart.setData(product)
//            adapterSevenView.setData(product)
//            Log.d("111313", "1313")
//        })

        recyclerViewTen.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                Log.d("데이터 로드1", "ㅁㅁㄴㅇ")

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                Log.d("데이터 로드1", visibleItemCount.toString())
                Log.d("데이터 로드1", totalItemCount.toString())
                Log.d("데이터 로드1", firstVisibleItemPosition.toString())
                Log.d("데이터 로드1", dy.toString())

                if (dy > 0 && (firstVisibleItemPosition + visibleItemCount >= totalItemCount - 3)) {
                    // 로딩 인디케이터 표시 (옵션)
                    //        showLoadingIndicator()

                    // 다음 페이지의 데이터 로드
                    productViewModel.liveTodoData.observe(viewLifecycleOwner, Observer { newItems ->
                        // 데이터를 현재의 어댑터에 추가
                        adapterTen.addData(newItems)
                        adapterTen.notifyDataSetChanged()

                        // 로딩 인디케이터 숨기기 (옵션)
                        //            hideLoadingIndicator()
                    })
                }
            }
        })


        // 각 어댑터에 클릭 리스너 설정
        setupItemClickListener(adapterTen)
        setupItemClickListener(adapterHt)
        setupItemClickListener(adapterView)
        setupItemClickListener(adapterSevenHeart)
        setupItemClickListener(adapterSevenView)

        /**
         * 알림버튼 클릭 시
         */
        binding.notificationButton.setOnClickListener {
            Log.d("adad111", "notification")
            val popupView = layoutInflater.inflate(R.layout.notification_popup, null)
            val notificationRecyclerView = popupView.findViewById<RecyclerView>(R.id.notificationRecyclerView)

            val notifications = listOf("새로운 상품이 등록되었습니다.",
                "주문한 상품이 발송되었습니다.",
                "문의하신 상품에 대한 답변이 도착했습니다.",
                "친구가 새로운 상품을 찜하였습니다.",
                "할인 행사가 시작되었습니다! 지금 확인하세요.",
                "리뷰에 대한 답글이 도착했습니다.",
                "구매하신 상품의 교환 신청이 완료되었습니다.",
                "상품 리뷰를 작성해주세요.",
                "누군가의 상품에 새로운 댓글이 있습니다.",
                "내 상품에 새로운 문의가 등록되었습니다.")  // 여기에 알림 데이터를 제공하셔야 합니다.
            val notificationAdapter = NotificationAdapter(notifications)  // 알림용 어댑터는 별도로 정의하셔야 합니다.

            notificationRecyclerView.adapter = notificationAdapter
            notificationRecyclerView.layoutManager = LinearLayoutManager(context)

            val itemHeight = dpToPx(requireContext(), 20f) /* 아이템의 높이 (예: 50dp를 픽셀로 변환한 값) */;
            val popupHeight = itemHeight * 10;

            val popupWindow = PopupWindow(
                popupView,
                WindowManager.LayoutParams.WRAP_CONTENT,
                popupHeight
            )

            popupWindow.isFocusable = true  // 외부를 탭할 때 Popup을 닫기 위해
            popupWindow.showAsDropDown(notificationButton)  // 알림 버튼 바로 아래에 표시
            popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.popup_background))
        }

        // Inflate the layout for this fragment
        return view
    }

    fun dpToPx(context: Context, dp: Float): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        Log.d("jecesAddFragment", "onViewStateRestored")
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onStart() {
        Log.d("jecesAddFragment", "onStart")
        super.onStart()
    }

    override fun onResume() {
        Log.d("jecesAddFragment", "onResume")
        super.onResume()
    }

    override fun onPause() {
        Log.d("jecesAddFragment", "onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.d("jecesAddFragment", "onStop")
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d("jecesAddFragment", "onSaveInstanceState")
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        Log.d("jecesAddFragment", "onDestroyView")
        super.onDestroyView()
    }

    override fun onDestroy() {
        Log.d("jecesAddFragment", "onDestroy")
        super.onDestroy()
    }

    override fun onDetach() {
        Log.d("jecesAddFragment", "onDetach")
        super.onDetach()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onClick(images: ArrayList<String>, position: Int) {
        Log.d("클릭클릭", "클릭")
    }


}