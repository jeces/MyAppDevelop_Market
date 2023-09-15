package com.example.applicationjeces.product

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.applicationjeces.R
import com.google.firebase.storage.FirebaseStorage

class ProductImageInfoRecyclerViewAdapter(
    var myId: String,
    var productId: String,
    var pName: String,
    var productImageList: ArrayList<String>,
    val context: Activity,
    private val onImageClickListener: OnImageClickListener
): RecyclerView.Adapter<ProductImageInfoRecyclerViewAdapter.Holder>() {

    interface OnImageClickListener {
        fun onClick(images: ArrayList<String>, position: Int, myId: String, pName: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.img_info_item_list, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        if(productImageList[0] != "basic_img.png") {
            Log.d("aaaaa", productImageList[position].toString())
            FirebaseStorage.getInstance().reference.child("${productId}/${pName}/" + productImageList[position]).downloadUrl.addOnCompleteListener {
                Log.d("이미지온바인드", productImageList[position])
                if(it.isSuccessful) {
                    Glide.with(context)
                        .load(it.result)
                        .override(180, 180)
                        .into(holder.image)
                }
            }
        } else {
            FirebaseStorage.getInstance().reference.child(productImageList[0]).downloadUrl.addOnCompleteListener {
                if(it.isSuccessful) {
                    Glide.with(context)
                        .load(it.result)
                        .override(180, 180)
                        .into(holder.image)
                }
            }
        }

        holder.itemView.setOnClickListener {
            /* 리스트 클릭시 FullscreenImageFragment로 전환 */
            onImageClickListener.onClick(productImageList, position, productId, pName)
        }
    }

    override fun getItemCount(): Int {
        Log.d("리스트몇개", productImageList.size.toString())
        return productImageList.size
    }

    fun updateData(newImageList: List<String>) {
        productImageList.clear()
        productImageList.addAll(newImageList)
        notifyDataSetChanged()
    }

    inner class Holder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val image = itemView.findViewById<ImageView>(R.id.infoimges)
    }
}
