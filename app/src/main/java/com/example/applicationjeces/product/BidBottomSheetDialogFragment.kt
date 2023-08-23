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
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import java.text.NumberFormat
import java.util.*

class BidCustomBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var productViewModel: ProductViewModel

    private var _binding: BidBottomSheetLayoutBinding? = null
    private val binding get() = _binding!!

    var currentBidPrice = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val pId = arguments?.getString("pId") ?: ""
        val pName = arguments?.getString("pName") ?: ""
        currentBidPrice = arguments?.getInt("productBidPrice") ?: 0

        /**
         * view 바인딩
         */
        _binding = BidBottomSheetLayoutBinding.inflate(inflater, container, false)
        val view = binding.root

        /**
         * 단위 입력(,)
         */
        binding.bidAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Do nothing here
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Do nothing here
            }

            override fun afterTextChanged(s: Editable) {
                binding.bidAmount.removeTextChangedListener(this)  // prevent infinite loop

                val str = s.toString().replace(",", "")
                if (str.isNotEmpty()) {
                    val formattedNumber = NumberFormat.getNumberInstance(Locale.US).format(str.toLong())
                    binding.bidAmount.setText(formattedNumber)
                    binding.bidAmount.setSelection(formattedNumber.length)  // move cursor to the end
                }

                binding.bidAmount.addTextChangedListener(this)  // re-add to start listening again
            }
        })


        /**
         * 뷰모델 초기화 생성자
         **/
        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        /**
         * 입찰가 업데이트(상시)
         */
        productViewModel.startListeningForBidUpdates(pId, pName)
        productViewModel.bidPrice.observe(viewLifecycleOwner, Observer { productBidPrices ->
            Log.d("afafaf", "${productBidPrices}")
            currentBidPrice = productBidPrices.toInt()
        })

        binding.bidSubmitButton.setOnClickListener {
            val bidAmount = binding.bidAmount.text.toString().trim().replace(",", "").toInt().toString()
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
    @RequiresApi(Build.VERSION_CODES.O)
    private fun processBid(pId : String, pName : String, bid_price : String) {
        val bidPriceWithoutComma = bid_price.replace(",", "").toInt()

        if (bidPriceWithoutComma <= currentBidPrice) {
            // Snackbar로 경고 메시지 표시
            Snackbar.make(binding.root, "입찰 가격이 현재 가격보다 낮습니다.", Snackbar.LENGTH_SHORT).show()
            binding.errorMessage.visibility = View.VISIBLE

            // 짧은 진동으로 사용자에게 피드백 제공
            val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))

            // 입력 필드 애니메이션 (예: 살짝 흔들기)
            binding.bidAmount.animate().translationX(10f).setDuration(100).withEndAction {
                binding.bidAmount.animate().translationX(-10f).setDuration(100).withEndAction {
                    binding.bidAmount.translationX = 0f
                }.start()
            }.start()
            return
        }

        lifecycleScope.launch {
            try {
                productViewModel.bidchange(pId, pName, bid_price).await()
                Toast.makeText(requireContext(), "$bid_price 원으로 입찰하였습니다.", Toast.LENGTH_SHORT).show()
                dismiss()
            } catch (e: Exception) {
                // Handle the exception if needed
                val errorMessage = Toast.makeText(requireContext(), "입찰 중 오류가 발생했습니다.", Toast.LENGTH_SHORT)
                errorMessage.show()
            }
        }
    }
}