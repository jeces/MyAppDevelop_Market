package com.example.applicationjeces.product

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.applicationjeces.JecesViewModel
import com.example.applicationjeces.R
import com.example.applicationjeces.chat.ChatActivity
import com.example.applicationjeces.chat.ChatroomData
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator

/**
 * A simple [Fragment] subclass.
 * Use the [FullscreenImageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class FullscreenImageFragment : Fragment() {

    private lateinit var images: ArrayList<String>
    private var position: Int = 0
    private var myId: String = ""
    private var pName: String = ""

    companion object {
        private const val IMAGES_KEY = "images"
        private const val POSITION_KEY = "position"
        private const val MY_ID = "myId"
        private const val P_NAME ="pName"

        fun newInstance(images: ArrayList<String>, position: Int, myId: String, pName: String): FullscreenImageFragment {
            val fragment = FullscreenImageFragment()

            val args = Bundle().apply {
                putStringArrayList(IMAGES_KEY, images)
                putInt(POSITION_KEY, position)
                putString(MY_ID, myId)
                putString(P_NAME, pName)
            }
            fragment.arguments = args

            return fragment
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            images = it.getStringArrayList(IMAGES_KEY) ?: arrayListOf()
            position = it.getInt(POSITION_KEY)
            myId = it.getString(MY_ID) ?: ""
            pName = it.getString(P_NAME) ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("asdf1", images.get(0))
        val view = inflater.inflate(R.layout.fragment_fullscreen_image, container, false)

        /**
         * viewpager2 adapter 장착
         */
        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)
        val adapter = FullscreenImageAdapter(images, myId, pName)
        viewPager.adapter = adapter
        viewPager.currentItem = position

        /**
         * indicator 장착
         */
        val dotsIndicator: DotsIndicator = view.findViewById(R.id.dots_indicator_full)
        val viewPager2: ViewPager2 = view.findViewById(R.id.viewPager)
        dotsIndicator.setViewPager2(viewPager2)

        return view
    }

    class FullscreenImageAdapter(private val images: ArrayList<String>, private val myId: String, private val pName: String) : RecyclerView.Adapter<FullscreenImageAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(R.id.fullscreen_image)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fullscreen_image, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            FirebaseStorage.getInstance().reference.child("${myId}/${pName}/" + images[position]).downloadUrl.addOnCompleteListener {
                if(it.isSuccessful) {
                    Log.d("asdf3", images.get(0))
                    Glide.with(holder.imageView)
                        .load(it.result)
                        .into(holder.imageView)
                }
            }
        }

        override fun getItemCount() = images.size
    }
}
