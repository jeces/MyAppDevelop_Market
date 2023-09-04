import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import com.example.applicationjeces.databinding.FragmentFilterSearchBinding
import com.example.applicationjeces.search.FilterCriteria
import com.example.applicationjeces.search.FilterListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FilterFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentFilterSearchBinding? = null
    private val binding get() = _binding!!

    private var filterListener: FilterListener? = null
    var currentFilterCriteria: FilterCriteria? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFilterSearchBinding.inflate(inflater, container, false)


        // 초기 상태 설정
        setCurrentFilterState()

        // 필터 적용 버튼 리스너 설정
        binding.applyFilterButton.setOnClickListener {
            val filterCriteria = FilterCriteria(
                minPrice = binding.minPriceEditText.text.toString().toIntOrNull(),
                maxPrice = binding.maxPriceEditText.text.toString().toIntOrNull(),
                category = binding.categorySpinner.selectedItem.toString(),
                includeSaleCompleted = binding.saleCompletedCheckBox.isChecked,
                sameRegion = binding.sameRegionCheckBox.isChecked
            )

            currentFilterCriteria = filterCriteria
            filterListener?.onFilterApplied(filterCriteria)
            dismiss()
        }

        return binding.root
    }

    private fun setCurrentFilterState() {
        Log.d("adadada111", currentFilterCriteria.toString())
        currentFilterCriteria?.let {
            Log.d("adadada111", "${it.minPrice?.toString()}/${it.maxPrice?.toString()}/")
            binding.minPriceEditText.setText(it.minPrice?.toString() ?: "")
            binding.maxPriceEditText.setText(it.maxPrice?.toString() ?: "")
            // 카테고리는 스피너의 아이템 위치를 기반으로 설정해야 합니다. 예를 들면:
            val categoryPosition = (binding.categorySpinner.adapter as ArrayAdapter<String>).getPosition(it.category)
            binding.categorySpinner.setSelection(categoryPosition)

            binding.saleCompletedCheckBox.isChecked = it.includeSaleCompleted
            binding.sameRegionCheckBox.isChecked = it.sameRegion
        }
    }


    fun setFilterListener(listener: FilterListener) {
        this.filterListener = listener
    }

    fun updateCurrentFilterCriteria(criteria: FilterCriteria) {
        currentFilterCriteria = criteria
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
