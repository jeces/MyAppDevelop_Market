package com.example.applicationjeces.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.applicationjeces.R
import com.example.applicationjeces.product.ProductViewModel
import kotlinx.android.synthetic.main.fragment_chat2.view.*
import kotlinx.android.synthetic.main.fragment_info.view.*
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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

        val view : View = inflater.inflate(R.layout.fragment_chat2, container, false)

        val productModel: ProductViewModel by activityViewModels()

        /* 지금 한번호출하기 때문에 바뀌지 않음. observ에서 바꿔줘야함 */
        view.messageActivity_textView_topName.text = productModel.chatArrayList[0].yourid

        /* 보낸 시간 */
        val time = System.currentTimeMillis()
        val dateFormat =SimpleDateFormat("MM월dd일 hh:mm")


        view.messageActivity_ImageView.setOnClickListener {
            val currentTime = dateFormat.format(Date(time)).toString()

            val chat = ChatData(productModel.chatArrayList[0].idx,view.messageActivity_editText.text.toString(), productModel.thisUser.toString(), currentTime.toString())
            productModel.addChat(chat)

//            view.messageActivity_editText.text
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
         * @return A new instance of fragment ChatFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}