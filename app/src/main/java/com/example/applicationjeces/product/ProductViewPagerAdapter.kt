package com.example.applicationjeces.product

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings.Global.getString
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
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

class ProductViewPagerAdapter(
    private val context: Context,
    private val productRepository: ProductRepository
) : ListAdapter<DocumentSnapshot, ProductViewPagerAdapter.Holder>(ProductDiffCallback()) {

    private val cachedUrls = mutableMapOf<String, String>()

    /* ViewHolder에게 item을 보여줄 View로 쓰일 item_data_list.xml를 넘기면서 ViewHolder 생성. 아이템 레이아웃과 결합 */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.product_item_list, parent, false))
    }

    /* Holder의 bind 메소드를 호출한다. 내용 입력 */
    /* getItemCount() 리턴값이 0일 경우 호출 안함 */
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val currentItemId = getItem(position).get("ID")
        val productName = getItem(position).get("productName")
        val productPrice = getItem(position).get("productPrice")
        var productImgUrl = getItem(position).get("productImgUrl")
        val productCount = getItem(position).get("productCount")
        val bidPrice = getItem(position).get("productBidPrice")

        val formattedPrice = formatToTenThousandWon(productPrice.toString())
        val formattedBidPrice = formatToTenThousandWon(bidPrice.toString())

        /* HomeFragment */
        holder.itemView.product_name.text = "${productName.toString()}"
        holder.itemView.product_price.text = "판매가 : ${formattedPrice}"
        holder.itemView.current_bid_price.text = "입찰가 : ${formattedBidPrice}"

        val imageUrl = if (productCount.toString() == "0") {
            "basic_img.png"
        } else {
            "${currentItemId}/${productName}/${productImgUrl}"

        }
        Log.d("131312", "${currentItemId}/${productName}/${productImgUrl}")

        // 이미지 URL 캐싱
        if (cachedUrls.containsKey(imageUrl)) {
            loadRoundedImage(holder, cachedUrls[imageUrl]!!)
        } else {
            productRepository.getImageUrl(currentItemId.toString(), productName.toString(), productImgUrl.toString(), productCount.toString())
                .addOnCompleteListener { task ->
                    // 조건부로 Fragment의 isAdded 메서드를 확인
                    val isFragmentAdded = (context is Fragment && context.isAdded)
                    if (task.isSuccessful && isFragmentAdded) {
                        val downloadUrl = task.result.toString()
                        cachedUrls[imageUrl] = downloadUrl
                        loadRoundedImage(holder, downloadUrl)
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

    // 라운드 처리된 이미지 로딩
    private fun loadRoundedImage(holder: Holder, url: String) {
        Glide.with(context)
            .load(url)
            .placeholder(R.drawable.ic_baseline_add_24)
            .override(100, 100)
            .apply(RequestOptions().transforms(CenterCrop(), RoundedCorners(16)))            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
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

    @SuppressLint("NotifyDataSetChanged")
    fun setData(product: List<DocumentSnapshot>) {
        submitList(product)
    }
    fun getDocumentSnapshotAt(position: Int): DocumentSnapshot {
        return getItem(position)
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

    /**
     * 만원 찍기
     */
    fun formatToTenThousandWon(numberString: String): String {
        val number = numberString.replace(",", "").toDoubleOrNull()
        return if (number != null) {
            val divided = number / 10000
            String.format("%.1f만원", divided)
        } else {
            "" // 또는 원하는 기본값을 반환합니다.
        }
    }

    /* inner class로 viewHolder 정의. 레이아웃 내 view 연결 */
    inner class Holder(ItemView: View): RecyclerView.ViewHolder(ItemView) {

    }
}

class ProductDiffCallback : DiffUtil.ItemCallback<DocumentSnapshot>() {
    override fun areItemsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
        // ID 필드를 기반으로 아이템이 동일한지 확인
        return oldItem["ID"] == newItem["ID"]
    }

    override fun areContentsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
        // 내용이 동일한지 확인
        return oldItem == newItem
    }
}
