package com.example.applicationjeces.product

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.applicationjeces.R
import com.example.applicationjeces.chat.ChatActivity
import com.example.applicationjeces.chat.ChatroomData
import com.example.applicationjeces.JecesViewModel
import com.google.firebase.Timestamp
import kotlinx.android.synthetic.main.fragment_info.*

class InfoActivity : AppCompatActivity(), ProductImageInfoRecyclerViewAdapter.OnImageClickListener {

    /* 이미지 리스트 */
    var imagelist = ArrayList<String>()
    private lateinit var jecesModel: JecesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
        Log.d("aa11", "33")

        jecesModel = ViewModelProvider(this)[JecesViewModel::class.java]
        Log.d("aa11", jecesModel.productArrayList.toString())

        /**
         * 자신의 위치 이동 저장
         */
        val pId = intent.getStringExtra("ID").toString()
        val pName = intent.getStringExtra("productName").toString()
        val productPrice = intent.getStringExtra("productPrice")
        val productDescription = intent.getStringExtra("productDescription")
        val productCount = intent.getStringExtra("productCount")
        val pChatCount = intent.getStringExtra("pChatCount")
        val pViewCount = intent.getStringExtra("pViewCount")
        val pHeartCount = intent.getStringExtra("pHeartCount")
        val productBidPrice = intent.getStringExtra("productBidPrice")
        val position = intent.getIntExtra("position", -1)
        val myId: String = jecesModel.thisUser.toString()
        jecesModel.whereMyUser("productInfo")

        Log.d("aa11", "44")
        productDetailName.text = pName
        productDetailPrice.text = productPrice + "원"
        productDetailDescription.text = productDescription
        product_chat_text.text = pChatCount
        product_view_text.text = pViewCount
        product_check_text.text = pHeartCount
        product_info_bid_price.text = productBidPrice + "원"
        Log.d("aa11", "55")
        if (productCount != null) {
            imagelist = jecesModel.getImage(pName, productCount.toInt()) as ArrayList<String>
        }

        /* 이미지 어뎁터 */
        val adapter = ProductImageInfoRecyclerViewAdapter(myId, pName, imagelist, this, this)

        /* 이미지 리사이클러뷰 어뎁터 장착 */
        imginfo_profile.adapter = adapter
        imginfo_profile.setHasFixedSize(true)
        imginfo_profile.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

        /**
         * ViewCount ++함
         * 단 자기 자신은 올리지 않음
         */
        if(myId != pId) jecesModel.viewCountUp(pId, pName)

        /**
         * 자기 자신 버튼 숨김
         */
        if(myId == pId) {
            chat_start_btn.visibility = View.INVISIBLE
            price_add_btn.visibility = View.INVISIBLE
        }

        /**
         * check되어있는지 확인하기
         */
        jecesModel.isCheckProduct(myId)
        product_check.isSelected = true

        /**
         * check 버튼
         */
        product_check.setOnClickListener {
            /**
             * 체크 안되어있다면
             */
            it.isSelected = !it.isSelected
        }

        /**
         * 채팅버튼
         */
        chat_start_btn.setOnClickListener {
            /**
             * 1. 채팅하기를 누르면 일단 채팅목록에 해당 상대방과의 채팅이 있는지 검색
             * 2. 있으면 그 채팅화면을 띄워주고
             * 3. 없으면 채팅룸 생성하고 채팅창 생성 - 단 채팅리스트 보여줄때 라스트 네임 있는걸로만 보여주기
             * 4. 생성은 됨, 생성하는것에서 다음 화면을 띄워줘야하는데 그게 안됨
             * */
            jecesModel.searchChat(pId).observe(this) { chat ->
                /**
                 * 채팅방이 있으면
                 */
                Log.d("gfggggggg", jecesModel.liveTodoChatroomDataCount.toString())
                if (chat.searchChat != null) {
                    Log.d("asdfasdf02", chat.toString())
                    /* 화면 띄움*/
                    val intent = Intent(this, ChatActivity::class.java)
                    intent.apply {
                        this.putExtra("chatidx", chat.searchChat!![0].getString("chatidx").toString())
                        this.putExtra("chatYourId", chat.searchChat!![0].getString("id").toString())
                    }
                    startActivity(intent)
                }
                else {
                    Log.d("gfgggggggㅎ", jecesModel.liveTodoChatroomDataCount.toString())
                    var chatroomData = ChatroomData(
                        "${jecesModel.liveTodoChatroomDataCount}",
                        "${myId},${pId}",
                        "",
                        "${myId}/0",
                        "${pId}/0",
                        Timestamp.now()
                    )
                    jecesModel.createChatroom(chatroomData)

                    /* 화면 띄움*/
                    val intent = Intent(this, ChatActivity::class.java)
                    intent.apply {
                        this.putExtra("chatidx", "2")
                        this.putExtra("chatYourId", "${myId},${pId}")
                    }
                    startActivity(intent)
                }
            }
        }

        /**
         * 입찰버튼
         * Modal 띄워서 현재입찰가격 위로 입찰 가능하도록 하기
         * 입찰성공 띄우기
         */
        price_add_btn.setOnClickListener {
            //showDialog(pId, pName)
            val builder = AlertDialog.Builder(this)
            val bidPriceEditText = EditText(this)
            bidPriceEditText.hint = "입찰 가격을 입력하세요." // 사용자에게 입력할 값을 설명하는 힌트 메시지
            builder.setTitle("Module Delete Message")
                .setMessage("입찰 가격")
                .setView(bidPriceEditText)
                .setPositiveButton("입찰") { _, _ ->
                    val bidPrice = bidPriceEditText.text.toString()
                    // TODO: 입찰 동작 처리
                    // jecesViewModel.bidchange(pId, pName, bidPrice)
                    Toast.makeText(this, "입찰 완료: $bidPrice", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("취소") { _, _ ->
                    Toast.makeText(this, "입찰 취소", Toast.LENGTH_SHORT).show()
                }
            builder.show()
        }
    }

    override fun onClick(images: ArrayList<String>, position: Int, myId: String, pName: String) {
        // Replace current activity with FullscreenImageActivity
        val intent = Intent(this, FullscreenImageFragment::class.java).apply {
            putExtra("images", images)
            putExtra("position", position)
            putExtra("myId", myId)
            putExtra("pName", pName)
        }
        startActivity(intent)
    }
}
