package com.example.applicationjeces.product

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.applicationjeces.MainActivity
import com.example.applicationjeces.R
import com.example.applicationjeces.chat.ChatActivity
import com.example.applicationjeces.chat.ChatroomData
import com.example.applicationjeces.databinding.ActivityAddBinding
import com.example.applicationjeces.databinding.ActivityInfoBinding
import com.example.applicationjeces.page.DataViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp

class AddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBinding
    private lateinit var productViewModel: ProductViewModel

    private var addFragment: AddFragment? = null // AddFragment 참조를 위한 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * ChatViewModel 연결
         */
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add)
        binding.apply {
            productviewModel = productviewModel
            lifecycleOwner = this@AddActivity
        }

        /**
         * ChatViewModel 생성자
         */
        productViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(
            ProductViewModel::class.java)!!

        // 기본적으로 AddFragment를 표시합니다.
        addFragment = AddFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, addFragment!!)
        transaction.commit()

        /**
         * Add 네비게이션
         */
        val bottomNavigation: BottomNavigationView = binding.bottomNavigation

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                /**
                 * 업로드 버튼
                 */
                R.id.imageupload -> {
                    addFragment?.openImagePicker()  // 여기서 함수 호출
                    true
                }
                /**
                 * 등록
                 */
                R.id.registration -> {
                    addFragment?.registerProduct()
                    true
                }

                else -> false
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
