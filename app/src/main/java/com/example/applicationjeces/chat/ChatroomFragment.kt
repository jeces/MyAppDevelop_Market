package com.example.applicationjeces.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.applicationjeces.R
import com.example.applicationjeces.product.ProductViewModel
import kotlinx.android.synthetic.main.fragment_chat.view.*
import java.io.PipedOutputStream

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

    private lateinit var productViewModel: ProductViewModel

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

        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        /* 뷰모델 초기화 */
        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        /* 어뎁터 가져옴 */
        val adapter = ChatroomRecyclerViewAdapter(emptyList(), this@ChatroomFragment, productViewModel.thisUser.toString())
        val recyclerView = view.chat_profile
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        /* 채팅룸 가져오기 */
        productViewModel.allChatroom()

        /* 뷰모델 연결 후 뷰모델 옵저버를 통해 불러옴 */
        productViewModel.liveTodoChatroomData.observe(viewLifecycleOwner, Observer { chatroom ->
            Log.d("방만들어짐?", "dd")
            adapter.setData(chatroom)
        })

        /* 항목 클릭시 */
        adapter.setItemClickListener(object: ChatroomRecyclerViewAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {

                /* 화면 띄움*/
                /* 프라그먼트에서 프라그먼트로 제어가 불가능하기 때문에 상위 액티비티에서 제어 해주어야 한다. */
                val intent = Intent(getActivity(), ChatActivity::class.java)
                intent.apply {
                    this.putExtra("chatidx", adapter.chatRoomList[position].get("chatidx").toString())
                    this.putExtra("chatYourId", adapter.chatRoomList[position].get("id").toString())
                }
                startActivity(intent)
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