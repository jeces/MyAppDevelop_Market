package com.example.applicationjeces.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applicationjeces.JecesViewModel
import com.example.applicationjeces.MainActivity
import com.example.applicationjeces.R
import com.example.applicationjeces.databinding.ActivityMain2Binding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main2.*

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMain2Binding
    private lateinit var jecesViewModel: JecesViewModel
    private lateinit var searchViewProduct: SearchView
    val adapter = ProductSearchRecyclerViewAdapter(emptyList(), this@SearchActivity)
    var jecesfirestore: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main2)

        /* firestore 가져옴 */
        jecesfirestore = FirebaseFirestore.getInstance()

        /* 서치뷰 가져오기 */
        searchViewProduct = findViewById(R.id.search_view)

        /* 뷰모델 연결 */
        jecesViewModel = ViewModelProvider(this)[JecesViewModel::class.java]

        /**
         * 자신의 위치 이동 저장
         */
        jecesViewModel.whereMyUser("search")

        val recyclerView: RecyclerView = findViewById(R.id.rv_profile2)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        /* 서치뷰 생성 */
        searchViewProduct.setOnQueryTextListener(searchViewTextListener)

        /* 검색 시 */
        jecesViewModel.searchProductsCall("").observe(this) { product ->
            Log.d("검색observe", product.toString())
            adapter.searchSetData(product)
        }

        /* 뒤로가기버튼 누를시 */
        search_back.setOnClickListener {
            val intent: Intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        /* 항목 클릭시 */
        adapter.setItemClickListener(object : ProductSearchRecyclerViewAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {

            }
        })

//        /* 항목 클릭시 */
//        adapter.setItemClickListener(object: ProductRecyclerViewAdapter.OnItemClickListener {
//            override fun onClick(v: View, position: Int) {
//                /* 화면 띄움*/
//                /* 프라그먼트에서 프라그먼트로 제어가 불가능하기 때문에 상위 액티비티에서 제어 해주어야 한다. */
//                /* ViewModel 가지고와서 PageLiveData 넘기기[업데이트 됨] */
//                val model: DataViewModel by activityViewModels()
//                model.changePageNum(PageData.DETAIL)
//
//                val productModel: ProductViewModel by activityViewModels()
//                productModel.liveTodoData.value?.get(position).toString()
//                productModel.setProductDetail(adapter.producFiretList[position].get("productName").toString(), adapter.producFiretList[position].get("productPrice").toString()
//                    , adapter.producFiretList[position].get("productDescription").toString(), adapter.producFiretList[position].get("productCount").toString(), position)
//
//                /* Navigation Bar Selected 넘겨야 됨[여기서부터해야함] */
//                val mActivity = activity as MainActivity
//                mActivity.bottomNavigationView.menu.findItem(R.id.detail).isChecked = true
//            }
//        })


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    /* 서치뷰 */
    private var searchViewTextListener: SearchView.OnQueryTextListener =
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

    /* firebase 검색 */
    private fun searchDatabase(searchName: String) {
        jecesViewModel.searchProductsCall(searchName).observe(this) {
            adapter.searchSetData(it)
        }
    }

    /* 뒤로가기 */
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