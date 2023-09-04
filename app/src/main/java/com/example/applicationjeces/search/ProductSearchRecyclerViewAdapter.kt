package com.example.applicationjeces.search

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings.Global.getString
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.applicationjeces.R
import com.example.applicationjeces.product.Response
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_info.view.*
import kotlinx.android.synthetic.main.product_item_list.view.*
import kotlinx.android.synthetic.main.product_item_list.view.product_img
import kotlinx.android.synthetic.main.product_item_list.view.product_name
import kotlinx.android.synthetic.main.product_item_list.view.product_price
import kotlinx.android.synthetic.main.product_item_list_search.view.*
import java.text.NumberFormat
import java.util.*

class ProductSearchRecyclerViewAdapter(var producFiretList: List<DocumentSnapshot>, val context: Context): RecyclerView.Adapter<ProductSearchRecyclerViewAdapter.Holder>() {

    /* ViewHolder에게 item을 보여줄 View로 쓰일 item_data_list.xml를 넘기면서 ViewHolder 생성. 아이템 레이아웃과 결합 */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.product_item_list_search, parent, false))
    }

    /* Holder의 bind 메소드를 호출한다. 내용 입력 */
    /* getItemCount() 리턴값이 0일 경우 호출 안함 */
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val currentItemId = producFiretList[position].get("ID")
        val currentItem = producFiretList[position].get("productName")
        val currentItem2 = producFiretList[position].get("productPrice")
        var currentItem3 = producFiretList[position].get("productImgUrl")
        val currentItem4 = producFiretList[position].get("productCount")
        val pChatCount = producFiretList[position].get("pChatCount")
        val pHearCount = producFiretList[position].get("pHeartCount")
        val bidPrice = producFiretList[position].get("productBidPrice")


        val timestampString = producFiretList[position].get("insertTime").toString()
        val pattern = """seconds=([\d]+)""".toRegex()
        val matchResult = pattern.find(timestampString)
        val seconds = matchResult?.groupValues?.get(1)?.toLongOrNull() ?: 0L
        val millis = seconds * 1000
        val timeAgo = getTimeAgo(millis)

        /* MainActivity2 */
        val formattedPrice = addCommasToNumberString(currentItem2.toString())
        val formattedBidPrice = addCommasToNumberString(bidPrice.toString())
        holder.itemView.product_name.text = currentItem.toString()
        holder.itemView.product_price.text = context.getString(R.string.product_price_format, formattedPrice)
        holder.itemView.upload_time.text = timeAgo
        holder.itemView.like_count.text = pHearCount.toString()
        holder.itemView.chat_count.text = pChatCount.toString()
        holder.itemView.current_bid.text = context.getString(R.string.product_price_format, formattedBidPrice)

        /* 이미지가 있을 때와 없을 때 */
        if(currentItem4.toString() == "0") {
            currentItem3 = "basic_img.png"
            FirebaseStorage.getInstance().reference.child("${currentItem3}").downloadUrl.addOnCompleteListener {
                if(it.isSuccessful) {
                    Glide.with(context)
                        .load(it.result)
                        .override(100, 100)
                        .fitCenter()
                        .into(holder.itemView.product_img)
                }
            }
        } else {
            /* 상품의 아이디가 들어가야 함 */
            FirebaseStorage.getInstance().reference.child("${currentItemId}/${currentItem}/$currentItem3").downloadUrl.addOnCompleteListener {
                if(it.isSuccessful) {
                    Log.d("뭐냐?",currentItem3.toString())
                    Glide.with(context)
                        .load(it.result)
                        .override(100, 100) //픽셀
                        .fitCenter()
                        .into(holder.itemView.product_img)
                }
            }
        }

        holder.itemView.setOnClickListener {
            /* 리스트 클릭시 Detail 화면 전환 */
            itemClickListener.onClick(it, position)
        }

        /* 이미지 초기화 */
        holder.itemView.product_img.setImageBitmap(null)
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
        return producFiretList.size
    }

    fun getTimeAgo(time: Long?): String {
        if (time == null) return "알 수 없음"

        val now = System.currentTimeMillis()
        val diff = now - time

        val minute = 60 * 1000
        val hour = 60 * minute
        val day = 24 * hour
        val week = 7 * day
        val month = 30 * day
        val year = 365 * day

        return when {
            diff < minute -> "방금 전"
            diff < hour -> "${diff / minute}분 전"
            diff < day -> "${diff / hour}시간 전"
            diff < week -> "${diff / day}일 전"
            diff < 4 * week -> {
                val weeks = diff / week
                if (weeks <= 1) "1주일 전" else "${weeks}주일 전"
            }
            diff < 12 * month -> {
                val months = diff / month
                if (months <= 1) "1달 전" else "${months}달 전"
            }
            else -> {
                val years = diff / year
                if (years <= 1) "1년 전" else "${years}년 전"
            }
        }
    }

    /**
     * 단위 (,) 찍기
     */
    fun addCommasToNumberString(numberString: String): String {
        val number = numberString.replace(",", "").toLongOrNull()
        return if (number != null) {
            NumberFormat.getNumberInstance(Locale.US).format(number)
        } else {
            "" // 또는 원하는 기본값을 반환합니다.
        }
    }

    /* 홈 전체 데이터 */
    @SuppressLint("NotifyDataSetChanged")
    fun setData(product: List<DocumentSnapshot>) {
        producFiretList.isEmpty()
        producFiretList = product
        /* 변경 알림 */
        notifyDataSetChanged()
    }

    /* 검색 전체 데이터 */
    @SuppressLint("NotifyDataSetChanged")
    fun searchSetData(product: Response) {
        if(product.products?.isEmpty() == null) {
            /* 검색어가 없다면 리스트를 비워줌*/
            producFiretList = emptyList()
        } else {
            /* 있다면 리스트에 넣음 */
            producFiretList = product.products!!
        }
        /* 변경 알림 */
        notifyDataSetChanged()
    }

    /* inner class로 viewHolder 정의. 레이아웃 내 view 연결 */
    inner class Holder(ItemView: View): RecyclerView.ViewHolder(ItemView) {

    }
}