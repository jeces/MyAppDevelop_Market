import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.applicationjeces.databinding.SetBottomSheetLayoutBinding
import com.example.applicationjeces.product.ProductViewModel
import com.example.applicationjeces.user.EditProfileActivity
import com.example.applicationjeces.user.LoginActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth

class SettingCustomBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var productViewModel: ProductViewModel

    private var _binding: SetBottomSheetLayoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        /**
         * view 바인딩
         */
        _binding = SetBottomSheetLayoutBinding.inflate(inflater, container, false)
        val view = binding.root

        /**
         * 뷰모델 초기화 생성자
         **/
        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]


        // 항목 클릭 리스너 설정
        binding.editProfile.setOnClickListener {
            // 프로필 수정 액션
            /**
             * 프로필 수정
             */
            val intent = Intent(activity, EditProfileActivity::class.java)
            startActivity(intent)
            dismiss()
        }

        binding.notificationSettings.setOnClickListener {
            // 알림 설정 액션
            dismiss()
        }

        binding.helpFeedback.setOnClickListener {
            // 도움말 & 피드백 액션
            dismiss()
        }

        binding.logout.setOnClickListener {
            // 로그아웃 액션
            /* 로그아웃 */
            FirebaseAuth.getInstance().signOut()
            /* 페이지 이동 */
            // val it = Intent(this, LoginActivity::class.java)
            val it = Intent(requireActivity(), LoginActivity::class.java) // 'this'를 'requireActivity()'로 변경
            requireActivity().startActivity(it) // 'startActivity()' 앞에 'requireActivity().' 추가
            dismiss()
        }

        return view
    }
}