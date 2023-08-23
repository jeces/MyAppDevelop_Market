package com.example.applicationjeces.chat

import SwipeCallback
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applicationjeces.R
import com.example.applicationjeces.databinding.FragmentChatBinding
import kotlinx.android.synthetic.main.fragment_chat.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatroomFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var chatViewModel: ChatViewModel
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!


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
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        val view = binding.root

        /**
         * 뷰모델 초기화 생성자
         **/
        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        /**
         *  나의 아이디
         **/
        val myId = chatViewModel.thisUser.toString()




        /**
         * 채팅 룸 viewpager2
         */
        val adapter = ChatroomRecyclerViewAdapter(this@ChatroomFragment, myId)
        binding.chatProfile2.adapter = adapter
        binding.chatProfile2.layoutManager = LinearLayoutManager(requireContext())
//        val viewPager = binding.chatroomViewpager2
//        val adapterCr = ChatroomRecyclerViewAdapter(this@ChatroomFragment, myId)
//        viewPager.adapter = adapterCr
        val itemTouchHelper = ItemTouchHelper(SwipeCallback())
        itemTouchHelper.attachToRecyclerView(binding.chatProfile2)

        /**
         *  채팅룸 가져오기
         **/
        chatViewModel.getAllChatroom()

        /**
         *  뷰모델 연결 후 뷰모델 옵저버를 통해 불러옴
         **/
        chatViewModel.liveTodoChatroomData.observe(viewLifecycleOwner, Observer { chatroom ->
            Log.d("왜안돼니", chatroom.toString())
            adapter.submitList(chatroom!!.toMutableList())
        })

        /**
         * 나의 위치 이동
         */
        chatViewModel.whereMyUser("chatroom")

        /**
         *  항목 클릭시
         **/
        adapter.setItemClickListener(object: ChatroomRecyclerViewAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                /* 화면 띄움*/
                /* 프라그먼트에서 프라그먼트로 제어가 불가능하기 때문에 상위 액티비티에서 제어 해주어야 한다. */
                val intent = Intent(getActivity(), ChatActivity::class.java)
                intent.apply {
                    this.putExtra("chatidx", adapter.currentList[position].chatidx)
                    this.putExtra("chatYourId", adapter.currentList[position].id)
                }

                /* 애니메이션 적용 */
                startActivity(intent)
                activity?.overridePendingTransition(R.anim.slide_out_bottom, R.anim.slide_in_top)
            }
        })
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
         * @return A new instance of fragment ChatFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatroomFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    inner class SwipeCallback : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false // 아이템 이동은 처리하지 않습니다.
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // 스와이프한 항목의 위치를 얻습니다.
            // 스와이프된 아이템을 원래 위치로 되돌립니다.
            val adapter = binding.chatProfile2.adapter
            adapter?.notifyItemChanged(viewHolder.adapterPosition)
        }

        private fun showOptionsForItem(position: Int) {
            // 항목에 대한 옵션을 표시하는 로직을 여기에 추가합니다.
            // 예: 대화상자 표시, 특정 뷰 가시성 변경 등
        }

        // 아이템 뷰가 고정되게 하려면 onChildDraw()를 오버라이드하여 아무 것도 하지 않도록 합니다.
        override fun onChildDraw(
            c: Canvas, recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float, dY: Float, actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            // 원래의 onChildDraw() 호출을 그대로 사용하여 아이템 뷰가 그려지도록 합니다.
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

            val itemView = viewHolder.itemView

            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                // 오른쪽으로 스와이프할 때
                val itemView = viewHolder.itemView
                val paint = Paint()
                paint.color = Color.WHITE
                paint.textSize = 40f
                if (dX > 0) {

                    // 박스 그리기
                    val background = RectF(itemView.left.toFloat(), itemView.top.toFloat(), dX, itemView.bottom.toFloat())
                    c.drawRect(background, paint)

                    // "알림" 텍스트 그리기
                    val textWidth = paint.measureText("알림")
                    c.drawText("알림", itemView.left.toFloat() + textWidth / 2, itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat()) / 2, paint)
                } else if (dX < 0) { // 왼쪽으로 스와이프할 때
                    // 왼쪽으로 스와이프할 때 "나가기" 텍스트와 박스를 그립니다.

                    // 박스 그리기
                    val background = RectF(itemView.right.toFloat() + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                    c.drawRect(background, paint)

                    // "나가기" 텍스트 그리기
                    val textWidth = paint.measureText("나가기")
                    c.drawText("나가기", itemView.right.toFloat() - textWidth * 1.5f, itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat()) / 2, paint)
                }
            }

            // 스와이프되는 거리(dX)가 아이템 너비의 절반을 넘어갈 경우 아이템을 원래 위치로 되돌립니다.
            if (Math.abs(dX) > viewHolder.itemView.width / 2) {
                val adapter = binding.chatProfile2.adapter
                adapter?.notifyItemChanged(viewHolder.adapterPosition)
            }
        }
    }
}

