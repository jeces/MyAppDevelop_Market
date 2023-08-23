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

    inner class SwipeCallback : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false // 아이템 이동은 처리하지 않습니다.
        }

        private var swipedPosition = -1
        private val optionButtonWidth = 200f
        private val swipeThreshold = optionButtonWidth // 스와이프의 임계치 설정

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // 스와이프한 항목의 위치를 얻습니다.
            swipedPosition = viewHolder.adapterPosition
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
            val itemView = viewHolder.itemView
            val paint = Paint()

            // 항목이 반대로 스와이프되어 복구될 때 아이템 뷰를 원래 위치로 되돌리는 로직
            if (!isCurrentlyActive && dX == 0f) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                return
            }

            // 항목 뷰를 움직이는 범위를 제한합니다.
            val newDX = when {
                dX > swipeThreshold -> swipeThreshold
                dX < -swipeThreshold -> -swipeThreshold
                else -> dX
            }

            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                if (dX > 0) { // 오른쪽 스와이프
                    paint.color = Color.WHITE
                    val background = RectF(itemView.left.toFloat(), itemView.top.toFloat(), dX, itemView.bottom.toFloat())
                    c.drawRect(background, paint)

                    paint.color = Color.BLACK
                    paint.textSize = 40f
                    val textWidth = paint.measureText("알림")
                    c.drawText("알림", itemView.left.toFloat() + textWidth / 2, itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat()) / 2, paint)
                } else if (dX < 0) { // 왼쪽 스와이프
                    paint.color = Color.WHITE
                    val background = RectF(itemView.right.toFloat() + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                    c.drawRect(background, paint)

                    paint.color = Color.BLACK
                    paint.textSize = 40f
                    val textWidth = paint.measureText("나가기")
                    c.drawText("나가기", itemView.right.toFloat() - textWidth * 1.5f, itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat()) / 2, paint)
                }
            }

            // 항목 뷰의 위치를 업데이트하는 코드
            super.onChildDraw(c, recyclerView, viewHolder, newDX, dY, actionState, isCurrentlyActive)
        }

        // 스와이프 후 항목이 원래 위치로 되돌아오게 설정
        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)

            // 스와이프 애니메이션 종료 후 아이템 뷰를 원래 위치로 되돌리기
            viewHolder.itemView.translationX = 0f

            if (swipedPosition != -1) {
                // 여기서 해당 항목의 상태를 변경하거나 리사이클러뷰의 어댑터를 업데이트합니다.
                // 예: adapter.notifyItemChanged(swipedPosition)
                swipedPosition = -1
            }
        }
    }
}

