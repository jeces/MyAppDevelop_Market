import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.applicationjeces.databinding.BidBottomSheetLayoutBinding
import com.example.applicationjeces.product.ProductViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

import com.google.android.material.snackbar.Snackbar
import android.os.VibrationEffect
import android.os.Vibrator
import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applicationjeces.MainActivity
import com.example.applicationjeces.R
import java.text.NumberFormat
import java.util.*

class CategoryBottomSheetFragment : BottomSheetDialogFragment() {

    interface CategoryListener {
        fun onCategorySelected(category: String)
    }

    private var categories = arrayOf("Category1", "Category2", "...") // 여기에 카테고리 목록을 넣어주세요.

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_category_bottom_sheet, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        recyclerView.adapter = CategoryAdapter(categories) { selectedCategory ->
            (activity as? CategoryListener)?.onCategorySelected(selectedCategory)
            dismiss()
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        return view
    }

    class CategoryAdapter(
        private val categories: Array<String>,
        private val onClick: (String) -> Unit
    ) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {
        class ViewHolder(val view: TextView) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false) as TextView
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = categories.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.view.text = categories[position]
            holder.view.setOnClickListener { onClick(categories[position]) }
        }
    }
}
