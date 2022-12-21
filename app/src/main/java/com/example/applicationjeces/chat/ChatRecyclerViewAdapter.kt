package com.example.applicationjeces.chat

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.applicationjeces.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.chat_left_item_list.view.*
import kotlinx.android.synthetic.main.chatroom_item_list.view.*
import kotlinx.android.synthetic.main.fragment_chat2.view.*
import kotlinx.android.synthetic.main.product_item_list.view.*
import java.text.SimpleDateFormat
import java.util.*

class ChatRecyclerViewAdapter(var chatList: List<DocumentSnapshot>, var context: Context, var thisUser: String): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val db = FirebaseStorage.getInstance()

    /* ViewHolder에게 item을 보여줄 View로 쓰일 item_data_list.xml를 넘기면서 ViewHolder 생성. 아이템 레이아웃과 결합 */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            viewtypeChat.LEFT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_left_item_list, parent, false)
                leftHolder(view)
            }
            viewtypeChat.RIGHT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_right_item_list, parent, false)
                rightHolder(view)
            }
            else -> throw IllegalArgumentException("viewtype 에러")
        }
    }

    /* Holder의 bind 메소드를 호출한다. 내용 입력 */
    /* getItemCount() 리턴값이 0일 경우 호출 안함 */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d("페리로드니?", "ㅇㅇ")
        /* holder의 종류에 따라 bind */
        when(holder) {
            is leftHolder -> {
                holder.bind(chatList[position])
            }
            is rightHolder -> {
                holder.bind(chatList[position])
            }
            /* 무슨 viewHolder인지 제대로 안정해줬으니까, as로 정해주기 */
            else -> {
                (holder as rightHolder).bind(chatList[position])
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if(payloads.isEmpty()) {
            Log.d("페리로드니?", payloads.toString())
            super.onBindViewHolder(holder, position, payloads)
        } else {
            Log.d("페이로드2", "asdf")
            for(payload in payloads) {
                var type: String = payload.toString()
                Log.d("페이로드before0", type)
                if(type == "onRefresh") {
                    holder.itemView.chat_time.visibility = View.INVISIBLE
                    Log.d("페이로드before", type)
                }
            }
        }
    }

    /* (2) 리스너 인터페이스 */
    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }
    /* (3) 외부에서 클릭 시 이벤트 설정 */
    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }
    /* (4) setItemClickListener로 설정한 함수 실행 */
    private lateinit var itemClickListener : OnItemClickListener
    /* 리스트 아이템 개수 */
    override fun getItemCount(): Int {
        /* productList 사이즈를 리턴합니다. */
        return chatList.size
    }

    /* viewType을 return해서 이걸로 구분한다. */
    override fun getItemViewType(position: Int): Int {
        /* 각 자신과 상대방에 따라 viewType에 따라서 레이아웃을 다르게 해줌 */
        return when(chatList[position].get("myid")) {
            thisUser -> 2
            else -> 1
        }
    }

    /* 홈 전체 데이터 */
    @SuppressLint("NotifyDataSetChanged")
    fun setData(chat: List<DocumentSnapshot>) {
        chatList = chat

        notifyDataSetChanged()
    }

    /* inner class로 viewHolder 정의. 레이아웃 내 view 연결
    * 상대방 말풍선 */
    inner class leftHolder(ItemView: View): RecyclerView.ViewHolder(ItemView) {
        private val myid: TextView = ItemView.findViewById(R.id.chat_name)
        private val messageText: TextView = ItemView.findViewById(R.id.chat_message)
        private val date: TextView = ItemView.findViewById(R.id.chat_time)
        private val img: ImageView = ItemView.findViewById(R.id.your_profile)

        fun bind(item: DocumentSnapshot) {
            myid.text = item.get("myid").toString()
            messageText.text = item.get("content").toString()
            date.text = changeTime(item.get("time") as com.google.firebase.Timestamp)
            yourProfilImg(item.get("myid").toString(), img)
        }
    }

    /* inner class로 viewHolder 정의. 레이아웃 내 view 연결
    * 나 자신 말풍선 */
    inner class rightHolder(ItemView: View): RecyclerView.ViewHolder(ItemView) {
        private val messageText: TextView = ItemView.findViewById(R.id.chat_message2)
        private val date: TextView = ItemView.findViewById(R.id.chat_time2)

        fun bind(item: DocumentSnapshot) {
            messageText.text = item.get("content").toString()
            date.text = changeTime(item.get("time") as com.google.firebase.Timestamp)
        }
    }

    /* 시간변환 */
    fun changeTime(timestamp: Timestamp): String {
        val mils = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
//        val sf = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA)
        val sf = SimpleDateFormat("aa hh:mm", Locale.KOREA)
        val nDate = Date(mils)
        val date = sf.format(nDate).toString()
        return date
    }

    fun yourProfilImg(yourId: String, chatroomUserImg: ImageView) {
        db.reference.child("${yourId}/${yourId}_profil.png").downloadUrl.addOnCompleteListener {
            if(it.isSuccessful) {
                Glide.with(context)
                    .load(it.result)
                    .override(70, 70)
                    .fitCenter()
                    .into(chatroomUserImg)
            } else {
                /* 없으면 기본 이미지 들고와라 */
                db.reference.child("basic_user.png").downloadUrl.addOnCompleteListener { its->
                    Glide.with(context)
                        .load(its.result)
                        .override(70, 70)
                        .fitCenter()
                        .into(chatroomUserImg)
                }
            }
        }
    }
}
