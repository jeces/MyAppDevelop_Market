package com.example.applicationjeces.chat

import android.annotation.SuppressLint
import android.content.ClipData.Item
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.applicationjeces.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.chatroom_item_list.view.*
import kotlinx.android.synthetic.main.product_item_list.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatroomRecyclerViewAdapter(var chatRoomList: List<DocumentSnapshot>, var context: Fragment, var myId: String): RecyclerView.Adapter<ChatroomRecyclerViewAdapter.Holder>() {

    private val destinationUsers : ArrayList<String> = arrayListOf()
    private val db = FirebaseStorage.getInstance()

    /* ViewHolder에게 item을 보여줄 View로 쓰일 item_data_list.xml를 넘기면서 ViewHolder 생성. 아이템 레이아웃과 결합 */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.chatroom_item_list, parent, false))
    }

    /* Holder의 bind 메소드를 호출한다. 내용 입력 */
    /* getItemCount() 리턴값이 0일 경우 호출 안함 */
    override fun onBindViewHolder(holder: Holder, position: Int) {

        holder.bind(chatRoomList[position])

        holder.itemView.setOnClickListener {
            /* 리스트 클릭시 Detail 화면 전환 */
            itemClickListener.onClick(it, position)
        }

        /* 이미지 초기화 */
        holder.itemView.chat_item_imageview.setImageBitmap(null)
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
        return chatRoomList.size
    }

    /* 홈 전체 데이터 */
    @SuppressLint("NotifyDataSetChanged")
    fun setData(chatroom: List<DocumentSnapshot>?) {
        if (chatroom != null) {
            chatRoomList = chatroom
            Log.d("챗룸리스트", chatRoomList.toString())
        }
        /* 변경 알림 */
        notifyDataSetChanged()
    }

    /* inner class로 viewHolder 정의. 레이아웃 내 view 연결 */
    inner class Holder(ItemView: View): RecyclerView.ViewHolder(ItemView) {
        private val chatroomYourId: TextView = ItemView.findViewById(R.id.chat_yourid)
        private val lastcomment: TextView = ItemView.findViewById(R.id.chat_lastchat)
        private val time: TextView = ItemView.findViewById(R.id.chatroom_time)
        private val chatroomUserImg: ImageView = ItemView.findViewById(R.id.chat_item_imageview)
        private val chatroomCount: TextView = ItemView.findViewById(R.id.chatroom_read)

        fun bind(item: DocumentSnapshot) {
            val Id = item.get("id").toString().split(",")
            val readN0 = item.getString("n0").toString().split("/")
            val readN1 = item.getString("n1").toString().split("/")
            if(myId == Id[0]) {
                if(readN0[0] == Id[0]) {
                    Log.d("asdfasdf1", readN0[1])
                    if(readN0[1] == "0") chatroomCount.text = " "
                    else chatroomCount.text = readN0[1]
                }
                else if(readN1[0] == Id[0]) {
                    Log.d("asdfasdf2", readN1[1])
                    if(readN1[1] == "0") chatroomCount.text = " "
                    else chatroomCount.text = readN1[1]
                }
                chatroomYourId.text = Id[1]
                yourChatroomProfilImg(Id[1], chatroomUserImg)
            } else {
                if(readN0[0] == Id[1]) {
                    Log.d("asdfasdf3", readN0[1])
                    if(readN0[1] == "0") chatroomCount.text = " "
                    else chatroomCount.text = readN0[1]
                }
                else if(readN1[0] == Id[1]) {
                    Log.d("asdfasdf4", readN1[1])
                    if(readN1[1] == "0") chatroomCount.text = " "
                    else chatroomCount.text = readN1[1]
                }
                chatroomYourId.text = Id[0]
                yourChatroomProfilImg(Id[0], chatroomUserImg)
            }
            lastcomment.text = item.get("lastcomment").toString()
            time.text = changeTime(item.get("time") as com.google.firebase.Timestamp)
        }
    }

    /* 시간변환 */
    fun changeTime(timestamp: Timestamp): String {
        val mils = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
        val sf = SimpleDateFormat("MM월 dd일 aa hh:mm", Locale.KOREA)
//        val sf = SimpleDateFormat("aa hh:mm", Locale.KOREA)
        val nDate = Date(mils)
        val date = sf.format(nDate).toString()
        return date
    }

    /* 상대방 프로필 이미지 */
    fun yourChatroomProfilImg(yourId: String, chatroomUserImg: ImageView) {
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
