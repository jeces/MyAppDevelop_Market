package com.example.applicationjeces.product

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.applicationjeces.MainActivity
import com.example.applicationjeces.R
import com.example.applicationjeces.databinding.ActivityAddBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

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
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("SELECT_HOME", true)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
