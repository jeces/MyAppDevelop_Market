package com.example.applicationjeces.product

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.applicationjeces.R
import com.example.applicationjeces.chat.ChatActivity
import com.example.applicationjeces.chat.ChatroomData
import com.example.applicationjeces.databinding.ActivityInfoBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp

class InfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInfoBinding
    private lateinit var productViewModel: ProductViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * ChatViewModel 연결
         */
        binding = DataBindingUtil.setContentView(this, R.layout.activity_info)
        binding.apply {
            productviewModel = productviewModel
            lifecycleOwner = this@InfoActivity
        }

        /**
         * ChatViewModel 생성자
         */
        productViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(
            ProductViewModel::class.java)!!

        val pId = intent.getStringExtra("ID").toString()
        val pName = intent.getStringExtra("productName").toString()
        val productPrice = intent.getStringExtra("productPrice")
        val productDescription = intent.getStringExtra("productDescription")
        val productCount = intent.getStringExtra("productCount")
        val pChatCount = intent.getStringExtra("pChatCount")
        val pViewCount = intent.getStringExtra("pViewCount")
        val pHeartCount = intent.getStringExtra("pHeartCount")
        val productBidPrice = intent.getStringExtra("productBidPrice")
        var myId = productViewModel.thisUser

        // 기본적으로 InfoFragment를 표시합니다.
        val fragment = InfoFragment().apply {
            arguments = Bundle().apply {
                putString("ID", pId)
                putString("productName", pName)
                putString("productPrice", productPrice)
                putString("productDescription", productDescription)
                putString("productCount", productCount)
                putString("pChatCount", pChatCount)
                putString("pViewCount", pViewCount)
                putString("pHeartCount", pHeartCount)
                putString("productBidPrice", productBidPrice)
            }
        }
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.commit()

        val bottomNavigation: BottomNavigationView = binding.bottomNavigation
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                /**
                 * 채팅버튼
                 */

                R.id.nav_chat -> {
                    // InfoFragment 표시
                    /**
                     * 1. 채팅하기를 누르면 일단 채팅목록에 해당 상대방과의 채팅이 있는지 검색
                     * 2. 있으면 그 채팅화면을 띄워주고
                     * 3. 없으면 채팅룸 생성하고 채팅창 생성 - 단 채팅리스트 보여줄때 라스트 네임 있는걸로만 보여주기
                     * 4. 생성은 됨, 생성하는것에서 다음 화면을 띄워줘야하는데 그게 안됨
                     * */
                    Log.d("asdas", "aaa")
                    productViewModel.searchChat(pId).observe(this) { chat ->
                        /**
                         * 채팅방이 있으면
                         */
                        Log.d("gfggggggg", productViewModel.liveTodoChatroomDataCount.toString())
                        if (chat.searchChat != null) {
                            Log.d("asdfasdf02", chat.toString())
                            /* 화면 띄움*/
                            /* 프라그먼트에서 프라그먼트로 제어가 불가능하기 때문에 상위 액티비티에서 제어 해주어야 한다. */
                            val intent = Intent(this, ChatActivity::class.java)
                            intent.apply {
                                this.putExtra("chatidx", chat.searchChat!![0].getString("chatidx").toString())
                                this.putExtra("chatYourId", chat.searchChat!![0].getString("id").toString())
                            }
                            startActivity(intent)
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        }
                        else {
                            Log.d("gfgggggggㅎ", productViewModel.liveTodoChatroomDataCount.toString())
                            var chatroomData = ChatroomData(
                                "${productViewModel.liveTodoChatroomDataCount}",
                                "${myId},${pId}",
                                "",
                                "${myId}/0",
                                "${pId}/0",
                                Timestamp.now()
                            )
                            productViewModel.createChatroom(chatroomData)

                            /* 화면 띄움*/
                            /* 프라그먼트에서 프라그먼트로 제어가 불가능하기 때문에 상위 액티비티에서 제어 해주어야 한다. */
                            val intent = Intent(this, ChatActivity::class.java)
                            intent.apply {
                                this.putExtra("chatidx", "2")
                                this.putExtra("chatYourId", "${myId},${pId}")
                            }
                            startActivity(intent)
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        }
                    }
                    true
                }
                R.id.nav_bid -> {
                    // Dialog 생성
                    Log.d("aaa1313a", "123123")
                    val dialogView = layoutInflater.inflate(R.layout.bid_dialog_layout, null)
                    val bidAmountEditText = dialogView.findViewById<EditText>(R.id.bidAmount)
                    val bidSubmitButton = dialogView.findViewById<Button>(R.id.bidSubmitButton)

                    val builder = AlertDialog.Builder(this)
                    builder.setView(dialogView)
                    builder.setTitle("입찰")
                    val alertDialog = builder.create()
                    alertDialog.show()

                    // 제출 버튼 클릭 리스너
                    bidSubmitButton.setOnClickListener {
                        val bidAmount = bidAmountEditText.text.toString().trim()
                        if (bidAmount.isEmpty()) {
                            Toast.makeText(this, "금액을 입력하세요.", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        // 금액 처리
                        processBid(bidAmount.toInt())  // 이는 실제로 입찰 금액을 처리하는 함수로 정의해야 합니다.

                        alertDialog.dismiss()  // 대화상자 닫기
                    }
                    true
                }

                // 기타 메뉴 항목 처리
                else -> false
            }
        }
    }

    private fun processBid(amount: Int) {
        // TODO: 실제 금액 처리 로직을 여기에 추가합니다. 예를 들면 서버에 입찰 요청을 보내는 등의 처리를 합니다.

        // 간단한 토스트 메시지로 처리 예제
        Toast.makeText(this, "$amount 원으로 입찰하였습니다.", Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
