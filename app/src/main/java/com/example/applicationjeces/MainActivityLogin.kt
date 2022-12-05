package com.example.applicationjeces

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.applicationjeces.databinding.ActivityMainLoginBinding
import com.example.applicationjeces.user.JoinFragment
import com.example.applicationjeces.user.LoginFragment
import com.example.applicationjeces.user.LoginViewModel
import com.example.applicationjeces.user.PageDataLogin

class MainActivityLogin : AppCompatActivity() {

    private lateinit var binding: ActivityMainLoginBinding
    private val loginViewModel by viewModels<LoginViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_login)
        binding.apply {
            /* xml data와 바인딩 */
            loginviewModel = loginViewModel
            lifecycleOwner = this@MainActivityLogin
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