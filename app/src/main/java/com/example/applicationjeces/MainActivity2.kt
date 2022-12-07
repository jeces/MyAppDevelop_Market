package com.example.applicationjeces

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applicationjeces.databinding.ActivityMain2Binding
import com.example.applicationjeces.product.ProductRecyclerViewAdapter
import com.example.applicationjeces.product.ProductViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity2 : AppCompatActivity() {

    private lateinit var binding: ActivityMain2Binding
    private lateinit var productViewModel: ProductViewModel
    private lateinit var productRecyclerViewAdapter: ProductRecyclerViewAdapter
    private lateinit var searchViewProduct: SearchView
    val adapter = ProductRecyclerViewAdapter(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main2)
        /* apater initialize 초기화 해야함 */
        productRecyclerViewAdapter = ProductRecyclerViewAdapter(emptyList())

        /* 서치뷰 가져오기 */
        searchViewProduct = findViewById(R.id.search_view)

        /* 뷰모델 연결 */
        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        val recyclerView: RecyclerView = findViewById(R.id.rv_profile2)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        /* 서치뷰 생성 */
        searchViewProduct.setOnQueryTextListener(searchViewTextListener)

        /* 리사이클뷰 여기가 못불러옴  */
//        productViewModel.getAll.observe(this, ) { product ->
//            Log.d("검색observe", product.toString())
//            adapter.setData(product)
//
//        }

        productViewModel.liveTodoData.observe(this, ) { product ->
            Log.d("검색observe", product.toString())
            adapter.setData(product)
        }
    }
    private  var searchViewTextListener: SearchView.OnQueryTextListener =
        object : SearchView.OnQueryTextListener {
            /* 검색버튼 입력시 호출, 검색버튼이 없으므로 사용하지 않음 */
            override fun onQueryTextSubmit(s: String): Boolean {
                return false
            }

            /* 텍스트 입력/수정시에 호출 */
            override fun onQueryTextChange(s: String): Boolean {
                Log.d("검색", "SearchVies Text is changed : $s")
                searchDatabase(s)
                return false
            }
        }


    /* 검색 */
    private fun searchDatabase1(query: String) {
//        val searchQuery = "%$query%"
//        productViewModel.searchProduct(searchQuery).observe(this) {
//            Log.d("검색2", "SearchVies Text is changed : $query")
//            adapter.setData(it)
//            Log.d("검색2.1", productViewModel.getAll.toString())
//        }
    }

    /* firebase 검색 */
    private fun searchDatabase(searchName: String) {
        Log.d("검색2", "ㅁ")
        productViewModel.searchProducts(searchName).observe(this) {
            adapter.setData(it)
        }
    }

    /* 뒤로가기s */
    override fun onBackPressed() {
        /* Navigation Bar Selected 넘겨야 됨[여기서부터해야함] */
        val mActivity: MainActivity? = null
        if (mActivity != null) {
            mActivity.bottomNavigationView.menu.findItem(R.id.home).isChecked = true
        }
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}