//package com.example.applicationjeces.product
//
//import android.annotation.SuppressLint
//import android.provider.Settings.Global.getString
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.GridLayoutManager
//import androidx.recyclerview.widget.ListAdapter
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.bumptech.glide.load.engine.DiskCacheStrategy
//import com.bumptech.glide.load.resource.bitmap.CenterCrop
//import com.bumptech.glide.load.resource.bitmap.RoundedCorners
//import com.bumptech.glide.request.RequestOptions
//import com.example.applicationjeces.R
//import com.google.firebase.firestore.DocumentSnapshot
//import com.google.firebase.storage.FirebaseStorage
//import jp.wasabeef.glide.transformations.RoundedCornersTransformation
//import kotlinx.android.synthetic.main.product_item_list.view.*
//import kotlinx.android.synthetic.main.product_item_list.view.product_img
//import kotlinx.android.synthetic.main.product_item_list.view.product_name
//import kotlinx.android.synthetic.main.product_item_list.view.product_price
//import kotlinx.android.synthetic.main.product_item_list_search.view.*
//import java.text.NumberFormat
//import java.util.*
//
//class ProductViewPager2Adapter(
//    private val fragment: Fragment,
//    private val repository: ProductRepository
//) : RecyclerView.Adapter<ProductViewPager2Adapter.ViewHolder>() {
//
//    private var data: List<DocumentSnapshot> = emptyList()
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val inflater = LayoutInflater.from(parent.context)
//        val view = inflater.inflate(R.layout.page_recycler_view, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun getItemCount(): Int {
//        val totalProducts = data.size
//        return (totalProducts + 8) / 9  // For 3x3 grid
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val start = position * 9
//        val end = min(start + 9, data.size)
//        val sublist = data.subList(start, end)
//
//        holder.bind(sublist, fragment, repository)
//    }
//
//    fun setData(data: List<DocumentSnapshot>) {
//        this.data = data
//        notifyDataSetChanged()
//    }
//
//    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        fun bind(data: List<DocumentSnapshot>, fragment: Fragment, repository: ProductRepository) {
//            val recyclerView: RecyclerView = itemView.findViewById(R.id.pageRecyclerView)
//            val adapter = ProductViewPagerAdapter(fragment, repository)
//            recyclerView.layoutManager = GridLayoutManager(fragment.context, 3)
//            recyclerView.adapter = adapter
//            adapter.setData(data)
//        }
//    }
//}
//
