package com.example.applicationjeces.chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applicationjeces.MainActivity
import com.example.applicationjeces.R
import com.example.applicationjeces.page.PageData
import com.example.applicationjeces.JecesViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_chat.*

class ChatActivity : AppCompatActivity() {

    private lateinit var productModel: JecesViewModel

    // 키보드 올라왔는지 확인
    private var isOpen = false

    private var isEmojiPanelVisible = false

    var jecesfirestore: FirebaseFirestore? = null
    var chatroomidx : String? = null
    var chatroomYourId : String? = null
    var messageCheck : String? = null
    var myId: String? = null
    var yourId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        /* putStringExtra */
        chatroomidx = intent.getStringExtra("chatidx")
        chatroomYourId = intent.getStringExtra("chatYourId")

        /* firestore 가져옴 */
        jecesfirestore = FirebaseFirestore.getInstance()

        /* 뷰모델 초기화 */
        productModel = ViewModelProvider(this)[JecesViewModel::class.java]

        /**
         * 자신의 위치 이동 저장
         */
        productModel.whereMyUser("chat")

        /* 상대방 이름 가져와서 토픽 이름에 넣기 */
        var Id = chatroomYourId?.split(",")
        if(Id?.get(0).toString() == productModel.thisUser) {
            yourId = Id?.get(1).toString()
            chat_topic_name.text = yourId
            myId = Id?.get(0)
        } else {
            yourId = Id?.get(0).toString()
            chat_topic_name.text = yourId
            myId = Id?.get(1)
        }

        val adapter = ChatRecyclerViewAdapter(myId.toString(), this@ChatActivity)
        val recyclerView: RecyclerView = findViewById(R.id.messageActivity_recyclerview)
        recyclerView.apply {
            recyclerView.adapter = adapter
            addItemDecoration(SpaceD())
            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = LinearLayoutManager(this@ChatActivity)

            /**
             * 1. 키보드가 올라온 경우에만 스크롤이 가능한 경우 처리
             * 키보드가 내려간 경우 스크롤이 불가능하지만 키보드가 올라오면서 스크롤이 가능한 경우
             **/
            addOnLayoutChangeListener(onLayoutChangeListener)

            /**
             * 2. 키보드가 올라온 상태에서 데이터를 추가해 키보드가 내려갔을 때에도 스크롤이 가능한 경우
             * 3. 화면 진입 시 데이터를 불러와 청므부터 스크롤이 가능한 경우
             * 키보드가 열리지 않은 상태에서 스크롤 가능 상태이면 StackFromEnd 설정
             * 키보드가 열린 상태에서 체크하면 키보드가 사라질 때 목록이 하단에 붙을 수 있음
             * */
            viewTreeObserver.addOnScrollChangedListener {
                if (isScrollable() && !isOpen) { // 스크롤이 가능하면서 키보드가 닫힌 상태일 떄만
                    setStackFromEnd()
                    removeOnLayoutChangeListener(onLayoutChangeListener)
                }
            }

        }

        /* 채팅 가져오기 */
        productModel.getChat(chatroomidx.toString())

        /* editText 변화 감지, 입력값있을 때 활성화 */
        chat_text.addTextChangedListener (object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            /** editText 변경 시 실행 **/
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
                /* 읽었는지 체크 */
                productModel.isRead(yourId!!, chatroomidx.toString())
                /* 리스트 전달 */
                adapter.submitList(chat.toMutableList())
            }
        }

        /* 채팅창에 들어온 순간 상대방 채팅을 다 읽어야 됨 */
        productModel.readChatAll(chatroomidx.toString(), yourId.toString())
        /* 나의 위치 변경 */
        productModel.whereMyUser("chat")

        /* 뒤로가기버튼 누를시 */
        chat_back.setOnClickListener {
//            val intent: Intent = Intent(this, MainActivity::class.java)
//            MainActivity().changeFragment(PageData.CHATROOM)
//            startActivity(intent)
//            /* 임시적으로 db 변경 "chat"이 아닌 다른걸로 만들어 줘야함 */
            val mainActivity = Intent(this, MainActivity::class.java)
            mainActivity.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            mainActivity.putExtra("destinationFragment", PageData.CHATROOM)
            startActivity(mainActivity)
            finish()
        }

        /* 메시지 보냄 */
        edit_send.setOnClickListener {
            /* 보낸 시간 */
            /* 여기서 시간비교해야함 바로 이전 데이터 가져와서 비교(id랑 시간이 같으면 처리 하지만 그사이에 상대방이 채팅을 칠 경우는 제외 해야함-바로이전데이터를 비교함) */
            /* 데이터 안에 앞뒤 시간이 같으면 false라는 데이터를 넣어버려서 리사이클러뷰에 넣어주면 될듯
             *  데이터 insert 시 앞의 시간과 비교해서 같으면 true라고 데이터셋에 적어두고 bind */
            /* 상대방이 whereUser = "chat"이면 true 아니면 false를 isRead체크할 수 있는거 만들어줘야함 */

            val chat = ChatData(chatroomidx.toString(), chat_text.text.toString(), productModel.thisUser.toString(), Timestamp.now(), "false", "false", "true")
            productModel.lastChat(chat, chatroomidx.toString(), yourId.toString()).toString()
            chat_text.text.clear()
        }

        /**
         * 이모티콘 클릭시 패널 염
         */
//        edit_emos.setOnClickListener {
//            isEmojiPanelVisible = !isEmojiPanelVisible
//            if (isEmojiPanelVisible) {
//                emoji_panel.visibility = View.VISIBLE
//                hideKeyboard()
//                chat_text.clearFocus()
//            } else {
//                emoji_panel.visibility = View.GONE
//            }
//        }
    }

    /* 키보드 유지 레이아웃 */
    private val onLayoutChangeListener =
        View.OnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            /* 키보드가 올라와 높이가 변함 */
            if(bottom < oldBottom) {
                messageActivity_recyclerview.scrollBy(0, oldBottom - bottom)
            }
        }

    /**
     * 키보드를 숨기는 메서드
     */
//    private fun hideKeyboard() {
//        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.hideSoftInputFromWindow(chat_text.windowToken, 0)
//    }

    /**
     * 이모티콘 패널에서 이모티콘을 선택할 때 호출되는 콜백
     */
    fun onEmojiSelected(emoji: String) {
        val currentPosition = chat_text.selectionEnd
        chat_text.text?.let {
            val newText = StringBuilder(it)
            newText.insert(currentPosition, emoji)
            chat_text.setText(newText)
            chat_text.setSelection(currentPosition + emoji.length)
        }
    }
}