package com.example.applicationjeces.chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applicationjeces.MainActivity
import com.example.applicationjeces.R
import com.example.applicationjeces.databinding.ActivityChatBinding
import com.example.applicationjeces.page.PageData
import com.example.applicationjeces.product.ProductViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var productModel: ProductViewModel
    val adapter = ChatRecyclerViewAdapter(emptyList(), this@ChatActivity)
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

        /* 어뎁터 가져옴 */
        val recyclerView: RecyclerView = findViewById(R.id.messageActivity_recyclerview)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        /* 뷰모델 연결 */

        productModel.getChat(chatroomidx.toString())
        /* 뷰모델 연결 후 뷰모델 옵저버를 통해 불러옴 */
        productModel.liveTodoChatData.observe(this) { chat ->
            adapter.setData(chat)
        }

        /* 메시지 보냄 */
        messageActivity_ImageView.setOnClickListener {
            /* 보낸 시간 */
            val chat = ChatData(chatroomidx.toString(), messageActivity_editText.text.toString(), productModel.thisUser.toString(), Timestamp.now())
            productModel.addChat(chat)
        }
    }

    /* 뒤로가기 */
    override fun onBackPressed() {
        /* Navigation Bar Selected 넘겨야 됨[여기서부터해야함] */
        MainActivity().bottomNavigationView.menu.findItem(R.id.chatroom).isChecked = true
        startActivity(Intent(this, MainActivity::class.java))
        MainActivity().changeFragment(PageData.CHATROOM)
        finish()
    }
}