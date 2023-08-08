package com.example.applicationjeces.frag

import android.content.Context
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.applicationjeces.MainActivity
import com.example.applicationjeces.R
import com.example.applicationjeces.page.DataViewModel
import com.example.applicationjeces.page.PageData
import com.example.applicationjeces.JecesViewModel
import com.example.applicationjeces.chat.ChatViewModel
import com.example.applicationjeces.databinding.FragmentChatBinding
import com.example.applicationjeces.databinding.FragmentHomeBinding
import com.example.applicationjeces.product.*
import com.example.applicationjeces.user.LoginActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
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
         * adver 이미지 가져오기
         */
        adverImagelist = productViewModel.getAdverImage(4) as ArrayList<String>

        /**
         * 맨 위 소개 페이지
         * viewpager2 adapter 장착
         */
        val viewPager = binding.viewPagerHomeProduce
        val adapterVp = AdverRecyclerViewAdapter(adverImagelist, this@HomeFragment, this)
        viewPager.adapter = adapterVp
////        viewPager.currentItem = position

        /**
         * indicator 장착
         */
        val dotsIndicator: DotsIndicator = binding.dotsIndicator
        val viewPager2: ViewPager2 = binding.viewPagerHomeProduce
        dotsIndicator.setViewPager2(viewPager2)

        /**
         * 자동 슬라이드
         */
        productViewModel.getAdverCounts().observe(viewLifecycleOwner, Observer { filesCount ->
            println("Files count: $filesCount")
            adverPageCount = filesCount
        })
        val handler = Handler(Looper.getMainLooper())
        val update = Runnable {
            val currentItem = viewPager2.currentItem
            viewPager2.setCurrentItem((currentItem + 1) % adverPageCount, true)
        }

        val swipeTimer = Timer()
        swipeTimer.schedule(object : TimerTask() {
            override fun run() {
                handler.post(update)
            }
        }, 5000, 5000)

        /**
         * 상단바 메뉴
         */
        val toolbarImage: ImageButton = view.findViewById(R.id.toolbarImageButton)
        toolbarImage.setOnClickListener {
            Log.d("aaa123", "123")
            // val popupMenu = PopupMenu(this, it)
            val popupMenu = PopupMenu(requireActivity(), it) // 'this'를 'requireActivity()'로 변경
            popupMenu.inflate(R.menu.option_menu) // 메뉴 리소스 파일 설정
            popupMenu.setOnMenuItemClickListener { menuItem ->
                // 메뉴 아이템을 클릭했을 때 수행할 동작
                when (menuItem.itemId) {
                    R.id.logout -> {
                        /* 로그아웃 */
                        FirebaseAuth.getInstance().signOut()
                        /* 페이지 이동 */
                        // val it = Intent(this, LoginActivity::class.java)
                        val it = Intent(requireActivity(), LoginActivity::class.java) // 'this'를 'requireActivity()'로 변경
                        requireActivity().startActivity(it) // 'startActivity()' 앞에 'requireActivity().' 추가
                        true
                    }
                    // 다른 메뉴 아이템 처리
                    else -> false
                }
            }
            popupMenu.show()
        }

        /**
         * 최근 등록된 상품
         */
        val viewPagerRecent: ViewPager2 = binding.viewPagerHomeProduceRecent
        val adapterRp = ProductViewPagerAdapter(requireContext(), myId, emptyList())
        viewPagerRecent.adapter = adapterRp

        /* 뷰모델 연결, 뷰모델을 불러옴 */
        productViewModel.liveTodoData.observe(viewLifecycleOwner, Observer { product ->
            /* ViewModel에 Observe를 활용하여 productViewModel에 ReadAllData 라이브 데이터가 바뀌었을때 캐치하여, adapter에서 만들어준 setData함수를 통해 바뀐데이터를 UI에 업데이트 해줌 */
            Log.d("dkfflwksk", "ddd")
            adapterRp.setData(product)
        })

        /**
         *  항목 클릭시
         **/
        Log.d("aa11", "-1")
        adapterRp.setItemClickListener(object: ProductViewPagerAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                val productModel: JecesViewModel by activityViewModels()
                /* 상품 정보 불러오기 */
                productModel.liveTodoData.value?.get(position).toString()
                productModel.setProductDetail(adapterRp.producFiretList[position].get("ID").toString(), adapterRp.producFiretList[position].get("productName").toString(), adapterRp.producFiretList[position].get("productPrice").toString()
                    , adapterRp.producFiretList[position].get("productDescription").toString(), adapterRp.producFiretList[position].get("productCount").toString(), adapterRp.producFiretList[position].get("pChatCount").toString()
                    , adapterRp.producFiretList[position].get("pViewCount").toString(), adapterRp.producFiretList[position].get("pHeartCount").toString(), adapterRp.producFiretList[position].get("productBidPrice").toString(), position)

                /* InfoActivity로 화면 전환 */
                val intent = Intent(getActivity(), InfoActivity::class.java)
                /* 필요한 데이터를 InfoActivity로 전달하기 위한 인텐트 파라미터 설정 */
                intent.putExtra("ID", adapterRp.producFiretList[position].get("ID").toString())
                intent.putExtra("productName", adapterRp.producFiretList[position].get("productName").toString())
                intent.putExtra("productPrice", adapterRp.producFiretList[position].get("productPrice").toString())
                intent.putExtra("productDescription", adapterRp.producFiretList[position].get("productDescription").toString())
                intent.putExtra("productCount", adapterRp.producFiretList[position].get("productCount").toString())
                intent.putExtra("pChatCount", adapterRp.producFiretList[position].get("pChatCount").toString())
                intent.putExtra("pViewCount", adapterRp.producFiretList[position].get("pViewCount").toString())
                intent.putExtra("pHeartCount", adapterRp.producFiretList[position].get("pHeartCount").toString())
                intent.putExtra("productBidPrice", adapterRp.producFiretList[position].get("productBidPrice").toString())
                intent.putExtra("position", position)
                startActivity(intent)

                /* 애니메이션 적용 */
                activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        })























//
//        val recyclerView = view.rv_profile
//        recyclerView.adapter = adapter
//        recyclerView.setHasFixedSize(true)
//        recyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
//        //recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

//        /**
//         * 찜한 상품
//         */
//        Log.d("aa11", "-2")
//        val recyclerView2 = view.select_profile
//        recyclerView2.adapter = adapter
//        recyclerView2.setHasFixedSize(true)
//        recyclerView2.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)



//        /**
//         *  항목 클릭시
//         **/
//        adapter.setItemClickListener(object: ProductRecyclerViewAdapter.OnItemClickListener {
//            override fun onClick(v: View, position: Int) {
////                /* 화면 띄움*/
////                /* 프라그먼트에서 프라그먼트로 제어가 불가능하기 때문에 상위 액티비티에서 제어 해주어야 한다. */
////                /* ViewModel 가지고와서 PageLiveData 넘기기[업데이트 됨] */
////                val model: DataViewModel by activityViewModels()
////                model.changePageNum(PageData.DETAIL)
////
////                val productModel: JecesViewModel by activityViewModels()
////                productModel.liveTodoData.value?.get(position).toString()
////                productModel.setProductDetail(adapter.producFiretList[position].get("ID").toString(), adapter.producFiretList[position].get("productName").toString(), adapter.producFiretList[position].get("productPrice").toString()
////                    , adapter.producFiretList[position].get("productDescription").toString(), adapter.producFiretList[position].get("productCount").toString(), adapter.producFiretList[position].get("pChatCount").toString(), adapter.producFiretList[position].get("pViewCount").toString(), adapter.producFiretList[position].get("pHeartCount").toString(), adapter.producFiretList[position].get("productBidPrice").toString(), position)
////
////                /* Navigation Bar Selected 넘겨야 됨[여기서부터해야함] */
////                val mActivity = activity as MainActivity
////                mActivity.bottomNavigationView.menu.findItem(R.id.detail).isChecked = true
//
//                /* 화면 띄움 */
//                val intent = Intent(getActivity(), InfoActivity::class.java)
//                intent.putExtra("param1", adapter.producFiretList[position].get("ID").toString())
//                intent.putExtra("param2", adapter.producFiretList[position].get("productName").toString())
//                startActivity(intent)
//                Log.d("들어갔음?", "들어갔음?")
//            }
//        })






        // Inflate the layout for this fragment
        return view
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