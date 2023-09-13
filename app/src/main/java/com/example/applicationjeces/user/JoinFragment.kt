package com.example.applicationjeces.user

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.applicationjeces.R
import com.example.applicationjeces.databinding.FragmentJoinBinding

class JoinFragment : Fragment() {

    private var _binding: FragmentJoinBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentJoinBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        setupInputValidation()
        setJoinButtonClickListener()
        observeSignUpStatus()

        return binding.root
    }

    private fun setupInputValidation() {
        binding.textInputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !isValidEmail(binding.textInputEditText.text.toString().trim())) {
                binding.textInputLayout.error = "이메일 형식을 확인해주세요."
            } else {
                binding.textInputLayout.error = null
            }
        }

        binding.passwordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !isValidPassword(binding.passwordEditText.text.toString().trim())) {
                binding.passwordLayout.error = "비밀번호는 특수문자, 숫자, 영문을 포함해야 합니다."
            } else {
                binding.passwordLayout.error = null
            }
        }

        binding.password2EditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && binding.passwordEditText.text.toString() != binding.password2EditText.text.toString()) {
                binding.password2Layout.error = "비밀번호가 일치하지 않습니다."
            } else {
                binding.password2Layout.error = null
            }
        }

        // NumberEditText formatting has been kept as is due to complexity
        binding.numberEditText.addTextChangedListener(object : TextWatcher {
            private var previousString = ""
            override fun afterTextChanged(s: Editable?) {
                binding.numberEditText.removeTextChangedListener(this)  // TextWatcher 제거
                val str = s.toString().replace("-", "")
                val formattedStr = when {
                    str.length <= 3 -> str
                    str.length <= 7 -> "${str.substring(0, 3)}-${str.substring(3)}"
                    else -> "${str.substring(0, 3)}-${str.substring(3, 7)}-${str.substring(7)}"
                }
                if (formattedStr != previousString) {
                    binding.numberEditText.setText(formattedStr)
                    binding.numberEditText.setSelection(formattedStr.length)
                }
                previousString = formattedStr
                binding.numberEditText.addTextChangedListener(this)  // TextWatcher 다시 추가
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Do nothing
            }
        })

        binding.numberEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !isValidPhoneNumber(binding.numberEditText.text.toString().trim())) {
                binding.numberLayout.error = "전화번호 형식을 확인해주세요."
            } else {
                binding.numberLayout.error = null
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex(pattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")
        return emailRegex.matches(email)
    }

    private fun isValidPassword(password: String): Boolean {
        val hasSpecialChar = Regex(pattern = """[!@#$%^&*()\-=+\\|\[\]{};:'",.<>?/]+""").containsMatchIn(password)
        val hasDigit = password.any { it.isDigit() }
        val hasLetter = password.any { it.isLetter() }
        return hasSpecialChar && hasDigit && hasLetter
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val regex = """^01[016789]-\d{3,4}-\d{4}$""".toRegex()
        return regex.matches(phoneNumber)
    }

    private fun setJoinButtonClickListener() {
        binding.joinBtn.setOnClickListener {
            if (isWarningShown()) {
                Log.d("조인1", "1")
                showWarningCheckDialog()
                return@setOnClickListener
            }
            Log.d("조인2", isWarningShown().toString())
            // 추가적인 검증 및 작업이 필요한 경우 여기에 코드를 추가하십시오.
        }
    }

    private fun observeSignUpStatus() {
        viewModel.signUpStatus.observe(viewLifecycleOwner, Observer { isSuccess ->
            if (isSuccess) {
                showEmailVerificationDialog()
            }
            // 실패 처리에 대한 코드도 여기에 추가하십시오.
        })
    }

    private fun isWarningShown(): Boolean {
        Log.d("조인3", binding.textInputLayout.error.toString())
        return binding.textInputLayout.error != null ||
                binding.passwordLayout.error != null ||
                binding.password2Layout.error != null ||
                binding.numberLayout.error != null ||
                binding.nameLayout.error != null
    }

    private fun showWarningCheckDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("확인 요청")
            .setMessage("모든 경고 메시지를 확인하고 다시 시도해주세요.")
            .setPositiveButton("확인", null)
        val dialog = builder.show()
        // 스타일 설정은 여기에서도 할 수 있습니다.
        // 제목의 텍스트 스타일을 변경
        dialog.findViewById<TextView>(android.R.id.title)?.apply {
            setTextColor(Color.BLUE)
            textSize = 20f
        }
        // 메시지의 텍스트 스타일을 변경
        dialog.findViewById<TextView>(android.R.id.message)?.apply {
            setTextColor(Color.RED)
            textSize = 18f
        }
        // 버튼의 텍스트 스타일을 변경
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
            setTextColor(Color.GREEN)
            textSize = 16f
        }

        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_bg)
    }

    private fun showEmailVerificationDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("이메일 인증")
            .setMessage("메일 인증을 완료하시고 로그인해주세요.")
            .setPositiveButton("확인") { _, _ -> navigateToLoginFragment() }
            .show()
        val dialog = builder.show()
        // 제목의 텍스트 스타일을 변경
        dialog.findViewById<TextView>(android.R.id.title)?.apply {
            setTextColor(Color.BLUE)
            textSize = 20f
        }
        // 메시지의 텍스트 스타일을 변경
        dialog.findViewById<TextView>(android.R.id.message)?.apply {
            setTextColor(Color.RED)
            textSize = 18f
        }
        // 버튼의 텍스트 스타일을 변경
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
            setTextColor(Color.GREEN)
            textSize = 16f
        }
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_bg)
        builder.setCancelable(false)  // 백 버튼 등으로 취소 불가능하게 설정
    }

    private fun navigateToLoginFragment() {
        binding.textInputEditText.text?.clear()
        binding.passwordEditText.text?.clear()
        binding.password2EditText.text?.clear()
        binding.numberEditText.text?.clear()
        (activity as? LoginActivity)?.changeFragment(PageDataLogin.LOGIN)
    }

    companion object {
        @JvmStatic
        fun newInstance() = JoinFragment()
    }
}
