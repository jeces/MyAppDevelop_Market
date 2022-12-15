package com.example.applicationjeces.frag

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.applicationjeces.R
import com.example.applicationjeces.product.Product

class Z_SearchFilterAdapter(val context: Context, val searchProductList: ArrayList<Product>): RecyclerView.Adapter<Z_SearchFilterAdapter.Holder>(),
    Filterable {

    //필터링을 위해 필요한 변수
    var products: ArrayList<Product> = searchProductList
    var itemFilter = ItemFilter()

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {

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
        return products.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.product_item_list, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val product: Product = products[position]
    }

    override fun getFilter(): Filter {
        return itemFilter
    }

    inner class ItemFilter : Filter() {
        override fun performFiltering(charSequence: CharSequence): FilterResults {
            val filterString = charSequence.toString()
            val results = FilterResults()
            Log.d("TAG", "charSequence : $charSequence")

            //검색이 필요없을 경우를 위해 원본 배열을 복제
            val filteredList: ArrayList<Product> = ArrayList<Product>()
            //공백제외 아무런 값이 없을 경우 -> 원본 배열
            if (filterString.trim { it <= ' ' }.isEmpty()) {
                results.values = searchProductList
                results.count = searchProductList.size

                return results
                //공백제외 2글자 이인 경우 -> 이름으로만 검색
            } else if (filterString.trim { it <= ' ' }.length <= 2) {
                for (product in searchProductList) {
                    if (product.product_name.contains(filterString)) {
                        filteredList.add(product)
                    }
                }
                //그 외의 경우(공백제외 2글자 초과) -> 이름/전화번호로 검색
            } else {
                for (product in searchProductList) {
                    if (product.product_name.contains(filterString) || product.product_price.contains(filterString)) {
                        filteredList.add(product)
                    }
                }
            }
            results.values = filteredList
            results.count = filteredList.size

            return results
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
            products.clear()
            products.addAll(filterResults.values as ArrayList<Product>)
            notifyDataSetChanged()
        }
    }


}