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

    fun changeFragment(pageData : PageDataLogin) {
        var targetFragment = supportFragmentManager.findFragmentByTag(pageData.tag)
        supportFragmentManager.commit {
            if(targetFragment == null) {
                targetFragment = getFragment(pageData)
                add(R.id.frame_layout, targetFragment!!, pageData.tag)
            }
            // 애니메이션 추가
            setCustomAnimations(R.anim.slide_in_right, // 새로운 Fragment가 들어올 때 애니메이션
                R.anim.slide_out_left, // 이전 Fragment가 나갈 때 애니메이션
                R.anim.slide_in_left, // 백스택에서 다시 복귀하는 이전 Fragment의 애니메이션
                R.anim.slide_out_right) // 백스택에서 복귀하는 새로운 Fragment가 나갈 때 애니메이션

            show(targetFragment!!)
            PageDataLogin.values()
                .filterNot { it == pageData }
                .forEach { type ->
                    supportFragmentManager.findFragmentByTag(type.tag)?.let {
                        hide(it)
                    }
                }
        }
        // BottomNavigationView의 선택 항목 변경
        when (pageData) {
            PageDataLogin.LOGIN -> binding.loginbottomNavigationView.selectedItemId = R.id.sign_in
            PageDataLogin.JOIN -> binding.loginbottomNavigationView.selectedItemId = R.id.sign_up
            // ... (기타 다른 탭에 대한 로직 추가)
        }
    }

    private fun getFragment(pageData : PageDataLogin) : Fragment {
        if(pageData.title == "login") {
            return LoginFragment.newInstance()
        } else if(pageData.title == "join") {
            return JoinFragment.newInstance()
        } else {
            return LoginFragment.newInstance()
        }
    }
}