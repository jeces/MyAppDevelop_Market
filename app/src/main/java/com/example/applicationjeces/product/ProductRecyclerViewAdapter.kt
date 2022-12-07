package com.example.applicationjeces.product

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.applicationjeces.R
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.synthetic.main.product_item_list.view.*

class ProductRecyclerViewAdapter(var producFiretList: List<DocumentSnapshot>): RecyclerView.Adapter<ProductRecyclerViewAdapter.Holder>() {

    private var productList = emptyList<Product>()


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
        Log.d("파이어베이스", currentItem.toString())
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
        Log.d("파이어베이스1", "리턴값")
        return producFiretList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(product: List<DocumentSnapshot>) {
        producFiretList.isEmpty()
        /* 리스트가 변경되었을 떄, 업데이트 해줌 */
//        this.producFiretList.isEmpty()
        //        this.productList.isEmpty()
        ////        this.productList.
        //        Log.d("검색어댑터1",this.productList.toString())
        //        this.productList = product
        //        Log.d("검색어댑터2",this.productList.toString())
        //        notifyDataSetChanged()
        //        Log.d("검색어댑터3",this.productList.toString())
        Log.d("파이어베이스2", product.toString())
        producFiretList = product
        /* */
        notifyDataSetChanged()
    }

    /* inner class로 viewHolder 정의. 레이아웃 내 view 연결 */
    inner class Holder(ItemView: View): RecyclerView.ViewHolder(ItemView) {

    }
}