package com.example.applicationjeces.product

import android.annotation.SuppressLint
import android.provider.Settings.Global.getString
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.applicationjeces.R
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.android.synthetic.main.product_item_list.view.*
import kotlinx.android.synthetic.main.product_item_list.view.product_img
import kotlinx.android.synthetic.main.product_item_list.view.product_name
import kotlinx.android.synthetic.main.product_item_list.view.product_price
import kotlinx.android.synthetic.main.product_item_list_search.view.*
import java.text.NumberFormat
import java.util.*

class ProductViewPagerAdapter(private val context: Fragment, var myId: String, var producFiretList: List<DocumentSnapshot>): RecyclerView.Adapter<ProductViewPagerAdapter.Holder>() {


    private val cachedUrls = mutableMapOf<String, String>()

    /* ViewHolder에게 item을 보여줄 View로 쓰일 item_data_list.xml를 넘기면서 ViewHolder 생성. 아이템 레이아웃과 결합 */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.product_item_list, parent, false))
    }

    /* Holder의 bind 메소드를 호출한다. 내용 입력 */
    /* getItemCount() 리턴값이 0일 경우 호출 안함 */
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val currentItemId = producFiretList[position].get("ID")
        val currentItem = producFiretList[position].get("productName")
        val currentItem2 = producFiretList[position].get("productPrice")
        var currentItem3 = producFiretList[position].get("productImgUrl")
        val currentItem4 = producFiretList[position].get("productCount")
        val bidPrice = producFiretList[position].get("productBidPrice")

        val formattedPrice = addCommasToNumberString(currentItem2.toString())
        val formattedBidPrice = addCommasToNumberString(bidPrice.toString())

        /* HomeFragment */
        holder.itemView.product_name.text = "${currentItem.toString()}"
        holder.itemView.product_price.text = "판매가 : ${context.requireContext().getString(R.string.bid_price_format, formattedPrice)}"
        holder.itemView.current_bid_price.text = "입찰가 : ${context.requireContext().getString(R.string.bid_price_format, formattedBidPrice)}"

        val imageUrl = if (currentItem4.toString() == "0") {
            "basic_img.png"
        } else {
            "${currentItemId}/${currentItem}/${currentItem3}"

        }
        Log.d("131312", "${currentItemId}/${currentItem}/${currentItem3}")
        val storageReference = FirebaseStorage.getInstance().reference.child(imageUrl)

        // 이미지 URL 캐싱
        if (cachedUrls.containsKey(imageUrl)) {
            loadRoundedImage(holder, cachedUrls[imageUrl]!!)
        } else {
            storageReference.downloadUrl.addOnCompleteListener {
                if (it.isSuccessful && context.isAdded) {
                    val downloadUrl = it.result.toString()
                    cachedUrls[imageUrl] = downloadUrl
                    loadRoundedImage(holder, downloadUrl)
                }
            }
        }
//
//        /* 이미지가 있을 때와 없을 때 */
//        if(currentItem4.toString() == "0") {
//            currentItem3 = "basic_img.png"
//            FirebaseStorage.getInstance().reference.child("${currentItem3}").downloadUrl.addOnCompleteListener {
//                if(it.isSuccessful && context.isAdded) {
//                    Glide.with(context)
//                        .load(it.result)
//                        .override(100, 100)
//                        .fitCenter()
//                        .into(holder.itemView.product_img)
//                }
//            }
//        } else {
//            /* 상품의 아이디가 들어가야 함 */
//            FirebaseStorage.getInstance().reference.child("${currentItemId}/${currentItem}/$currentItem3").downloadUrl.addOnCompleteListener {
//                if(it.isSuccessful && context.isAdded) {
//                    Log.d("뭐냐?",currentItem3.toString())
//                    Glide.with(context)
//                        .load(it.result)
//                        .override(100, 100) //픽셀
//                        .fitCenter()
//                        .into(holder.itemView.product_img)
//                }
//            }
//        }

        holder.itemView.setOnClickListener {
            /* 리스트 클릭시 Detail 화면 전환 */
            itemClickListener.onClick(it, position)
        }

        /* 이미지 초기화 */
        holder.itemView.product_img.setImageBitmap(null)
    }

    // 라운드 처리된 이미지 로딩
    private fun loadRoundedImage(holder: Holder, url: String) {
        Glide.with(context)
            .load(url)
            .placeholder(R.drawable.ic_baseline_add_24)
            .override(100, 100)
            .apply(RequestOptions().transforms(CenterCrop(), RoundedCorners(23)))            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .skipMemoryCache(true) // 메모리 캐시 비활성화
            .into(holder.itemView.product_img)
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

    fun getTimeAgo(time: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - time

        return when {
            diff < 60 * 1000 -> "방금 전"
            diff < 60 * 1000 * 60 -> "${diff / (60 * 1000)}분 전"
            diff < 60 * 1000 * 60 * 24 -> "${diff / (60 * 1000 * 60)}시간 전"
            else -> "어제"
        }
    }

    /* 홈 전체 데이터 */
    @SuppressLint("NotifyDataSetChanged")
    fun setData(product: List<DocumentSnapshot>) {
        producFiretList = product
        Log.d("dkfflwksk", producFiretList.toString())
        /* 변경 알림 */
        notifyDataSetChanged()
    }

    /**
     * 페이징
     */
    fun addData(newItems: List<DocumentSnapshot>) {
        val startSize = producFiretList.size
        (producFiretList as MutableList).addAll(newItems)
        notifyItemRangeInserted(startSize, newItems.size)
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



    /* inner class로 viewHolder 정의. 레이아웃 내 view 연결 */
    inner class Holder(ItemView: View): RecyclerView.ViewHolder(ItemView) {

    }
}