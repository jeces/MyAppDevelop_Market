package com.example.applicationjeces.user

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.applicationjeces.MainActivity
import com.example.applicationjeces.R
import com.example.applicationjeces.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseUser

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        val (savedEmail, savedPassword) = SharedPreferencesHelper.getCredentials(requireContext())
        if (savedEmail != null && savedPassword != null) {
            viewModel.loginUser(savedEmail, savedPassword)
        }

        binding.loginTextInputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !isValidEmail(binding.loginTextInputEditText.text.toString().trim())) {
                binding.loginTextInputEditText.error = "이메일 형식을 확인해주세요."
            } else {
                binding.loginTextInputEditText.error = null
            }
        }

        // 체크박스 상태 저장
        binding.rememberMeCheckBox.setOnCheckedChangeListener { _, isChecked ->
            val editor = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
            editor?.putBoolean("rememberMe", isChecked)
            editor?.apply()
        }

        binding.autoLoginCheckBox.setOnCheckedChangeListener { _, isChecked ->
            val editor = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
            editor?.putBoolean("autoLogin", isChecked)
            editor?.apply()
        }

        // 체크박스 상태 불러오기
        val prefs = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        binding.rememberMeCheckBox.isChecked = prefs?.getBoolean("rememberMe", false) ?: false
        binding.autoLoginCheckBox.isChecked = prefs?.getBoolean("autoLogin", false) ?: false


        login()

        // ViewModel에서 결과 관찰
        viewModel.loginResult.observe(viewLifecycleOwner, Observer { result ->
            result.fold(
                onSuccess = { user ->
                    val email = binding.loginTextInputEditText.text.toString().trim()
                    val password = binding.loginPasswordEditText.text.toString().trim()
                    if (user?.isEmailVerified == true) {
                        // 아이디와 비밀번호를 SharedPreferences에 저장
                        SharedPreferencesHelper.saveCredentials(email, password, requireContext())
                        pageMove(user)
                    } else {
                        showAlert("인증 오류", "이메일 인증을 완료해주세요.")
                    }
                },
                onFailure = { exception ->
                    when (exception?.message) {
                        "There is no user record corresponding to this identifier. The user may have been deleted." -> showAlert("로그인 오류", "회원 정보가 존재하지 않습니다.")
                        "The password is invalid or the user does not have a password." -> showAlert("로그인 오류", "비밀번호가 다릅니다.")
                        else -> showAlert("로그인 오류", "알 수 없는 오류가 발생했습니다. 다시 시도해주세요.")
                    }
                }
            )
        })

        return binding.root
    }

    override fun onStart() {
        super.onStart()
//        pageMove(FirebaseAuth.getInstance().currentUser)
    }

    fun login() {
        binding.loginBtn.setOnClickListener {
            val email = binding.loginTextInputEditText.text.toString().trim()
            val password = binding.loginPasswordEditText.text.toString().trim()

            if (!isValidEmail(email)) {
                showAlert("오류", "이메일 형식을 확인해주세요.")
                return@setOnClickListener
            }

            viewModel.loginUser(email, password)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex(pattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")
        return emailRegex.matches(email)
    }

    private fun showAlert(title: String, message: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("확인", null)
        val dialog = builder.show()

        // 스타일 설정은 여기에서도 할 수 있습니다.
        // 제목의 텍스트 스타일을 변경
        dialog.findViewById<TextView>(android.R.id.title)?.apply {
            setTextColor(Color.BLUE)
            textSize = 15f
        }
        // 메시지의 텍스트 스타일을 변경
        dialog.findViewById<TextView>(android.R.id.message)?.apply {
            setTextColor(Color.RED)
            textSize = 13f
        }
        // 버튼의 텍스트 스타일을 변경
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
            setTextColor(Color.GRAY)
            textSize = 11f
        }

        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_bg)
    }

    fun pageMove(user: FirebaseUser?) {
        if(user != null) {
            val it = Intent(context, MainActivity::class.java)
            startActivity(it)

            /* 애니메이션 적용 */
            activity?.overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom)
        }
    }

    object SharedPreferencesHelper {
        private const val PREFS_NAME = "com.example.applicationjeces.userprefs"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"

        fun saveCredentials(email: String, password: String, context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putString(KEY_EMAIL, email)
            editor.putString(KEY_PASSWORD, password)
            editor.apply()
        }

        fun getCredentials(context: Context): Pair<String?, String?> {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val email = prefs.getString(KEY_EMAIL, null)
            val password = prefs.getString(KEY_PASSWORD, null)
            return Pair(email, password)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = LoginFragment()
    }
}