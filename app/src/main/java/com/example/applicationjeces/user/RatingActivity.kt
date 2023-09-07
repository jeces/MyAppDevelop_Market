package com.example.applicationjeces.user

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.applicationjeces.R
import com.example.applicationjeces.databinding.ActivityMainLoginBinding
import com.example.applicationjeces.databinding.FragmentMyinfoBinding
import com.example.applicationjeces.product.ProductViewModel

class RatingActivity : AppCompatActivity() {

    private lateinit var productViewModel: ProductViewModel
    private var _binding: FragmentMyinfoBinding? = null
    private val binding get() = _binding!!

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.rating_activity)
//
//        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
//        val btnSubmitRating = findViewById<Button>(R.id.btn_submit_rating)
//
//        btnSubmitRating.setOnClickListener {
//            val rating = ratingBar.rating
//            // 별점을 데이터베이스에 저장하거나 다른 작업을 수행
//            // ...
//
//            Toast.makeText(this, "별점 $rating 이/가 저장되었습니다.", Toast.LENGTH_SHORT).show()
//            finish()  // 평가 제출 후 Activity 종료
//        }
//    }
}
