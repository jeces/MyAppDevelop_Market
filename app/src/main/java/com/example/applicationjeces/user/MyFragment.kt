package com.example.applicationjeces.user

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.applicationjeces.databinding.FragmentMyinfoBinding
import com.example.applicationjeces.product.ProductViewModel
import com.example.applicationjeces.product.ProductViewPagerAdapter
import com.example.applicationjeces.user.EditProfileActivity
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_info.view.*
import kotlinx.android.synthetic.main.fragment_myinfo.*

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
        var myId = productViewModel.thisUser.toString()
        setMyImage(myId)
        binding.profileName.text = myId



        /**
         * 나의 판매목록 상품
         */
        val adapter = MyProductRecyclerViewAdapter(this@MyFragment, myId, emptyList())
        val recyclerView = binding.myProductSaleV
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        // Change from LinearLayoutManager to GridLayoutManager
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 5)

        /**
         * 이건 뷰페이저로 만들꺼임
         * 뷰모델 연결, 뷰모델을 불러옴
         * 이건 전체 상품
         * 따라서 나눠야 함
         * 1. adapter를 나누고 표현되어야 함
         **/
        productViewModel.liveTodoData.observe(viewLifecycleOwner, Observer { product ->
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



        binding.editProfile.setOnClickListener {
            val intent = Intent(activity, EditProfileActivity::class.java)
            startActivity(intent)
        }

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
                db.reference.child("basic_user.png").downloadUrl.addOnCompleteListener { its ->
                    Glide.with(this@MyFragment)
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