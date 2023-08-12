package com.example.applicationjeces.product

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applicationjeces.R
import com.example.applicationjeces.chat.ChatActivity
import com.example.applicationjeces.chat.ChatroomData
import com.example.applicationjeces.JecesViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import kotlinx.android.synthetic.main.fragment_info.*

class InfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        val pId = intent.getStringExtra("ID").toString()
        val pName = intent.getStringExtra("productName").toString()
        val productPrice = intent.getStringExtra("productPrice")
        val productDescription = intent.getStringExtra("productDescription")
        val productCount = intent.getStringExtra("productCount")
        val pChatCount = intent.getStringExtra("pChatCount")
        val pViewCount = intent.getStringExtra("pViewCount")
        val pHeartCount = intent.getStringExtra("pHeartCount")
        val productBidPrice = intent.getStringExtra("productBidPrice")

        // 기본적으로 InfoFragment를 표시합니다.
        val fragment = InfoFragment().apply {
            arguments = Bundle().apply {
                putString("ID", pId)
                putString("productName", pName)
                putString("productPrice", productPrice)
                putString("productDescription", productDescription)
                putString("productCount", productCount)
                putString("pChatCount", pChatCount)
                putString("pViewCount", pViewCount)
                putString("pHeartCount", pHeartCount)
                putString("productBidPrice", productBidPrice)
            }
        }
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.commit()

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_info -> {
                    // InfoFragment 표시
                    val fragment = InfoFragment()
                    val transaction = supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.fragmentContainer, fragment)
                    transaction.commit()
                    true
                }
                // 기타 메뉴 항목 처리
                else -> false
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
