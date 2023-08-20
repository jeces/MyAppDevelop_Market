package com.example.applicationjeces.product

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.applicationjeces.R
import com.example.applicationjeces.user.MyFragment
import com.google.firebase.storage.FirebaseStorage

class ProductImageInfoRecyclerViewAdapter_z(
    var myId: String,
    var pName: String,
    var productImageList: ArrayList<String>,
    val context: Fragment,
    private val onImageClickListener: MyFragment
): RecyclerView.Adapter<ProductImageInfoRecyclerViewAdapter_z.Holder>() {

    interface OnImageClickListener {
        fun onClick(images: ArrayList<String>, position: Int, myId: String, pName: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.img_info_item_list, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        if(productImageList[0] != "basic_img.png") {
            FirebaseStorage.getInstance().reference.child("${myId}/${pName}/" + productImageList[position]).downloadUrl.addOnCompleteListener {
                Log.d("이미지온바인드", productImageList[position])
                if(it.isSuccessful && context.isAdded) {
                    Glide.with(context)
                        .load(it.result)
                        .override(180, 180)
                        .into(holder.image)
                }
            }
        } else {
            FirebaseStorage.getInstance().reference.child(productImageList[0]).downloadUrl.addOnCompleteListener {
                if(it.isSuccessful && context.isAdded) {
                    Glide.with(context)
                        .load(it.result)
                        .override(180, 180)
                        .into(holder.image)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        Log.d("리스트몇개", productImageList.size.toString())
        return productImageList.size
    }

    inner class Holder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val image = itemView.findViewById<ImageView>(R.id.infoimges)
    }
}
