package com.example.applicationjeces.loading

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.applicationjeces.user.LoginActivity

class SplashActivity : AppCompatActivity() {
    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("aaaadd", "123")
        viewModel.loading.observe(this, Observer { isLoading ->
            Log.d("aaaadd", "1234")
            if (!isLoading) {
                Log.d("aaaaddd", "12345")
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        })
    }
}