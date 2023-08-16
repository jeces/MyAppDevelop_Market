package com.example.applicationjeces.product

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.applicationjeces.MainActivity
import com.example.applicationjeces.R
import com.example.applicationjeces.chat.ChatActivity
import com.example.applicationjeces.chat.ChatroomData
import com.example.applicationjeces.databinding.ActivityInfoBinding
import com.example.applicationjeces.page.DataViewModel
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




        /**
         * Info 네비게이션
         */
        val bottomNavigation: BottomNavigationView = binding.bottomNavigation
        /**
         * 자기 자신 버튼 숨김
         */
        if(myId == pId) {
            bottomNavigation.menu.findItem(R.id.nav_bid).isVisible = false
            bottomNavigation.menu.findItem(R.id.nav_chat).isVisible = false
        } else {
            bottomNavigation.menu.findItem(R.id.nav_edit).isVisible = false
            bottomNavigation.menu.findItem(R.id.nav_delete).isVisible = false
            bottomNavigation.menu.findItem(R.id.nav_update).isVisible = false
        }
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
                /**
                 * 입찰
                 */
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
                        processBid(pId, pName, bidAmount)  // 이는 실제로 입찰 금액을 처리하는 함수로 정의해야 합니다.

                        alertDialog.dismiss()  // 대화상자 닫기
                    }
                    true
                }
                /**
                 * 상품 삭제
                 */
                R.id.nav_delete -> {

                    val dialogView = LayoutInflater.from(this).inflate(R.layout.delete_custom_modal, null)
                    val titleText = dialogView.findViewById<TextView>(R.id.titleText)
                    val messageText = dialogView.findViewById<TextView>(R.id.messageText)
                    val positiveButton = dialogView.findViewById<Button>(R.id.OkBtn)
                    val negativeButton = dialogView.findViewById<Button>(R.id.NoBtn)

                    titleText.text = "상품 삭제"
                    messageText.text = "정말로 상품을 삭제하시겠습니까?"

                    val builder = AlertDialog.Builder(this)
                    builder.setView(dialogView)

                    val alertDialog = builder.create()

                    positiveButton.setOnClickListener {
                        productViewModel.deleteProduct(pId, pName)
                        Toast.makeText(this, "상품이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        alertDialog.dismiss()
                        /* 화면 띄움*/
                        /* 프라그먼트에서 프라그먼트로 제어가 불가능하기 때문에 상위 액티비티에서 제어 해주어야 한다. */
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }

                    negativeButton.setOnClickListener {
                        // Negative Button 클릭시 작업
                        alertDialog.dismiss()
                    }

                    alertDialog.show()
                    true

                }
                /**
                 * 상품 업데이트 수정
                 */
                R.id.nav_edit -> {
                    true
                }
                /**
                 * 상품 최신으로 끌어올리기
                 */
                R.id.nav_update -> {
                    val dialogView = LayoutInflater.from(this).inflate(R.layout.custom_update_dialog, null)
                    val titleText = dialogView.findViewById<TextView>(R.id.utitleText)
                    val messageText = dialogView.findViewById<TextView>(R.id.umessageText)
                    val positiveButton = dialogView.findViewById<Button>(R.id.uOkBtn)
                    val negativeButton = dialogView.findViewById<Button>(R.id.uNoBtn)

                    titleText.text = "상품 업데이트"
                    messageText.text = "정말로 상품을 업데이트하시겠습니까?"

                    val builder = AlertDialog.Builder(this)
                    builder.setView(dialogView)

                    val alertDialog = builder.create()

                    positiveButton.setOnClickListener {
                        productViewModel.updateProduct(pId, pName)
                        Toast.makeText(this, "상품이 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
                        alertDialog.dismiss()
                    }

                    negativeButton.setOnClickListener {
                        // Negative Button 클릭시 작업
                        alertDialog.dismiss()
                    }

                    alertDialog.show()
                    true
                }
                /**
                 * 기타
                 */
                else -> false
            }
        }
    }

    private fun processBid(pId : String, pName : String, bid_price : String) {
        productViewModel.bidchange(pId, pName, bid_price)
        Toast.makeText(this, "$bid_price 원으로 입찰하였습니다.", Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
