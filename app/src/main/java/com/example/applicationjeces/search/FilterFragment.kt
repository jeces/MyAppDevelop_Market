import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.applicationjeces.databinding.FragmentFilterSearchBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FilterFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentFilterSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFilterSearchBinding.inflate(inflater, container, false)

        // 필터 적용 버튼 리스너 설정
        binding.applyFilterButton.setOnClickListener {
            // 필터링 로직 작성
            dismiss()  // 필터 적용 후 다이얼로그 닫기
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
