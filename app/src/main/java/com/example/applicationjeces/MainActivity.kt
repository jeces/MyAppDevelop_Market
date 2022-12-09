package com.example.applicationjeces

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.applicationjeces.databinding.ActivityMainBinding
import com.example.applicationjeces.frag.*
import com.example.applicationjeces.message.MessageFragment
import com.example.applicationjeces.page.DataViewModel
import com.example.applicationjeces.page.PageData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val jecesViewModel by viewModels<DataViewModel>()
    private var target: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.apply {
            viewModel = jecesViewModel
            lifecycleOwner = this@MainActivity

        }

        /* 앨범에 접근하는것을 허용하는 메세지 */
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)


        /* fab 메세지 */
        binding.fab.setOnClickListener { view ->
            changeFragment(PageData.MESSAGE)
            bottomNavigationView.menu.findItem(R.id.detail).isChecked = true
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
        }

        /* LiveData의 value의 변경을 감지하고 호출 PageNum이 변경되면 호출 */
        jecesViewModel.currentPages.observe(this) {
            /* it은 LiveData의 value 값 즉 jecesViewModel 객체의 value값이 넘어온다. 처음 선언된 currentPage 넘어옴 */
            changeFragment(it)
        }
    }

    /* 현재화면 함수 */
    fun changeFragment(pageData: PageData) {
        /* 현재 Fragment[맨 처음은 tag 등록이 안되어있기 때문에 아래에서 등록을 3개를 시켜줘야함 */
        var targetFragment = supportFragmentManager.findFragmentByTag(pageData.tag)

        /* Fragment Ktx의 commit 함수 */
        supportFragmentManager.commit {
            /* 현재 Fragment가 null이라면[무조건 들어옴] */
            if(targetFragment == null) {
                /* getFragment를 호출하여 Fragment 획득 */
                targetFragment = getFragment(pageData)
                Log.d("search", targetFragment.toString())
                if(target == true) {
                    startActivity(Intent(this@MainActivity, MainActivity2::class.java))
                    Log.d("search", targetFragment.toString())
                    target = false
                    return
                }
                /* 현재 Fragment tag에 등록
                *  맨처음 3번 등록해야지 쓸 수 있음 */
                add(R.id.frame_layout, targetFragment!!, pageData.tag)
                Log.d("ㅇㅇ1", pageData.tag)
                Log.d("ㅇㅇ2", targetFragment.toString())
            }
            /* 현재 Fragment show */
            show(targetFragment!!)
            /* 나머지 Fragment hide */
            PageData.values()
                .filterNot { it == pageData }
                .forEach {type ->
                    supportFragmentManager.findFragmentByTag(type.tag)?.let {
                        hide(it)
                    }
                }
        }
    }
    // startActivity(Intent(this@MainActivity, MainActivity2::class.java))
    /* 실행시 Fragment값 GET 함수 */
    private fun getFragment(pageData: PageData): Fragment {
        Log.d("dddddd", pageData.title)
        if(pageData.title == "home") {
            return HomeFragment.newInstance(pageData.title, pageData.tag)
        } else if(pageData.title == "add") {
            return AddFragment.newInstance(pageData.title, pageData.tag)
        } else if(pageData.title == "search") {
            target = true
            return SearchFragment.newInstance(pageData.title, pageData.tag)
        } else if(pageData.title == "setting") {
            return SettingFragment.newInstance(pageData.title, pageData.tag)
        } else if(pageData.title == "detail") {
            return InfoFragment.newInstance(pageData.title, pageData.tag)
        } else if(pageData.title == "message") {
            return MessageFragment.newInstance(pageData.title, pageData.tag)
        } else {
            return HomeFragment.newInstance(pageData.title, pageData.tag)
        }
    }

    /* 상단바 옵션 메뉴 */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        /* 메뉴바 생성 */
        menuInflater.inflate(R.menu.option_menu, menu)

        /* 메뉴바를 직접 추가하는 것*/
        menu?.add(Menu.NONE, Menu.FIRST + 1, Menu.NONE, "PROGRAMMATIC MENU1")
        menu?.add(Menu.NONE, Menu.FIRST + 2, Menu.NONE, "PROGRAMMATIC MENU2")

        var sub: Menu? = menu?.addSubMenu("PROGRAMMATIC MENU3")
        sub?.add(Menu.NONE, Menu.FIRST + 3, Menu.NONE, "PROGRAMMATIC MENU3-1")
        sub?.add(Menu.NONE, Menu.FIRST + 4, Menu.NONE, "PROGRAMMATIC MENU3-2")
        return super.onCreateOptionsMenu(menu)
    }
    /* 상단바 옵션 메뉴 클릭*/
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.logout -> {
                /* 로그아웃 */
                FirebaseAuth.getInstance().signOut()
                /* 페이지 이동 */
                val it = Intent(this, MainActivityLogin::class.java)
                startActivity(it)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        Log.d("jecesMainActivity1", "onStart" )
    }

    override fun onResume() {
        super.onResume()
        Log.d("jecesMainActivity1", "onResume" )
    }

    override fun onPause() {
        super.onPause()
        Log.d("jecesMainActivity1", "onPause" )
    }

    override fun onStop() {
        super.onStop()
        Log.d("jecesMainActivity1", "onStop" )
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("jecesMainActivity1", "onDestroy" )
    }
}