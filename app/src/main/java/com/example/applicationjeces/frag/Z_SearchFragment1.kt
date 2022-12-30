//package com.example.applicationjeces.frag
//
//import android.os.Bundle
//import android.util.Log
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.lifecycle.ViewModelProvider
//import com.example.applicationjeces.R
//import com.example.applicationjeces.product.ProductRecyclerViewAdapter
//import com.example.applicationjeces.ProductViewModel
//import kotlinx.android.synthetic.main.fragment_search1.view.*
//
//// TODO: Rename parameter arguments, choose names that match
//// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"
//
///**
// * A simple [Fragment] subclass.
// * Use the [SearchFragment1.newInstance] factory method to
// * create an instance of this fragment.
// */
//class SearchFragment1 : Fragment() {
//    // TODO: Rename and change types of parameters
//    private var param1: String? = null
//    private var param2: String? = null
//
//    private lateinit var productViewModel: ProductViewModel
//    val adapter = ProductRecyclerViewAdapter(emptyList())
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        var view = inflater.inflate(R.layout.fragment_search1, container, false)
//        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]
//        view.searchBtn.setOnClickListener {
//            if(view.searchText.text != null) {
//                searchProductData(view.searchText.text.toString())
//            }
//        }
//        // Inflate the layout for this fragment
//        return view
//    }
//
//    private fun searchProductData(productName: String) {
//        Log.d("뷰모델어뎁터검색Frag1", productName)
//        val searchProduct = "%$productName%"
//
////        /* 뷰모델 연결, 뷰모델을 불러옴 검색으로 불러와야 함 */
////        productViewModel.searchProduct(searchProduct).observe(viewLifecycleOwner, Observer {
////            /* ViewModel에 Observe를 활용하여 productViewModel에 ReadAllData 라이브 데이터가 바뀌었을때 캐치하여, adapter에서 만들어준 setData함수를 통해 바뀐데이터를 UI에 업데이트 해줌 */
////            Log.d("뷰모델ㅁ", searchProduct)
////            adapter.setData(it)
////            Log.d("뷰모델ㅇ2", it.toString())
////        })
//    }
//
//    companion object {
//        /**
//         * Use this factory method to create a new instance of
//         * this fragment using the provided parameters.
//         *
//         * @param param1 Parameter 1.
//         * @param param2 Parameter 2.
//         * @return A new instance of fragment SearchFragment1.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            SearchFragment1().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
//}