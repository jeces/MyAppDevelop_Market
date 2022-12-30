package com.example.applicationjeces.frag

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.applicationjeces.MainActivity
import com.example.applicationjeces.R
import com.example.applicationjeces.page.DataViewModel
import com.example.applicationjeces.page.PageData
import com.example.applicationjeces.product.ProductRecyclerViewAdapter
import com.example.applicationjeces.JecesViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private  lateinit var jecesViewModel: JecesViewModel

    override fun onAttach(context: Context) {
        Log.d("jecesAddFragment", "onAttach")
        super.onAttach(context)
    }

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
        Log.d("홈연결", "ㅇㅇ")
        /* 레이아웃 연결 */
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        /* 리사이클러뷰 */
        val adapter = ProductRecyclerViewAdapter(emptyList(), this@HomeFragment)
        val recyclerView = view.rv_profile
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        /* 뷰모델 연결, 뷰모델을 불러옴 */
        jecesViewModel = ViewModelProvider(this)[JecesViewModel::class.java]
        jecesViewModel.liveTodoData.observe(viewLifecycleOwner, Observer { product ->
            /* ViewModel에 Observe를 활용하여 productViewModel에 ReadAllData 라이브 데이터가 바뀌었을때 캐치하여, adapter에서 만들어준 setData함수를 통해 바뀐데이터를 UI에 업데이트 해줌 */
            Log.d("dkfflwksk", "ddd")
            adapter.setData(product)
        })

        /* 항목 클릭시 */
        adapter.setItemClickListener(object: ProductRecyclerViewAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                /* 화면 띄움*/
                /* 프라그먼트에서 프라그먼트로 제어가 불가능하기 때문에 상위 액티비티에서 제어 해주어야 한다. */
                /* ViewModel 가지고와서 PageLiveData 넘기기[업데이트 됨] */
                val model: DataViewModel by activityViewModels()
                model.changePageNum(PageData.DETAIL)

                val productModel: JecesViewModel by activityViewModels()
                productModel.liveTodoData.value?.get(position).toString()
                productModel.setProductDetail(adapter.producFiretList[position].get("productName").toString(), adapter.producFiretList[position].get("productPrice").toString()
                    , adapter.producFiretList[position].get("productDescription").toString(), adapter.producFiretList[position].get("productCount").toString(), position)

                /* Navigation Bar Selected 넘겨야 됨[여기서부터해야함] */
                val mActivity = activity as MainActivity
                mActivity.bottomNavigationView.menu.findItem(R.id.detail).isChecked = true
            }
        })

        // Inflate the layout for this fragment
        return view
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        Log.d("jecesAddFragment", "onViewStateRestored")
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onStart() {
        Log.d("jecesAddFragment", "onStart")
        super.onStart()
    }

    override fun onResume() {
        Log.d("jecesAddFragment", "onResume")
        super.onResume()
    }

    override fun onPause() {
        Log.d("jecesAddFragment", "onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.d("jecesAddFragment", "onStop")
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d("jecesAddFragment", "onSaveInstanceState")
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        Log.d("jecesAddFragment", "onDestroyView")
        super.onDestroyView()
    }

    override fun onDestroy() {
        Log.d("jecesAddFragment", "onDestroy")
        super.onDestroy()
    }

    override fun onDetach() {
        Log.d("jecesAddFragment", "onDetach")
        super.onDetach()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}