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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.applicationjeces.R
import com.example.applicationjeces.databinding.FragmentMyinfoBinding
import com.example.applicationjeces.product.InfoActivity
import com.example.applicationjeces.product.ProductRepository
import com.example.applicationjeces.product.ProductViewModel
import com.example.applicationjeces.product.ProductViewPagerAdapter
import com.example.applicationjeces.user.EditProfileActivity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
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
         * 나의 판매목록 상품
         */

        setupViewPager(
            binding.productMyViewPager,
            productViewModel.myProductLiveTodoData,
            { productViewModel.mySetProduct(myId) },
            repository,
            binding.dotsIndicatorProductMy
        )

        /**
         * 나의 판매목록 버튼
         */
        binding.myProductSale.setOnClickListener {
            if (productMyViewPager.visibility == View.GONE) {
                productMyViewPager.visibility = View.VISIBLE
                dotsIndicatorProductMy.visibility = View.VISIBLE
                val layoutParams = binding.ProductPc.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.topToBottom = R.id.dotsIndicatorProductMy
                binding.ProductPc.layoutParams = layoutParams
            } else {
                productMyViewPager.visibility = View.GONE
                dotsIndicatorProductMy.visibility = View.GONE
                val layoutParams = binding.ProductPc.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.topToBottom = R.id.View1
                binding.ProductPc.layoutParams = layoutParams
            }
        }

        /**
         * 나의 구매 목록
         */
        setupViewPager(
            binding.productPcViewPager,
            productViewModel.myProductLiveTodoData,
            { productViewModel.mySetProduct("") },
            repository,
            binding.dotsIndicatorProductMy
        )

        /**
         * 나의 구매목록 버튼
         */
        binding.ProductPc.setOnClickListener {
            if ((productPcViewPager.visibility == View.GONE)) {
                productPcViewPager.visibility = View.VISIBLE
                dotsIndicatorPc.visibility = View.VISIBLE
                val layoutParams = binding.ProductFv.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.topToBottom = R.id.dotsIndicatorPc
                binding.ProductFv.layoutParams = layoutParams
            } else {
                productPcViewPager.visibility = View.GONE
                dotsIndicatorPc.visibility = View.GONE
                val layoutParams = binding.ProductFv.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.topToBottom = R.id.View2
                binding.ProductFv.layoutParams = layoutParams
            }
        }

        /**
         * 나의 관심 목록
         */
        setupViewPager(
            binding.productFvViewPager,
            productViewModel.myProductFvLiveTodoData,
            productViewModel::mySetProductFv,
            repository,
            binding.dotsIndicatorProductMy
        )

        /**
         * 나의 관심목록 버튼
         */
        binding.ProductFv.setOnClickListener {
            if ((productFvViewPager.visibility == View.GONE)) {
                productFvViewPager.visibility = View.VISIBLE
                dotsIndicatorFv.visibility = View.VISIBLE
                val layoutParams = binding.ReviewText.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.topToBottom = R.id.dotsIndicatorFv
                binding.ReviewText.layoutParams = layoutParams
            } else {
                productFvViewPager.visibility = View.GONE
                dotsIndicatorFv.visibility = View.GONE
                val layoutParams = binding.ReviewText.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.topToBottom = R.id.View3
                binding.ReviewText.layoutParams = layoutParams
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
                val adapter = ProductViewPagerAdapter(this@MyFragment, repository)
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
            Log.d("들어는 왔냐", "ㅁㅁㅁㅁㅁㅁ")
            pagerAdapter.notifyDataSetChanged()
        })
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