package com.example.applicationjeces.frag

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.applicationjeces.R
import com.example.applicationjeces.product.ProductRecyclerViewAdapter
import com.example.applicationjeces.product.ProductViewModel
import kotlinx.android.synthetic.main.fragment_search2.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment2.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment2 : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private  lateinit var productViewModel: ProductViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /* 레이아웃 연결 */
        val view = inflater.inflate(R.layout.fragment_search2, container, false)

        /* 리사이클러뷰 */
        val adapter = ProductRecyclerViewAdapter(emptyList())
        val recyclerView = view.rv_search2
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        /* 뷰모델 연결, 뷰모델을 불러옴 */
//        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]
//        productViewModel.getAll.observe(viewLifecycleOwner, Observer { product ->
//            /* ViewModel에 Observe를 활용하여 productViewModel에 ReadAllData 라이브 데이터가 바뀌었을때 캐치하여, adapter에서 만들어준 setData함수를 통해 바뀐데이터를 UI에 업데이트 해줌 */
//            Log.d("뷰모델어뎁터검색Frag2", product.toString())
//            adapter.setData(product)
//        })

        productViewModel.liveTodoData.observe(viewLifecycleOwner, Observer { product ->
            /* ViewModel에 Observe를 활용하여 productViewModel에 ReadAllData 라이브 데이터가 바뀌었을때 캐치하여, adapter에서 만들어준 setData함수를 통해 바뀐데이터를 UI에 업데이트 해줌 */
            Log.d("뷰모델어뎁터검색Frag2", product.toString())
            adapter.setData(product)
        })


        // Inflate the layout for this fragment
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SearchFragment2.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment2().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}