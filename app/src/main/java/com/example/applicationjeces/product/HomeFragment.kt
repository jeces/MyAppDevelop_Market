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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.applicationjeces.R
import com.example.applicationjeces.databinding.FragmentHomeBinding
import com.example.applicationjeces.user.LoginFragment
import com.google.firebase.firestore.DocumentSnapshot
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

        setupViewPager(
            binding.productRecentTenViewPager,
            productViewModel.recentProducts,
            productViewModel::fetchRecentProducts,
            repository,
            binding.dotsIndicatorRecentTen
        )

        setupViewPager(
            binding.productHeartViewPager,
            productViewModel.productsSortedByHeartCount,
            productViewModel::fetchProductsSortedByHeartCount,
            repository,
            binding.dotsIndicatorHeart
        )

        setupViewPager(
            binding.productViewCountViewPager,
            productViewModel.productsSortedByViewCount,
            productViewModel::fetchProductsSortedByViewCount,
            repository,
            binding.dotsIndicatorViewCount
        )

        setupViewPager(
            binding.productRecentHeartViewPager,
            productViewModel.productRecentByHeartCount,
            productViewModel::fetRecentProductHeartCount,
            repository,
            binding.dotsIndicatorRecentHeart
        )

        setupViewPager(
            binding.productRecentViewtViewPager,
            productViewModel.productRecentByViewCount,
            productViewModel::fetRecentProductViewCount,
            repository,
            binding.dotsIndicatorRecentView
        )

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

    private fun setupViewPager(
        viewPager: ViewPager2,
        liveData: LiveData<List<DocumentSnapshot>>,
        viewModelFunction: () -> Unit,
        repository: ProductRepository,
        dotsIndicator: DotsIndicator
    ) {
        // Call viewModel function
        viewModelFunction()

        // Set up the ViewPager with a RecyclerView inside
        setupViewPagerWithRecyclerView(viewPager, liveData, repository)

        // Attach the DotsIndicator to the ViewPager2
        dotsIndicator.setViewPager2(viewPager)
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    private fun setupViewPagerWithRecyclerView(
        viewPager: ViewPager2,
        liveData: LiveData<List<DocumentSnapshot>>,
        repository: ProductRepository
    ) {
        val pagerAdapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val view = inflater.inflate(R.layout.page_recycler_view, parent, false)
                return MyViewHolder(view)
            }

            override fun getItemCount(): Int {
                val totalProducts = liveData.value?.size ?: 0
                return (totalProducts + 8) / 9  // For 3x3 grid
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val recyclerView = holder.itemView.findViewById<RecyclerView>(R.id.pageRecyclerView)
                recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
                val adapter = ProductViewPagerAdapter(this@HomeFragment, repository)
                recyclerView.adapter = adapter

                val start = position * 9
                val end = Math.min(start + 9, liveData.value?.size ?: 0)
                val sublist = liveData.value?.subList(start, end)

                adapter.setData(sublist ?: emptyList())
                setupItemClickListener(adapter) // Set item click listener
            }
        }

        viewPager.adapter = pagerAdapter

        liveData.observe(viewLifecycleOwner, Observer { products ->
            pagerAdapter.notifyDataSetChanged()
        })
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