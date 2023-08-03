package com.example.applicationjeces.product

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

class AdverRecyclerViewAdapter(
    var adverImageList: ArrayList<String>,
    val context: Fragment,
    private val onImageClickListener: OnImageClickListener
): RecyclerView.Adapter<AdverRecyclerViewAdapter.Holder>() {

    interface OnImageClickListener {
        fun onClick(images: ArrayList<String>, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_adver_image, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        FirebaseStorage.getInstance().reference.child("adverhome/" + adverImageList[position]).downloadUrl.addOnCompleteListener {
            Log.d("이미지온바인드", adverImageList[position])
            if(it.isSuccessful) {
                Glide.with(context)
                    .load(it.result)
                    .override(180, 180)
                    .into(holder.image)
            }
        }

        holder.itemView.setOnClickListener {
            /* 리스트 클릭시 FullscreenImageFragment로 전환 */
            onImageClickListener.onClick(adverImageList, position)
        }
    }

    override fun getItemCount(): Int {
        Log.d("리스트몇개", adverImageList.size.toString())
        return adverImageList.size
    }

    inner class Holder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val image = itemView.findViewById<ImageView>(R.id.adver_image)
    }
}
