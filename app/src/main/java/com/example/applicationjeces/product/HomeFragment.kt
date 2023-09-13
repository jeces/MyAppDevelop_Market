package com.example.applicationjeces.product

import SettingCustomBottomSheetDialogFragment
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageButton
import android.widget.PopupWindow
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.applicationjeces.R
import com.example.applicationjeces.databinding.FragmentHomeBinding
import com.example.applicationjeces.user.LoginFragment
import com.google.gson.Gson
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*
import kotlin.collections.ArrayList
/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment(), AdverRecyclerViewAdapter.OnImageClickListener {

    private lateinit var productViewModel: ProductViewModel
    private lateinit var notificationDao: NotificationDao
    private val repository = ProductRepository()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val slideRunnable = object : Runnable {
        override fun run() {
            val viewPager2 = binding.viewPagerHomeProduce
            if (adverPageCount != 0) {
                val nextItem = (viewPager2.currentItem + 1) % adverPageCount
                viewPager2.setCurrentItem(nextItem, true)
                handler.postDelayed(this, delayMillis)
            }
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val delayMillis = 3000L


    /* adver 이미지 리스트 */
    var adverImagelist = ArrayList<String>()

    /* adver 이미지 개수 */
    var adverPageCount = 0

    override fun onAttach(context: Context) {
        Log.d("jecesAddFragment", "onAttach")
        super.onAttach(context)
    }



    // 리스너 설정 함수
    fun setupItemClickListener(adapter: ProductViewPagerAdapter) {
        adapter.setItemClickListener(object : ProductViewPagerAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                val documentSnapshot = adapter.getDocumentSnapshotAt(position)
                val product = documentSnapshot.data as HashMap<String, Any>
                onProductClicked(product, position)
            }
        })
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
        val adapterTen = ProductViewPagerAdapter(this@HomeFragment, repository)
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
        val adapterHt = ProductViewPagerAdapter(this@HomeFragment, repository)
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
        val adapterView = ProductViewPagerAdapter(this@HomeFragment, repository)
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
        val adapterSevenHeart = ProductViewPagerAdapter(this@HomeFragment, repository)
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
        val adapterSevenView = ProductViewPagerAdapter(this@HomeFragment, repository)
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

        recyclerViewTen.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                Log.d("데이터 로드1", "ㅁㅁㄴㅇ")

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (dy > 0 && (firstVisibleItemPosition + visibleItemCount >= totalItemCount - 3)) {
                    // 로딩 인디케이터 표시 (옵션)
                    //        showLoadingIndicator()

                    // 다음 페이지의 데이터 로드
                    // 수정 후:
                    productViewModel.liveTodoData.observe(viewLifecycleOwner, Observer { newItems ->
                        // 변경된 데이터만 추가
                        val currentSize = adapterTen.itemCount
                        adapterTen.submitList(newItems)
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
        // 아래의 "yourUserId"는 실제 사용자 ID로 교체해야 합니다.
        productViewModel.notificationsProduct(myId, requireContext())
        val notificationDatabase = NotificationDatabase.getDatabase(requireContext())
        notificationDao = notificationDatabase.notificationDao()
        productViewModel.newNotification.observe(viewLifecycleOwner, Observer { notification ->
            Log.d("adad111", "notification")
            sendNotification(notification.title, notification.message)
        })

        binding.notificationButton.setOnClickListener {
            val popupView = layoutInflater.inflate(R.layout.notification_popup, null)
            val notificationRecyclerView = popupView.findViewById<RecyclerView>(R.id.notificationRecyclerView)
            notificationRecyclerView.layoutManager = LinearLayoutManager(context)

            val notificationAdapter = NotificationAdapter(emptyList())  // 초기 알림 목록은 비어 있습니다.
            notificationRecyclerView.adapter = notificationAdapter

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

            // LiveData를 사용하여 알림 데이터를 관찰
            NotificationDatabase.getDatabase(requireContext()).notificationDao().getAllNotifications().observe(viewLifecycleOwner) { notifications ->
                // 데이터베이스에서 가져온 알림으로 어댑터 업데이트
                notificationAdapter.setNotifications(notifications)
            }
        }


        // Inflate the layout for this fragment
        return view
    }

    // 공통으로 사용할 항목 클릭 메서드
    fun onProductClicked(product: HashMap<String, Any>, position: Int) {
        val gson = Gson()
        val tags = listOf(product["tags"]) as List<String>
        productViewModel.setProductDetail(
            product["ID"].toString(),
            product["productName"].toString(),
            product["productPrice"].toString().toInt(),
            product["productDescription"].toString(),
            product["productCount"].toString().toInt(),
            product["pChatCount"].toString().toInt(),
            product["pViewCount"].toString().toInt(),
            product["pHeartCount"].toString().toInt(),
            product["productBidPrice"].toString(),
            product["insertTime"].toString(), position,
            tags, product["category"].toString(),
            product["state"].toString()
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

    /* 알림 */
    private fun sendNotification(title: String, message: String) {
        val builder = NotificationCompat.Builder(requireActivity(), "MY_channel")
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setContentTitle(title)
            .setContentText(message)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel_id = "MY_channel"
            val channel_name = "채널이름"
            val descriptionText = "설명글"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channel_id, channel_name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager = requireActivity().getSystemService(
                NOTIFICATION_SERVICE
            ) as NotificationManager

            notificationManager.createNotificationChannel(channel)
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }


    fun dpToPx(context: Context, dp: Float): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수를 방지하기 위한 뷰 바인딩 해제
    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }

    override fun onClick(images: ArrayList<String>, position: Int) {
        Log.d("클릭클릭", "클릭")
    }


}