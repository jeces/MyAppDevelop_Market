package com.example.applicationjeces.chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.applicationjeces.MainActivity
import com.example.applicationjeces.R
import com.example.applicationjeces.page.PageData
import com.example.applicationjeces.databinding.ActivityChatBinding
import com.example.applicationjeces.product.AdverRecyclerViewAdapter
import com.google.firebase.Timestamp
import kotlinx.android.synthetic.main.activity_chat.view.*

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatViewModel: ChatViewModel

    /* 키보드 올라왔는지 확인 변수 */
    private var isOpen = false

    /* 각 변수들 */
    var chatroomidx : String? = null
    var chatroomYourId : String? = null
    var messageCheck : String? = null
    var myId: String? = null
    var yourId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /**
         * ChatViewModel 연결
         */
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat)
        binding.apply {
            chatViewModel = chatViewModel
            lifecycleOwner = this@ChatActivity
        }

        /**
         * ChatViewModel 생성자
         */
        chatViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(ChatViewModel::class.java)

        /**
         * putStringExtra 가져오기
         **/
        chatroomidx = intent.getStringExtra("chatidx")
        chatroomYourId = intent.getStringExtra("chatYourId")

        /**
         * 상대방 이름 가져와서 토픽 이름에 넣기
         */
        yourId = chatroomYourId?.let { chatViewModel.updateYourId(it) }
        binding.chatTopicName.text = yourId.toString()

        /**
         * 자신의 위치 이동 저장
         */
        chatViewModel.whereMyUser("chat")

        /**
         * 채팅 리사이클러 뷰
         */

        var adapter = ChatRecyclerViewAdapter(chatViewModel.myId.value.toString(), this)
        binding.messageActivityRecyclerview.apply {
            messageActivity_recyclerview.adapter = adapter
            addItemDecoration(SpaceD())
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@ChatActivity)
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
                /* 스크롤이 가능하면서 키보드가 닫힌 상태일 때만 */
                if (isScrollable() && !isOpen) {
                    setStackFromEnd()
                    removeOnLayoutChangeListener(onLayoutChangeListener)
                }
            }
        }

        /**
         * 채팅 가져오기
         **/
        chatViewModel.getChat(chatroomidx.toString())


        /**
         * ListAdapter 리사이클러뷰 채팅 보여주기
         **/
        chatViewModel.liveTodoChatDataList.observe(this) { chat ->
            chat?.let {
                Log.d("asdfasda11", chatViewModel.liveTodoChatDataList.value.toString())
                /* 리스트 전달 */
                adapter.submitList(chat.toMutableList())
                /* 읽었는지 체크 */
                chatViewModel.isRead(chatViewModel.yourId.value!!, chatroomidx.toString())
                /* 스크롤 제일 아래로 */
                chatViewModel.liveTodoChatDataList.value?.size?.let {
                    binding.messageActivityRecyclerview.smoothScrollToPosition(it)
                }
            }
        }

        /**
         * editText 변화 감지, 입력값있을 때 활성화
         **/
        binding.chatText.addTextChangedListener (object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            /** editText 변경 시 실행 **/
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                messageCheck = binding.chatText.text.toString()
                binding.editSend.isVisible = messageCheck!!.isNotEmpty()
                binding.editSharp.isVisible = messageCheck!!.isEmpty()
            }
            override fun afterTextChanged(p0: Editable?) {

            }
        })

        /**
         *  채팅창에 들어온 순간 상대방 채팅을 다 읽어야 됨
         **/
        chatViewModel.readChatAll(chatroomidx.toString(), chatViewModel.yourId.value.toString())


        /**
         * 뒤로가기버튼 누를시
         **/
        binding.chatBack.setOnClickListener {
            val mainActivity = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("destinationFragment", PageData.CHATROOM)
            }
            startActivity(mainActivity)
            finish()
        }

        /**
         * 메시지 보냄
         **/
        binding.editSend.setOnClickListener {
            /* 보낸 시간 */
            /* 여기서 시간비교해야함 바로 이전 데이터 가져와서 비교(id랑 시간이 같으면 처리 하지만 그사이에 상대방이 채팅을 칠 경우는 제외 해야함-바로이전데이터를 비교함) */
            /* 데이터 안에 앞뒤 시간이 같으면 false라는 데이터를 넣어버려서 리사이클러뷰에 넣어주면 될듯
             *  데이터 insert 시 앞의 시간과 비교해서 같으면 true라고 데이터셋에 적어두고 bind */
            /* 상대방이 whereUser = "chat"이면 true 아니면 false를 isRead체크할 수 있는거 만들어줘야함 */
            val chat = ChatData(
                chatroomidx.toString(),
                binding.chatText.text.toString(),
                chatViewModel.thisUser.toString(),
                Timestamp.now(),
                "false",
                "false",
                "true"
            )
            chatViewModel.lastChat(chat, chatroomidx.toString(), chatViewModel.yourId.value.toString())
            binding.chatText.text.clear()
        }
    }


    /* 키보드 유지 레이아웃 */
    private val onLayoutChangeListener =
        View.OnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            /* 키보드가 올라와 높이가 변함 */
            if(bottom < oldBottom) {
                binding.messageActivityRecyclerview.scrollBy(0, oldBottom - bottom)
            }
        }
}