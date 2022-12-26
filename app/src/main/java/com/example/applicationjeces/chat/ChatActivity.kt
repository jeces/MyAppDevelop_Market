package com.example.applicationjeces.chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applicationjeces.MainActivity
import com.example.applicationjeces.R
import com.example.applicationjeces.page.PageData
import com.example.applicationjeces.product.ProductViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_chat.*

class ChatActivity : AppCompatActivity() {

    private lateinit var productModel: ProductViewModel

    var jecesfirestore: FirebaseFirestore? = null
    var chatroomidx : String? = null
    var chatroomYourId : String? = null
    var messageCheck : String? = null
    var myId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        /* putStringExtra */
        chatroomidx = intent.getStringExtra("chatidx")
        chatroomYourId = intent.getStringExtra("chatYourId")

        /* firestore 가져옴 */
        jecesfirestore = FirebaseFirestore.getInstance()

        /* 뷰모델 초기화 */
        productModel = ViewModelProvider(this)[ProductViewModel::class.java]

        /* 상대방 이름 가져와서 토픽 이름에 넣기 */
        var yourId = chatroomYourId?.split(",")
        if(yourId?.get(0).toString() == productModel.thisUser) {
            chat_topic_name.text = yourId?.get(1).toString()
            myId = yourId?.get(0)
        } else {
            chat_topic_name.text = yourId?.get(0).toString()
            myId = yourId?.get(1)
        }

        /* 어뎁터 가져옴 */
        val adapter = ChatRecyclerViewAdapter(myId.toString(), this@ChatActivity)
        val recyclerView: RecyclerView = findViewById(R.id.messageActivity_recyclerview)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        /* 채팅 가져오기 */
        productModel.getChat(chatroomidx.toString())

        /* editText 변화 감지, 입력값있을 때 활성화 */
        chat_text.addTextChangedListener (object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            /* editText 변경 시 실행 */
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                messageCheck = chat_text.text.toString()
                edit_send.isVisible = messageCheck!!.isNotEmpty()
                edit_sharp.isVisible = messageCheck!!.isEmpty()
            }
            override fun afterTextChanged(p0: Editable?) {

            }
        })

        /* ListAdapter 리사이클러뷰 채팅 보여주기 */
        productModel.liveTodoChatDataList.observe(this) { chat ->
            chat?.let {
                /* 스크롤 제일 아래로 */
                productModel.liveTodoChatDataList.value?.size?.let { recyclerView.smoothScrollToPosition(it.toInt()) }
                /* 데이터 안에 앞뒤 시간이 같으면 false라는 데이터를 넣어버려서 리사이클러뷰에 넣어주면 될듯?
                *  데이터 insert 시 앞의 시간과 비교해서 같으면 true라고 데이터셋에 적어두고 bind할 때 하면 될듯?*/
                /* 리스트 전달 */
                adapter.submitList(chat.toMutableList())
            }
        }

        /* 뒤로가기버튼 누를시 */
        chat_back.setOnClickListener {
            val intent: Intent = Intent(this, MainActivity::class.java)
            MainActivity().changeFragment(PageData.CHATROOM)
            startActivity(intent)
        }

        /* 메시지 보냄 */
        edit_send.setOnClickListener {
            /* 보낸 시간 */
            /* 여기서 시간비교해야함 바로 이전 데이터 가져와서 비교(id랑 시간이 같으면 처리 하지만 그사이에 상대방이 채팅을 칠 경우는 제외 해야함-바로이전데이터를 비교함) */
            /* 여기서 리스트 널포인트 처리 */
            val chat = ChatData(chatroomidx.toString(), chat_text.text.toString(), productModel.thisUser.toString(), Timestamp.now(), "false")
            productModel.lastChat(chat).toString()
            chat_text.text.clear()
        }
    }
}