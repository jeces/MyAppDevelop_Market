package com.example.applicationjeces.chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applicationjeces.MainActivity
import com.example.applicationjeces.R
import com.example.applicationjeces.databinding.ActivityChatBinding
import com.example.applicationjeces.page.DataViewModel
import com.example.applicationjeces.page.PageData
import com.example.applicationjeces.product.ProductViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main2.*
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var productModel: ProductViewModel
    private lateinit var pageViewModel: DataViewModel

    var jecesfirestore: FirebaseFirestore? = null
    var chatroomidx : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        /* putStringExtra */
        chatroomidx = intent.getStringExtra("chatidx")

        /* firestore 가져옴 */
        jecesfirestore = FirebaseFirestore.getInstance()

        /* 뷰모델 초기화 */
        productModel = ViewModelProvider(this)[ProductViewModel::class.java]
        pageViewModel = ViewModelProvider(this)[DataViewModel::class.java]

        val adapter = ChatRecyclerViewAdapter(emptyList(), this@ChatActivity, productModel.thisUser.toString())

        /* 어뎁터 가져옴 */
        val recyclerView: RecyclerView = findViewById(R.id.messageActivity_recyclerview)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        /* 상대방 이름 표시 */

        /* 뷰모델 연결 */
        productModel.getChat(chatroomidx.toString())

        /* 뷰모델 연결 후 뷰모델 옵저버를 통해 불러옴 */
        productModel.liveTodoChatData.observe(this) { chat ->
            productModel.liveTodoChatData.value?.size?.let { recyclerView.smoothScrollToPosition(it.toInt()) }
            adapter.setData(chat)
        }

        /* 뒤로가기버튼 누를시 */
        chat_back.setOnClickListener {
            val intent: Intent = Intent(this, MainActivity::class.java)
            MainActivity().getFragment(PageData.CHATROOM)
            startActivity(intent)
        }

//        /* 메시지 보냄 */
//        messageActivity_ImageView.setOnClickListener {
//            /* 보낸 시간 */
//            val chat = ChatData(chatroomidx.toString(), messageActivity_editText.text.toString(), productModel.thisUser.toString(), Timestamp.now())
//            productModel.addChat(chat)
//            messageActivity_editText.text.clear()
//        }
    }
}