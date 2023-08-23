import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.applicationjeces.databinding.BidBottomSheetLayoutBinding
import com.example.applicationjeces.databinding.SetBottomSheetLayoutBinding
import com.example.applicationjeces.product.ProductViewModel
import com.example.applicationjeces.user.LoginActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class BidCustomBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var productViewModel: ProductViewModel

    private var _binding: BidBottomSheetLayoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val pId = arguments?.getString("pId") ?: ""
        val pName = arguments?.getString("pName") ?: ""

        /**
         * view 바인딩
         */
        _binding = BidBottomSheetLayoutBinding.inflate(inflater, container, false)
        val view = binding.root

        /**
         * 뷰모델 초기화 생성자
         **/
        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        binding.bidSubmitButton.setOnClickListener {
            val bidAmount = binding.bidAmount.text.toString().trim()
            if (bidAmount.isEmpty()) {
                Toast.makeText(requireContext(), "금액을 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 금액 처리
            processBid(pId, pName, bidAmount)  // 이는 실제로 입찰 금액을 처리하는 함수로 정의해야 합니다.
        }

        return view
    }

    /**
     * processBid에서 bidchange를 호출하고 완료될 때까지 기다림
     */
    private fun processBid(pId : String, pName : String, bid_price : String) {
        lifecycleScope.launch {
            try {
                productViewModel.bidchange(pId, pName, bid_price).await()
                Toast.makeText(requireContext(), "$bid_price 원으로 입찰하였습니다.", Toast.LENGTH_SHORT).show()
                dismiss()
            } catch (e: Exception) {
                // Handle the exception if needed
            }
        }
    }
}