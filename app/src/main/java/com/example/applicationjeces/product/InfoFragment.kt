package com.example.applicationjeces.product

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applicationjeces.R
import com.example.applicationjeces.product.ProductImageInfoRecyclerViewAdapter
import com.example.applicationjeces.JecesViewModel
import com.example.applicationjeces.chat.ChatActivity
import com.example.applicationjeces.chat.ChatroomData
import com.google.firebase.Timestamp
import kotlinx.android.synthetic.main.fragment_bid_dialog.view.*
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

        /**
         * 자신의 위치 이동 저장
         */
        jecesModel.whereMyUser("productInfo")

        var pId: String = jecesModel.productArrayList[0].product_id
        var pName: String = jecesModel.productArrayList[0].product_name
        var myId: String = jecesModel.thisUser.toString()

        view.productDetailName.setText(pName)
        view.productDetailPrice.setText(jecesModel.productArrayList[0].product_price + "원")
        view.productDetailDescription.setText(jecesModel.productArrayList[0].product_description)
        view.product_chat_text.setText(jecesModel.productArrayList[0].chatCount)
        view.product_view_text.setText(jecesModel.productArrayList[0].viewCount)
        view.product_check_text.setText(jecesModel.productArrayList[0].heartCount)
        view.product_info_bid_price.setText(jecesModel.productArrayList[0].product_bid_price + "원")

        imagelist = jecesModel.getImage(pName, jecesModel.productArrayList[0].product_count) as ArrayList<String>

        /* 이미지 어뎁터 */
        val adapter = ProductImageInfoRecyclerViewAdapter(myId, pName, imagelist, this@InfoFragment)

        /* 이미지 리사이클러뷰 어뎁터 장착 */
        val recyclerView =  view!!.imginfo_profile
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        /**
         * 이미지 클릭시 확대
         */
        adapter.setItemClickListener(object: ProductImageInfoRecyclerViewAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                var intent = Intent(getActivity(), ImageActivity::class.java)
                intent.putExtra("image", position)
                val opt = ActivityOptions.makeSceneTransitionAnimation(getActivity(), view, "imgTrans")
                startActivity(intent, opt.toBundle())
            }
        })

        /**
         * ViewCount ++함
         * 단 자기 자신은 올리지 않음
         */
        if(myId != pId) jecesModel.viewCountUp(pId, pName)


        /**
         * 자기 자신 버튼 숨김
         */

        if(myId == pId) {

            view.chat_start_btn.visibility = View.INVISIBLE
            view.price_add_btn.visibility = View.INVISIBLE
        }

        /**
         * check되어있는지 확인하기
         */
        jecesModel.isCheckProduct(myId)
        view.product_check.isSelected = true

        /**
         * check 버튼
         */
        view.product_check.setOnClickListener {
            /**
             * 체크 안되어있다면
             */
            it.isSelected = !it.isSelected
        }

        /**
         * 채팅버튼
         */
        view.chat_start_btn.setOnClickListener {
            /**
             * 1. 채팅하기를 누르면 일단 채팅목록에 해당 상대방과의 채팅이 있는지 검색
             * 2. 있으면 그 채팅화면을 띄워주고
             * 3. 없으면 채팅룸 생성하고 채팅창 생성 - 단 채팅리스트 보여줄때 라스트 네임 있는걸로만 보여주기
             * 4. 생성은 됨, 생성하는것에서 다음 화면을 띄워줘야하는데 그게 안됨
             * */
            jecesModel.searchChat(pId).observe(viewLifecycleOwner) { chat ->
                /**
                 * 채팅방이 있으면
                 */
                Log.d("gfggggggg", jecesModel.liveTodoChatroomDataCount.toString())
                if (chat.searchChat != null) {
                    Log.d("asdfasdf02", chat.toString())
                    /* 화면 띄움*/
                    /* 프라그먼트에서 프라그먼트로 제어가 불가능하기 때문에 상위 액티비티에서 제어 해주어야 한다. */
                    val intent = Intent(getActivity(), ChatActivity::class.java)
                    intent.apply {
                        this.putExtra("chatidx", chat.searchChat!![0].getString("chatidx").toString())
                        this.putExtra("chatYourId", chat.searchChat!![0].getString("id").toString())
                    }
                    startActivity(intent)
                }
                else {
                    Log.d("gfgggggggㅎ", jecesModel.liveTodoChatroomDataCount.toString())
                    var chatroomData = ChatroomData(
                        "${jecesModel.liveTodoChatroomDataCount}",
                        "${myId},${pId}",
                        "",
                        "${myId}/0",
                        "${pId}/0",
                        Timestamp.now()
                    )
                    jecesModel.createChatroom(chatroomData)

                    /* 화면 띄움*/
                    /* 프라그먼트에서 프라그먼트로 제어가 불가능하기 때문에 상위 액티비티에서 제어 해주어야 한다. */
                    val intent = Intent(getActivity(), ChatActivity::class.java)
                    intent.apply {
                        this.putExtra("chatidx", "2")
                        this.putExtra("chatYourId", "${myId},${pId}")
                    }
                    startActivity(intent)
                }
            }
        }

        /**
         * 입찰버튼
         * Modal 띄워서 현재입찰가격 위로 입찰 가능하도록 하기
         * 입찰성공 띄우기
         */
        view.price_add_btn.setOnClickListener {
            showDialog(pId, pName)
        }



        // Inflate the layout for this fragment
        return view
    }



    fun showDialog(pId: String, pName: String) {
        val bidDialog = BidDialog(pId, pName)
        bidDialog.show(parentFragmentManager, "BidDialog")
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