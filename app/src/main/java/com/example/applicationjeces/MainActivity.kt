package com.example.applicationjeces

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.applicationjeces.chat.ChatViewModel
import com.example.applicationjeces.chat.ChatroomFragment
import com.example.applicationjeces.databinding.ActivityMainBinding
import com.example.applicationjeces.page.DataViewModel
import com.example.applicationjeces.page.PageData
import com.example.applicationjeces.product.AddActivity
import com.example.applicationjeces.user.MyFragment
import com.example.applicationjeces.product.AddFragment
import com.example.applicationjeces.product.HomeFragment
import com.example.applicationjeces.product.ProductViewModel
import com.example.applicationjeces.search.SearchActivity
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val productViewModel by viewModels<DataViewModel>()
    private val productsViewModel by viewModels<ProductViewModel>()
    private var target: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 상태표시줄 투명하게 만들기
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT

        // 해시키
        getAppKeyHash()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.apply {
            viewModel = productViewModel
            lifecycleOwner = this@MainActivity
        }

        // 앨범에 접근하는것을 허용하는 메세지
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)

        // LiveData의 value의 변경을 감지하고 호출 PageNum이 변경되면 호출
        productViewModel.currentPages.observe(this) {
            changeFragment(it)
        }

        intent.getBooleanExtra("SELECT_HOME", false).let {
            if (it) selectHome()
        }

        /**
         * 내가 안읽은 채팅이 있다면 바꿔주기
         */
        productsViewModel.unreadChatCount.observe(this) { count ->
            val menuItem = binding.bottomNavigationView.menu.findItem(R.id.chatroom)
            if (count > 0) {
                val unreadIcon = ContextCompat.getDrawable(this, R.drawable.baseline_mark_unread_chat_alt_24)?.mutate()
                menuItem.icon = unreadIcon
            } else {
                // 필요한 경우 다른 아이콘으로 되돌리기 (색상 필터를 제거합니다.)
                val defaultIcon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_chat_bubble_24)?.mutate()
                menuItem.icon = defaultIcon
            }
        }

        // 사용자 ID와 필드 키 (예: "n0" 또는 "n1")로 데이터 가져오기
        productsViewModel.fetchUnreadMessagesCount(productsViewModel.thisUser)
    }

    // 앱 해시 키 얻는 코드
    fun getAppKeyHash() {
        try {
            val info =
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                var md: MessageDigest
                md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val something = String(Base64.encode(md.digest(), 0))
                Log.e("Hash key", something)
            }
        } catch (e: Exception) {

            Log.e("name not found", e.toString())
        }
    }


    private fun selectHome() {
        binding.bottomNavigationView.selectedItemId = R.id.home
    }

    /**
     * 페이지 전환 함수
     **/
    private fun changeFragment(pageData: PageData) {
        var targetFragment = supportFragmentManager.findFragmentByTag(pageData.tag)

        if (pageData.title == "add" || pageData.title == "search") {
            target = pageData.title
            startActivity(Intent(this@MainActivity, if(target == "search") SearchActivity::class.java else AddActivity::class.java))
            target = ""
            return
        }

        supportFragmentManager.commit {
            if (targetFragment == null) {
                targetFragment = getFragment(pageData)
                add(R.id.frame_layout, targetFragment!!, pageData.tag)
            }
            show(targetFragment!!)
            PageData.values().filterNot { it == pageData }.forEach {
                supportFragmentManager.findFragmentByTag(it.tag)?.let { hide(it) }
            }
        }
    }
    // startActivity(Intent(this@MainActivity, MainActivity2::class.java))
    /* 실행시 Fragment값 GET 함수 */
    /* 처음은 띄워주기 위해 사용됨. 후에 Hide로 숨겨져서 사용 안함. 한번쓰고 버림 */
    private fun getFragment(pageData: PageData): Fragment = when (pageData.title) {
        "home", "add", "search" -> HomeFragment.newInstance()
        "chatroom", "message" -> ChatroomFragment.newInstance(pageData.title, pageData.tag)
        "my" -> MyFragment.newInstance(pageData.title, pageData.tag)
        else -> HomeFragment.newInstance()
    }

    private fun logLifeCycleEvent(event: String) {
        Log.d("MainActivity", event)
    }

    override fun onStart() {
        super.onStart()
        logLifeCycleEvent("onStart")
    }

    override fun onResume() {
        super.onResume()
        logLifeCycleEvent("onResume")
    }

    override fun onPause() {
        super.onPause()
        logLifeCycleEvent("onPause")
    }

    override fun onStop() {
        super.onStop()
        logLifeCycleEvent("onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        logLifeCycleEvent("onDestroy")
    }
}