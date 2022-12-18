package com.example.applicationjeces.chat

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.applicationjeces.R
import com.example.applicationjeces.product.Product
import com.example.applicationjeces.product.ProductViewModel
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.synthetic.main.chat_item_list.view.*
import kotlinx.android.synthetic.main.chatroom_item_list.view.*
import kotlinx.android.synthetic.main.product_item_list.view.*
import java.security.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDate.parse
import java.util.*
import java.util.logging.Level.parse

class ChatRecyclerViewAdapter(var chatList: List<DocumentSnapshot>, var context: Context, var thisUser: String): RecyclerView.Adapter<ChatRecyclerViewAdapter.Holder>() {

    /* ViewHolder에게 item을 보여줄 View로 쓰일 item_data_list.xml를 넘기면서 ViewHolder 생성. 아이템 레이아웃과 결합 */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.chat_item_list, parent, false))
    }

    /* Holder의 bind 메소드를 호출한다. 내용 입력 */
    /* getItemCount() 리턴값이 0일 경우 호출 안함 */
    override fun onBindViewHolder(holder: Holder, position: Int) {

        val chatroomidx = chatList[position].get("chatroomidx")
        val content = chatList[position].get("content")
        val myid = chatList[position].get("myid")
        val timestamp = chatList[position].get("time") as com.google.firebase.Timestamp

        /* 시간변환 */
        val mils = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
        val sf = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA)
        val nDate = Date(mils)
        val date = sf.format(nDate).toString()

        /* Timestamp를 한글로 바꿈 */
//        val sf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.KOREA)
//        sf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
////        val time = sf.format(timestamp.timestamp)

        Log.d("쳇리스트", date)

        if(thisUser == myid) {
            holder.itemView.messageItem_imageview_profile.visibility = View.INVISIBLE
            holder.itemView.messageItem_textview_name.visibility = View.INVISIBLE
            holder.itemView.messageItem_linearlayout_main.gravity = Gravity.RIGHT
            holder.itemView.messageItem_textView_time.gravity = Gravity.RIGHT
            holder.itemView.messageItem_textView_message.gravity = Gravity.RIGHT
        } else {
            holder.itemView.messageItem_linearlayout_main.gravity = Gravity.LEFT
        }


        holder.itemView.messageItem_textview_name.text = myid.toString()
        holder.itemView.messageItem_textView_message.text = content.toString()
        holder.itemView.messageItem_textView_time.text = date

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

    /* 홈 전체 데이터 */
    @SuppressLint("NotifyDataSetChanged")
    fun setData(chat: List<DocumentSnapshot>) {
        chatList.isEmpty()
        chatList = chat
        /* 변경 알림 */
        notifyDataSetChanged()
    }

    /* inner class로 viewHolder 정의. 레이아웃 내 view 연결 */
    inner class Holder(ItemView: View): RecyclerView.ViewHolder(ItemView) {

    }
}
