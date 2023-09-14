package com.example.applicationjeces.product

import android.content.Intent
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add)
        productViewModel = ViewModelProvider(this).get(ProductViewModel::class.java)
        binding.productviewModel = productViewModel
        binding.lifecycleOwner = this

        if (savedInstanceState == null) {
            // 첫 생성 시에만 Fragment 추가
            val addFragment = AddFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, addFragment)
                .commit()
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val bottomNavigation: BottomNavigationView = binding.bottomNavigation

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.addBak -> {
                    navigateToMainActivity()
                    true
                }
                R.id.registration -> {
                    val addFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer) as? AddFragment
                    if (addFragment?.validateFields() == true) {
                        addFragment.registerProduct()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left)
    }
}
