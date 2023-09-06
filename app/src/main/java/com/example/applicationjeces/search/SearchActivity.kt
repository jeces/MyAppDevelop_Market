package com.example.applicationjeces.search

import FilterFragment
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.applicationjeces.product.ProductViewModel
import com.example.applicationjeces.MainActivity
import com.example.applicationjeces.R
import com.example.applicationjeces.databinding.ActivitySearchBinding
import com.example.applicationjeces.product.InfoActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main2.*
import java.util.HashMap

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var productViewModel: ProductViewModel
    private var currentFilterCriteria: FilterCriteria? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ViewModel 연결
        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        productViewModel.whereMyUser("search")

        // RecyclerView 설정
        val adapter = ProductSearchRecyclerViewAdapter(emptyList(), this)
        binding.rvProfile2.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(this@SearchActivity)
        }

        // 검색 결과 관찰
        productViewModel.searchProducts("")
        productViewModel.searchResponse.observe(this, Observer { product ->
            adapter.searchSetData(product)
        })

        // SearchView 설정
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {

                Log.d("검색", "SearchView Text is changed : $newText")
                productViewModel.searchProducts(newText ?: "", currentFilterCriteria)
                return false
            }
        })

        // 뒤로가기 버튼 클릭 리스너
        binding.searchBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // 옵션 선택 리스너
        binding.filterButton.setOnClickListener {
            val filterFragment = FilterFragment()
            filterFragment.setFilterListener(object: FilterListener {
                override fun onFilterApplied(filterCriteria: FilterCriteria) {
                    currentFilterCriteria = filterCriteria
                    // 필터가 적용된 상태로 다시 검색
                    val currentQuery = binding.searchView.query.toString()
                    productViewModel.searchProducts(currentQuery, currentFilterCriteria)
                }
            })
            filterFragment.currentFilterCriteria = currentFilterCriteria // 값을 전달
            filterFragment.show(supportFragmentManager, filterFragment.tag)
        }

        setupItemClickListener(adapter)
    }

    // 리스너 설정 함수
    fun setupItemClickListener(adapter: ProductSearchRecyclerViewAdapter) {
        adapter.setItemClickListener(object : ProductSearchRecyclerViewAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                val product = adapter.producFiretList[position].data as HashMap<String, Any>
                onProductClicked(product, position)
            }
        })
    }

    fun onProductClicked(product: HashMap<String, Any>, position: Int) {
        val gson = Gson()
        val tags = listOf(product["tags"]) as List<String>

        productViewModel.setProductDetail(
            product["ID"].toString(),
            product["productName"].toString(),
            product["productPrice"].toString().toInt(),
            product["productDescription"].toString(),
            product["productCount"].toString().toInt(),
            product["pChatCount"].toString().toInt(),
            product["pViewCount"].toString().toInt(),
            product["pHeartCount"].toString().toInt(),
            product["productBidPrice"].toString(),
            product["insertTime"].toString(),
            position,
            tags,
            product["category"].toString(),
            product["state"].toString()
        )

        val tagsJson = gson.toJson(tags)
        val intent = Intent(this, InfoActivity::class.java).apply {
            putExtra("ID", product["ID"].toString())
            putExtra("IDX", product["IDX"].toString())
            putExtra("productName", product["productName"].toString())
            putExtra("productPrice", product["productPrice"].toString())
            putExtra("productDescription", product["productDescription"].toString())
            putExtra("productCount", product["productCount"].toString())
            putExtra("pChatCount", product["pChatCount"].toString())
            putExtra("pViewCount", product["pViewCount"].toString())
            putExtra("pHeartCount", product["pHeartCount"].toString())
            putExtra("productBidPrice", product["productBidPrice"].toString())
            putExtra("insertTime", product["insertTime"].toString())
            putExtra("position", position)
            putExtra("tags", tagsJson)
        }

        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // 뒤로가기
    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}