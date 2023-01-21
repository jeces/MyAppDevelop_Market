package com.example.applicationjeces.product

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.applicationjeces.R
import kotlinx.android.synthetic.main.activity_image.*

class ImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

//        val img = when(intent.getIntExtra("image", 1)) {
//            1 -> R.drawable.img01
//            2 -> R.drawable.img02
//            else -> R.drawable.img03
//        }
//        image_full.setImageResource(img)

        image_full.setOnClickListener {
            supportFinishAfterTransition()
        }
    }
}