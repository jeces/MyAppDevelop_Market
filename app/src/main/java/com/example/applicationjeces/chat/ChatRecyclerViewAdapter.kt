package com.example.applicationjeces.chat

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.applicationjeces.R
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*


/* 참고하기
* https://minoflower.tistory.com/36 */
class ChatRecyclerViewAdapter(private var myId: String, var context: Context): ListAdapter<ChatData, RecyclerView.ViewHolder>(diffUtil) {

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
        /* holder의 종류에 따라 bind */
        when(holder) {
            is leftHolder -> {
                holder.bind(currentList[position])
            }
            is rightHolder -> {
                /* position은 0이상이 되어야 비교가 됨 */
                Log.d("타임이같음포지션", position.toString())
                if(position > 0) {
                    if(changeTime(currentList[position].time) == changeTime(currentList[position - 1].time)) {
                        Log.d("타임이같음", "${changeTime(currentList[position].time)} / ${changeTime(currentList[position - 1].time)}")
                        holder.bind(currentList[position - 1], "OverLap")
                    } else {

                    }
                }
                holder.bind(currentList[position], "NoOverLap")
            }
            /* 무슨 viewHolder인지 제대로 안정해줬으니까, as로 정해주기 */
            else -> {
                (holder as rightHolder).bind(currentList[position], "OverLap")
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

    /* viewType을 return해서 이걸로 구분한다. */
    override fun getItemViewType(position: Int): Int {
        /* 각 자신과 상대방에 따라 viewType에 따라서 레이아웃을 다르게 해줌 */
        return when(currentList[position].myid) {
            myId -> 2
            else -> 1
        }
    }

    /* inner class로 viewHolder 정의. 레이아웃 내 view 연결
    * 상대방 말풍선 */
    inner class leftHolder(ItemView: View): RecyclerView.ViewHolder(ItemView) {
        private val myid: TextView = ItemView.findViewById(R.id.chat_name)
        private val messageText: TextView = ItemView.findViewById(R.id.chat_message)
        private val date: TextView = ItemView.findViewById(R.id.chat_time)
        private val img: ImageView = ItemView.findViewById(R.id.your_profile)

        fun bind(item: ChatData) {
            myid.text = item.myid
            messageText.text = item.content
            date.text = changeTime(item.time as com.google.firebase.Timestamp)
            yourProfilImg(item.myid, img)
        }
    }

    /* inner class로 viewHolder 정의. 레이아웃 내 view 연결
    * 나 자신 말풍선 */
    inner class rightHolder(ItemView: View): RecyclerView.ViewHolder(ItemView) {
        private val messageText: TextView = ItemView.findViewById(R.id.chat_message2)
        private val date: TextView = ItemView.findViewById(R.id.chat_time2)
        fun bind(item: ChatData, overLap: String) {
            if(overLap == "OverLap") {
                messageText.text = null
                date.text = null
                Log.d("타임이같음0", "dd")
                messageText.text = item.content
                date.text = " "
            } else if(overLap == "NoOverLap") {
                messageText.text = null
                date.text = null
                Log.d("타임이같음1", "dd")
                messageText.text = item.content
                date.text = changeTime(item.time as com.google.firebase.Timestamp)
            }
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

    companion object{
        val diffUtil = object : DiffUtil.ItemCallback<ChatData>(){
            override fun areItemsTheSame(oldItem: ChatData, newItem: ChatData): Boolean {
                Log.d("diffUtil123", (oldItem == newItem).toString())
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ChatData, newItem: ChatData): Boolean {
                Log.d("diffUtil1234", (oldItem == newItem).toString())
                return oldItem == newItem
            }
        }
    }
}
