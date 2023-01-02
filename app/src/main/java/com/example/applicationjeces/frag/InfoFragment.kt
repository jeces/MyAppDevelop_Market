package com.example.applicationjeces.frag

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applicationjeces.R
import com.example.applicationjeces.product.ProductImageInfoRecyclerViewAdapter
import com.example.applicationjeces.JecesViewModel
import com.example.applicationjeces.product.Response
import kotlinx.android.synthetic.main.fragment_add.view.*
import kotlinx.android.synthetic.main.fragment_info.*
import kotlinx.android.synthetic.main.fragment_info.view.*

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
class InfoFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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

        val view = inflater.inflate(R.layout.fragment_info, container, false)

        val jecesModel: JecesViewModel by activityViewModels()
        view.productDetailName.setText(jecesModel.productArrayList[0].product_name)
        view.productDetailPrice.setText(jecesModel.productArrayList[0].product_price)
        view.productDetailDescription.setText(jecesModel.productArrayList[0].product_description)

        Log.d("infocount", jecesModel.productArrayList[0].product_count.toString())

        imagelist = jecesModel.getImage(jecesModel.productArrayList[0].product_name, jecesModel.productArrayList[0].product_count) as ArrayList<String>

        /* 이미지 어뎁터 */
        val adapter = ProductImageInfoRecyclerViewAdapter(imagelist, this@InfoFragment)

        /* 이미지 리사이클러뷰 어뎁터 장착 */
        val recyclerView =  view!!.imginfo_profile
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        view.chat_start_btn.setOnClickListener {
            /**
             * 1. 채팅하기를 누르면 일단 채팅목록에 해당 상대방과의 채팅이 있는지 검색
             * 2. 있으면 그 채팅화면을 띄워주고
             * 3. 없으면 채팅만 생성하고 화면을 만듬(채팅룸을 만들지는 않음)
             * 4. 단 채팅이 없을 경우 다른 화면으로 이동 시 채팅내용 삭제하기
             * 5. 채팅을 칠 경우 채팅룸 생성
             * */
            jecesModel.searchChat(jecesModel.productArrayList[0].product_id).observe(viewLifecycleOwner, { chat ->
                Log.d("asdfasdf", chat.toString())

            })
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
            InfoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}