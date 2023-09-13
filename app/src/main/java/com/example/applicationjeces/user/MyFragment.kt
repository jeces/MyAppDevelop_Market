package com.example.applicationjeces.user

import ReviewAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.applicationjeces.R
import com.example.applicationjeces.databinding.FragmentMyinfoBinding
import com.example.applicationjeces.product.InfoActivity
import com.example.applicationjeces.product.ProductRepository
import com.example.applicationjeces.product.ProductViewModel
import com.example.applicationjeces.product.ProductViewPagerAdapter
import com.example.applicationjeces.user.EditProfileActivity
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_info.view.*
import kotlinx.android.synthetic.main.fragment_myinfo.*
import java.util.HashMap

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [InfoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

/* https://greensky0026.tistory.com/224 */
/* viewpager2 이미지 슬라이더 사용하기 */
/* 디테일 */
class MyFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var productViewModel: ProductViewModel
    val repository = ProductRepository()
    private var _binding: FragmentMyinfoBinding? = null
    private val binding get() = _binding!!

    /* 이미지 리스트 */
    var imagelist = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        /**
         * view 바인딩
         */
        _binding = FragmentMyinfoBinding.inflate(inflater, container, false)
        val view = binding.root

        /**
         * 뷰모델 초기화 생성자
         **/
        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        /**
         * 나의 프로필 이미지 및 이름
         */
        var myId = productViewModel.thisUser
        setMyImage(myId)

        /**
         * 화면 폭 계산
         * GridLayout 계산
         */
        val displayMetrics = resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        val numberOfColumns = (dpWidth / 100).toInt()

        /**
         * 프로필 이름 옵저버
         */
        productViewModel.fetchUserName("")
        productViewModel.nickName.observe(viewLifecycleOwner, Observer { nick->
            binding.profileName.text = nick
        })

        /**
         * 나의 판매목록 상품
         */
//        val adapter = MyProductRecyclerViewAdapter(this@MyFragment, myId, emptyList(), "myProduct")
        val adapter = ProductViewPagerAdapter(this@MyFragment, repository)
        val recyclerView = binding.myProductSaleV
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        // Change from LinearLayoutManager to GridLayoutManager
        recyclerView.layoutManager = GridLayoutManager(requireContext(), numberOfColumns)

        /**
         * 나의 판매 개수
         */
        productViewModel.fetMyProductCellCount()
        productViewModel.productMyCellCount.observe(viewLifecycleOwner, Observer { count ->
            binding.ProductCellCount.text = count.toString()
        })

        /**
         * 나의 찜한 개수
         */
        productViewModel.fetMyProductHeartCount()
        productViewModel.productMyHeartCount.observe(viewLifecycleOwner, Observer { count ->
            binding.ProductHeartCount.text = count.toString()
        })

        /**
         * 옵져브
         **/
        productViewModel.mySetProduct("")
        productViewModel.myProductLiveTodoData.observe(viewLifecycleOwner, Observer { product ->
            /* ViewModel에 Observe를 활용하여 productViewModel에 ReadAllData 라이브 데이터가 바뀌었을때 캐치하여, adapter에서 만들어준 setData함수를 통해 바뀐데이터를 UI에 업데이트 해줌 */
            adapter.setData(product)
        })

        /**
         * 나의 판매목록 버튼
         */
        binding.myProductSale.setOnClickListener {
            if (recyclerView.visibility == View.VISIBLE) {
                recyclerView.visibility = View.GONE
            } else {
                recyclerView.visibility = View.VISIBLE
            }
        }

        /**
         * 나의 구매 목록
         */
//        val adapterPc = MyProductRecyclerViewAdapter(this@MyFragment, myId, emptyList(), "myProductPc")
        val adapterPc = ProductViewPagerAdapter(this@MyFragment, repository)
        val recyclerViewPc = binding.myProductPc
        recyclerViewPc.adapter = adapterPc
        recyclerViewPc.setHasFixedSize(true)
        // Change from LinearLayoutManager to GridLayoutManager
        recyclerViewPc.layoutManager = GridLayoutManager(requireContext(), numberOfColumns)


        binding.editProfile.setOnClickListener {
            val intent = Intent(activity, EditProfileActivity::class.java)
            startActivity(intent)
        }

        /**
         * 나의 구매목록 버튼
         */
        binding.ProductPc.setOnClickListener {
            if (recyclerViewPc.visibility == View.VISIBLE) {
                recyclerViewPc.visibility = View.GONE
            } else {
                recyclerViewPc.visibility = View.VISIBLE
            }
        }

        /**
         * 나의 관심 목록
         */
        val adapterFv = ProductViewPagerAdapter(this@MyFragment, repository)
        val recyclerViewFv = binding.myProductFv
        recyclerViewFv.adapter = adapterFv
        recyclerViewFv.setHasFixedSize(true)
        // Change from LinearLayoutManager to GridLayoutManager
        recyclerViewFv.layoutManager = GridLayoutManager(requireContext(), numberOfColumns)

        /**
         * 옵져브
         **/
        productViewModel.mySetProductFv()
        productViewModel.myProductFvLiveTodoData.observe(viewLifecycleOwner, Observer { product ->
            /* ViewModel에 Observe를 활용하여 productViewModel에 ReadAllData 라이브 데이터가 바뀌었을때 캐치하여, adapter에서 만들어준 setData함수를 통해 바뀐데이터를 UI에 업데이트 해줌 */
            Log.d("aaaaddd", "aadada")
            adapterFv.setData(product)
        })

        /**
         * 나의 관심목록 버튼
         */
        binding.ProductFv.setOnClickListener {
            if (recyclerViewFv.visibility == View.VISIBLE) {
                recyclerViewFv.visibility = View.GONE
            } else {
                recyclerViewFv.visibility = View.VISIBLE
            }
        }

        /**
         * 버튼 클릭
         */
        binding.ReviewText.setOnClickListener {
            if (reviewRecyclerView.visibility == View.VISIBLE) {
                reviewRecyclerView.visibility = View.GONE
            } else {
                reviewRecyclerView.visibility = View.VISIBLE
            }
        }

        val reviewAdapter = ReviewAdapter(emptyList())
        val reviewRecyclerView = binding.reviewRecyclerView
        reviewRecyclerView.adapter = reviewAdapter

        binding.editProfile.setOnClickListener {
            val intent = Intent(activity, EditProfileActivity::class.java)
            startActivity(intent)
        }

        setupItemClickListener(adapter)
        setupItemClickListener(adapterFv)
        setupItemClickListener(adapterPc)

        // Inflate the layout for this fragment
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment InfoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
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

    /**
     * 나의 프로필 이미지
     */
    fun setMyImage(myId: String) {
        var db = FirebaseStorage.getInstance()
        db.reference.child("${myId}/profil/${myId}_Profil_IMAGE_.png").downloadUrl.addOnCompleteListener {
            if(it.isSuccessful) {
                Glide.with(this@MyFragment)
                    .load(it.result)
                    .override(70, 70)
                    .fitCenter()
                    .circleCrop() // 또는 .transform(RoundedCorners(radius)) 를 사용하여 모서리의 반경을 설정
                    .into(binding.profileImage)
            } else {
                db.reference.child("common/basic_user.png").downloadUrl.addOnCompleteListener { its ->
                    Glide.with(this@MyFragment)
                        .load(its.result)
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