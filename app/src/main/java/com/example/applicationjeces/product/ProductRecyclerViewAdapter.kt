package com.example.applicationjeces.product

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.applicationjeces.R
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.synthetic.main.product_item_list.view.*

class ProductRecyclerViewAdapter(var producFiretList: List<DocumentSnapshot>): RecyclerView.Adapter<ProductRecyclerViewAdapter.Holder>() {

    /* ViewHolder에게 item을 보여줄 View로 쓰일 item_data_list.xml를 넘기면서 ViewHolder 생성. 아이템 레이아웃과 결합 */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.product_item_list, parent, false))
    }

    /* Holder의 bind 메소드를 호출한다. 내용 입력 */
    /* getItemCount() 리턴값이 0일 경우 호출 안함 */
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val currentItem = producFiretList[position].get("productName")
        val currentItem2 = producFiretList[position].get("productPrice")
        /* HomeFragment */
        holder.itemView.product_name.text = currentItem.toString()
        holder.itemView.product_price.text = currentItem2.toString()

        holder.itemView.setOnClickListener {
            /* 리스트 클릭시 Detail 화면 전환 */
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
        return producFiretList.size
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
        if(product!!.products?.isEmpty() == null) {
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