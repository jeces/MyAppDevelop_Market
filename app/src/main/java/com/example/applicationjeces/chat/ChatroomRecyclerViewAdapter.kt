package com.example.applicationjeces.chat

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.applicationjeces.R
import com.example.applicationjeces.databinding.ChatroomItemListBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.UserInfo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.chatroom_item_list.view.*
import java.text.SimpleDateFormat
import java.util.*

class ChatroomRecyclerViewAdapter(var contexts: Fragment, var myId: String): ListAdapter<ChatroomData, RecyclerView.ViewHolder>(diffUtil) {

    private val db = FirebaseStorage.getInstance()
    private val firestoreDb = FirebaseFirestore.getInstance()

    /* ViewHolder에게 item을 보여줄 View로 쓰일 item_data_list.xml를 넘기면서 ViewHolder 생성. 아이템 레이아웃과 결합 */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d("챗룸ㅇㅇ1", "dd")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chatroom_item_list, parent, false)
        return roomHolder(view)
    }

    /* Holder의 bind 메소드를 호출한다. 내용 입력 */
    /* getItemCount() 리턴값이 0일 경우 호출 안함 */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d("챗룸ㅇㅇ2", "dd")
        when(holder) {
            is roomHolder -> {
                holder.bind(currentList[position])

                holder.itemView.setOnClickListener {
                    /* 리스트 클릭시 Detail 화면 전환 */
                    itemClickListener.onClick(it, position)
                }
                /* 이미지 초기화 */
                holder.itemView.chat_item_imageview.setImageBitmap(null)
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

    override fun getItemViewType(position: Int): Int {
        Log.d("챗룸ㅇㅇ3", "ㅇㅇ")
        return 1
    }

    /* inner class로 viewHolder 정의. 레이아웃 내 view 연결 */
    inner class roomHolder(ItemView: View): RecyclerView.ViewHolder(ItemView) {

        private val binding = ChatroomItemListBinding.bind(ItemView)

        val chatroomYourId = binding.chatYourid
        val lastcomment = binding.chatLastchat
        val time = binding.chatroomTime
        val chatroomUserImg = binding.chatItemImageview
        val chatroomCount = binding.chatroomRead

        fun bind(item: ChatroomData) {
            val ids = parseIds(item.id)
            val readN0 = item.n0.split("/")
            val readN1 = item.n1.split("/")

            assignChatroomCount(ids, readN0, readN1)
            assignUserNameAndProfileImage(ids)

            lastcomment.text = item.lastcomment.toString()
            time.text = changeTime(item.time as com.google.firebase.Timestamp)
        }

        private fun parseIds(idString: String): List<String> {
            return idString.split(",").also {
                Log.d("챗룸ㅇㅇ4", "${it[0]}/${it[1]}")
            }
        }

        private fun assignChatroomCount(ids: List<String>, readN0: List<String>, readN1: List<String>) {
            if(myId == ids[0]) {
                chatroomCount.text = getChatroomCountForId(ids[0], readN0, readN1)
            } else {
                chatroomCount.text = getChatroomCountForId(ids[1], readN0, readN1)
            }
        }

        private fun getChatroomCountForId(id: String, readN0: List<String>, readN1: List<String>): String {
            if(readN0[0] == id) {
                return if(readN0[1] == "0") " " else readN0[1]
            } else if(readN1[0] == id) {
                return if(readN1[1] == "0") " " else readN1[1]
            }
            return " "
        }

        private fun assignUserNameAndProfileImage(ids: List<String>) {
            if(myId == ids[0]) {
                setUserName(ids[1], chatroomYourId)
                yourChatroomProfilImg(ids[1], chatroomUserImg)
            } else {
                setUserName(ids[0], chatroomYourId)
                yourChatroomProfilImg(ids[0], chatroomUserImg)
            }
        }
    }

    /**
     * 상대이름[Firestore 쿼리 캐싱]]
     */
    private val userNameCache = mutableMapOf<String, String>()
    fun setUserName(yourId: String, textView: TextView) {
        userNameCache[yourId]?.let { cachedName ->
            textView.text = cachedName
            return
        }
        firestoreDb.collection("UserInfo")
            .whereEqualTo("id", yourId)
            .limit(1)  // 한 개의 결과만을 원합니다.
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val name = querySnapshot.documents[0].getString("name") ?: "Unknown"
                    userNameCache[yourId] = name  // 캐시에 이름 저장
                    textView.text = name
                } else {
                    textView.text = "Unknown"  // 또는 기본값을 설정
                }
            }
            .addOnFailureListener { exception ->
                Log.d("FirestoreError", "Error getting documents: ", exception)
                textView.text = "Error"  // 적절한 에러 처리를 수행
            }
    }

    /**
     * 시간변환
     **/
    fun changeTime(timestamp: Timestamp): String {
        val mils = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
        val currentDate = Calendar.getInstance()
        val targetDate = Calendar.getInstance().apply { timeInMillis = mils }

        val diff = currentDate[Calendar.DAY_OF_YEAR] - targetDate[Calendar.DAY_OF_YEAR]

        return when {
            diff == 0 -> {
                val sf = SimpleDateFormat("aa hh:mm", Locale.KOREA)
                sf.format(Date(mils))
            }
            diff in 1..6 -> {
                "${diff}일 전"
            }
            else -> {
                val sf = SimpleDateFormat("MM월 dd일", Locale.KOREA)
                sf.format(Date(mils))
            }
        }
    }

    /**
     * 상대방 프로필 이미지
     **/
    fun yourChatroomProfilImg(yourId: String, chatroomUserImg: ImageView) {
        db.reference.child("${yourId}/profil/${yourId}_Profil_IMAGE_.png").downloadUrl.addOnCompleteListener {
            if(it.isSuccessful) {
                Glide.with(contexts)
                    .load(it.result)
                    .override(70, 70)
                    .fitCenter()
                    .circleCrop() // 또는 .transform(RoundedCorners(radius)) 를 사용하여 모서리의 반경을 설정
                    .into(chatroomUserImg)
            } else {
                /* 없으면 기본 이미지 들고와라 */
                db.reference.child("basic_user.png").downloadUrl.addOnCompleteListener { its->
                    Glide.with(contexts)
                        .load(its.result)
                        .override(70, 70)
                        .fitCenter()
                        .circleCrop() // 또는 .transform(RoundedCorners(radius)) 를 사용하여 모서리의 반경을 설정
                        .into(chatroomUserImg)
                }
            }
        }
    }

    /**
     * 상대방 판매물품 이미지
     * 1. 상대방 판매 이미지를 가져와야 함(해당 물품 채팅을 클릭 했을 때의). 클릭했을 때... 이건 고민좀 해보자
     **/
    fun yourChatroomProductlImg(yourId: String, chatroomUserImg: ImageView) {
        db.reference.child("${yourId}/${yourId}_profil.png").downloadUrl.addOnCompleteListener {
            if(it.isSuccessful) {
                Glide.with(contexts)
                    .load(it.result)
                    .override(70, 70)
                    .fitCenter()
                    .circleCrop() // 또는 .transform(RoundedCorners(radius)) 를 사용하여 모서리의 반경을 설정
                    .into(chatroomUserImg)
            } else {
                /* 없으면 기본 이미지 들고와라 */
                db.reference.child("basic_user.png").downloadUrl.addOnCompleteListener { its->
                    Glide.with(contexts)
                        .load(its.result)
                        .override(70, 70)
                        .fitCenter()
                        .circleCrop() // 또는 .transform(RoundedCorners(radius)) 를 사용하여 모서리의 반경을 설정
                        .into(chatroomUserImg)
                }
            }
        }
    }

    companion object{
        val diffUtil = object : DiffUtil.ItemCallback<ChatroomData>(){
            // User의 id를 비교해서 같으면 areContentsTheSame으로 이동(id 대신 data 클래스에 식별할 수 있는 변수 사용)
            override fun areItemsTheSame(oldItem: ChatroomData, newItem: ChatroomData): Boolean {
                Log.d("챗룸ㅇㅇ 올드", "${oldItem}/${newItem}")
                Log.d("챗룸ㅇㅇ 올드", "${oldItem == newItem}/${oldItem == newItem}")
                return oldItem.id == newItem.id
            }
            override fun areContentsTheSame(oldItem: ChatroomData, newItem: ChatroomData): Boolean {
                // User의 내용을 비교해서 같으면 true -> UI 변경 없음
                // User의 내용을 비교해서 다르면 false -> UI 변경
                Log.d("챗룸ㅇㅇ 올드ㄴ", "${oldItem}/${newItem}")
                Log.d("챗룸ㅇㅇ 올드ㄴ", "${oldItem == newItem}/${oldItem == newItem}")
                return oldItem == newItem
            }
        }
    }
}