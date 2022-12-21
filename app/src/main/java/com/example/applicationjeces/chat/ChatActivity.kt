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

    private lateinit var productModel: ProductViewModel

    var jecesfirestore: FirebaseFirestore? = null
    var chatroomidx : String? = null
    var chatroomYourId : String? = null
    var messageCheck : String? = null

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

        val adapter = ChatRecyclerViewAdapter(emptyList(), this@ChatActivity, productModel.thisUser.toString())

        /* 어뎁터 가져옴 */
        val recyclerView: RecyclerView = findViewById(R.id.messageActivity_recyclerview)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        /* 상대방 이름 가져와서 토픽 이름에 넣기 */
        var yourId = chatroomYourId?.split(",")
        if(yourId?.get(0).toString() == productModel.thisUser) {
            chat_topic_name.text = yourId?.get(1).toString()
        } else {
            chat_topic_name.text = yourId?.get(0).toString()
        }

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

        /* 채팅 가져오기 */
        productModel.getChat(chatroomidx.toString())

        /* 뷰모델 연결 후 뷰모델 옵저버를 통해 불러옴 */
        productModel.liveTodoChatData.observe(this) { chat ->

            adapter.setData(chat)

            /* 변경 알림 */
            /* 말풍선 겹치기
                *  말풍선 다르게 표시됨. 뒤에 말풍선은 그냥 꼬리표 없는 말풍선 다시 그리는거 payload됬을 때 말풍선도 변경, time 없애고, 이름없애고 */
            /* onBindview payload 전달 */
            /* https://rkdxowhd98.tistory.com/98
            * https://taehyungk.github.io/posts/android-RecyclerView-DiffUtil/ */
//            adapter.notifyItemChanged(adapter.itemCount - 2, "onRefresh")
            Log.d("페이로드activity", "ㅇ")

            /* 스크롤 제일 아래로 */
            productModel.liveTodoChatData.value?.size?.let { recyclerView.smoothScrollToPosition(it.toInt()) }
        }

        /* 뒤로가기버튼 누를시 */
        chat_back.setOnClickListener {
            val intent: Intent = Intent(this, MainActivity::class.java)
            MainActivity().getFragment(PageData.CHATROOM)
            startActivity(intent)
        }

        /* 메시지 보냄 */
        edit_send.setOnClickListener {
            /* 보낸 시간 */
            val chat = ChatData(chatroomidx.toString(), chat_text.text.toString(), productModel.thisUser.toString(), Timestamp.now())
            productModel.addChat(chat)
            chat_text.text.clear()
        }
    }
}