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

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainLoginBinding
    private val loginViewModel by viewModels<LoginViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * 상태표시줄 투명하게 만들기
         */
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_login)
        binding.apply {
            /* xml data와 바인딩 */
            loginviewModel = loginViewModel
            lifecycleOwner = this@LoginActivity
        }

        loginViewModel.currentPages.observe(this) {
            changeFragment(it)
        }
    }

    private fun changeFragment(pageData : PageDataLogin) {
        var targetFragment = supportFragmentManager.findFragmentByTag(pageData.tag)
        supportFragmentManager.commit {
            if(targetFragment == null) {
                targetFragment = getFragment(pageData)
                add(R.id.frame_layout, targetFragment!!, pageData.tag)
            }
            show(targetFragment!!)
            PageDataLogin.values()
                .filterNot { it == pageData }
                .forEach { type ->
                    supportFragmentManager.findFragmentByTag(type.tag)?.let {
                        hide(it)
                    }
                }
        }
    }

    private fun getFragment(pageData : PageDataLogin) : Fragment {
        if(pageData.title == "login") {
            return LoginFragment.newInstance(pageData.title, pageData.tag)
        } else if(pageData.title == "join") {
            return JoinFragment.newInstance(pageData.title, pageData.tag)
        } else {
            return LoginFragment.newInstance(pageData.title, pageData.tag)
        }
    }
}