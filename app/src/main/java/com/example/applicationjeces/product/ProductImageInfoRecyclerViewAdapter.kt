package com.example.applicationjeces.product

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.Model
import com.example.applicationjeces.R
import com.example.applicationjeces.frag.AddFragment
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ProductImageInfoRecyclerViewAdapter(var productImageList: ArrayList<String>, val context: Fragment): RecyclerView.Adapter<ProductImageInfoRecyclerViewAdapter.Holder>() {

    /* ViewHolder에게 item을 보여줄 View로 쓰일 item_data_list.xml를 넘기면서 ViewHolder 생성. 아이템 레이아웃과 결합 */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.img_item_list, parent, false))
    }

    /* Holder의 bind 메소드를 호출한다. 내용 입력 */
    /* getItemCount() 리턴값이 0일 경우 호출 안함 */
    override fun onBindViewHolder(holder: Holder, position: Int) {
//        val item = productImageList[position]
        FirebaseStorage.getInstance().reference.child("productimg/" + productImageList[position]).downloadUrl.addOnCompleteListener {
            Log.d("이미지온바인드", productImageList[position])
            if(it.isSuccessful) {
                Glide.with(context)
                    .load(it.result)
                    .override(180, 180)
                    .into(holder.image)
            } else {
//                Toast.makeText(context, it.exception.toString(), Toast.LENGTH_SHORT).show()
                Log.d("들어왔니?", "123")
            }
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
        Log.d("리스트몇개", productImageList.size.toString())
        return productImageList.size
    }

    /* inner class로 viewHolder 정의. 레이아웃 내 view 연결 */
    inner class Holder(ItemView: View): RecyclerView.ViewHolder(ItemView) {
        private var view: View = ItemView
        var image = ItemView.findViewById<ImageView>(R.id.addimges)
    }
}