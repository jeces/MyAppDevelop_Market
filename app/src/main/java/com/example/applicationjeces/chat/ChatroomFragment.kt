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
}

