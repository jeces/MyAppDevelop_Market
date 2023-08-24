package com.example.applicationjeces.product

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.applicationjeces.MainActivity
import com.example.applicationjeces.R
import com.example.applicationjeces.databinding.FragmentInfoBinding
import com.google.firebase.storage.FirebaseStorage
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import kotlinx.android.synthetic.main.chat_right_item_list.view.view3_2
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class InfoFragment : Fragment(), ProductImageInfoRecyclerViewAdapter.OnImageClickListener {

    /* 이미지 리스트 */
    var imagelist = ArrayList<String>()
    private lateinit var productViewModel: ProductViewModel
    private var _binding: FragmentInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /**
         * view 바인딩
         */
        _binding = FragmentInfoBinding.inflate(inflater, container, false)
        val view = binding.root

        /**
         * 뷰모델 초기화 생성자
         **/
        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        /**
         * 자신의 위치 이동 저장
         */
        val pId = arguments?.getString("ID").toString()
        val pName = arguments?.getString("productName").toString()
        val productPrice = arguments?.getString("productPrice")
        val productDescription = arguments?.getString("productDescription")
        val productCount = arguments?.getString("productCount")
        val pChatCount = arguments?.getString("pChatCount")
        val pViewCount = arguments?.getString("pViewCount")
        val pHeartCount = arguments?.getString("pHeartCount")
//        val productBidPrices = arguments?.getString("productBidPrice")
//        val position = arguments?.getString("position", -1)
        val myId: String = productViewModel.thisUser.toString()
        val pIdx: String = arguments?.getString("IDX").toString()
        productViewModel.whereMyUser("productInfo")

        binding.productName.text = pName

        /**
         * 상대방 닉네임 가져오기
         */
        productViewModel.fetchProductNickName(pId)
        productViewModel.productNickName.observe(viewLifecycleOwner, Observer { productNick ->
//            binding.productBidPrice.text = "₩ " + addCommasToNumberString(productBidPrices.toString()) + "원"
            binding.sellerName.text = productNick
        })


        /**
         * 입찰가 업데이트(상시)
         */
        productViewModel.startListeningForBidUpdates(pId, pName)
        productViewModel.bidPrice.observe(viewLifecycleOwner, Observer { productBidPrices ->
//            binding.productBidPrice.text = "₩ " + addCommasToNumberString(productBidPrices.toString()) + "원"
            val formattedBidPrice = addCommasToNumberString(productBidPrices.toString())
            binding.productBidPrice.text = getString(R.string.bid_price_format, formattedBidPrice)
        })

        val formattedPrice = addCommasToNumberString(productPrice.toString())
        binding.productCellPrice.text = getString(R.string.product_price_format, formattedPrice)
//        binding.productCellPrice.text = "₩ " + addCommasToNumberString(productPrice.toString()) + "원"
        binding.productDetailDescription.text = productDescription
        binding.productChatText.text = pChatCount
        binding.productViewText.text = pViewCount
        binding.productCheckText.text = pHeartCount

        /**
         * 판매자 프로필 이미지
         */
        setYourImage(pId)

        /**
         * 맨 위 상품 이미지
         * viewpager2 adapter 장착
         */
        val viewPager = binding.viewPagerInfoProduce
        val adapterImg = ProductImageInfoRecyclerViewAdapter(myId, pId, pName, imagelist, requireActivity(), this)
        viewPager.adapter = adapterImg

        /**
         * 상품 이미지
         */
        if (productCount != null) {
            productViewModel.getImage(pId, pName, productCount.toInt()).observe(viewLifecycleOwner, Observer { list ->
                adapterImg.updateData(list)  // 어댑터에 데이터가 변경되었음을 알립니다.
            })
        }

        /**
         * indicator 장착
         */
        val dotsIndicator: DotsIndicator = binding.dotsIndicatorInfo
        val viewPager2: ViewPager2 = binding.viewPagerInfoProduce
        dotsIndicator.setViewPager2(viewPager2)

        /**
         * ViewCount ++함
         * 단 자기 자신은 올리지 않음
         */
        if (myId != pId) productViewModel.viewCountUp(pId, pName)

        /**
         * 자기 자신 버튼 숨김
         */
        if (myId == pId) {
            binding.iconTextSection.visibility = View.INVISIBLE
        }

        /**
         * check되어있는지 확인하기
         */
        // LiveData 가져오기
        val productExistLiveData = productViewModel.isCheckProduct(myId, pIdx)
        // LiveData 관찰하기
        productExistLiveData.observe(viewLifecycleOwner, Observer { exists ->
            Log.d("13131313", exists.toString())
            binding.productCheck.isSelected = exists
        })

        /**
         * check 버튼
         */
        binding.productCheck.setOnClickListener {
            /**
             * 체크 안되어있다면
             */
            it.isSelected = !it.isSelected
            if(it.isSelected) {
                productViewModel.setMyFavorit(pIdx)
            } else {
                //지워야 함
                productViewModel.removeMyFavorit(pIdx)
            }
        }

        /**
         * 뒤로가기버튼 누를시
         **/
        binding.backBt.setOnClickListener {
            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish() // 현재 Activity를 종료하고 싶다면 추가
            requireActivity().overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left)
        }

        return view
    }

    /**
     * 항목 풀 스크린
     **/


//
//    override fun onBackPressed() {
//        super.onBackPressed()
//        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
//    }
    companion object {
        // InfoFragment 인스턴스 생성 및 데이터 전달을 위한 newInstance 메서드
        fun newInstance(pId: String, pName: String, /* 다른 매개변수들 */): InfoFragment {
            val fragment = InfoFragment()
            val args = Bundle()
            args.putString("ID", pId)
            args.putString("productName", pName)
            // ... 나머지 데이터도 이와 같은 방식으로 저장
            fragment.arguments = args
            return fragment
        }
    }
    override fun onClick(images: ArrayList<String>, position: Int, myId: String, pName: String) {
        val fragment = FullscreenImageFragment.newInstance(images, position, myId, pName)
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    /**
     * 판매자 프로필 이미지
     */
    fun setYourImage(pId: String) {
        var db = FirebaseStorage.getInstance()
        db.reference.child("${pId}/profil/${pId}_Profil_IMAGE_.png").downloadUrl.addOnCompleteListener {
            if(it.isSuccessful) {
                Glide.with(this@InfoFragment)
                    .load(it.result)
                    .override(70, 70)
                    .fitCenter()
                    .circleCrop() // 또는 .transform(RoundedCorners(radius)) 를 사용하여 모서리의 반경을 설정
                    .into(binding.sellerImage)
            } else {
                db.reference.child("basic_user.png").downloadUrl.addOnCompleteListener { its ->
                    Glide.with(this@InfoFragment)
                        .load(it.result)
                        .override(70, 70)
                        .fitCenter()
                        .circleCrop() // 또는 .transform(RoundedCorners(radius)) 를 사용하여 모서리의 반경을 설정
                        .into(binding.sellerImage)
                }
            }
        }.addOnFailureListener {

        }
    }

    /**
     * 단위 (,) 찍기
     */
    fun addCommasToNumberString(numberString: String): String {
        val number = numberString.replace(",", "").toLongOrNull()
        return if (number != null) {
            NumberFormat.getNumberInstance(Locale.US).format(number)
        } else {
            "" // 또는 원하는 기본값을 반환합니다.
        }
    }

}
