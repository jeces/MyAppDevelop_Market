package com.example.applicationjeces.user

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.applicationjeces.MainActivity
import com.example.applicationjeces.R
import com.example.applicationjeces.databinding.FragmentJoinBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_join.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [JoinFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
/* 회원가입 */
class JoinFragment : Fragment() {

    private var _binding: FragmentJoinBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: LoginViewModel

    /* firebase Auth */
    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
        _binding = FragmentJoinBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

//        /* 로그인 세센을 체크하는 부분 */
//        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
//            Log.e("로그인", "로그인")
//            val user = firebaseAuth.currentUser
//            if(user != null) {
//                val it = Intent(context, MainActivity::class.java)
//                startActivity(it)
//            }
//        }

        join()

        viewModel.signUpStatus.observe(viewLifecycleOwner, Observer { isSuccess ->
            if (isSuccess) {
                showEmailVerificationDialog()
            } else {
                Toast.makeText(context, "가입 실패", Toast.LENGTH_LONG).show()
            }
        })



        return binding.root
    }

    override fun onStart() {
        super.onStart()

    }

    fun join() {
        binding.joinBtn.setOnClickListener {
            val email = textInputEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val name = nameEditText.text.toString().trim()

            viewModel.createUser(email, password, name)
            viewModel.changePageNum(PageDataLogin.LOGIN)
        }
    }

    private fun showEmailVerificationDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("이메일 인증")
        builder.setMessage("메일 인증을 완료하시고 로그인해주세요.")
        builder.setPositiveButton("확인") { _, _ ->
            // 이곳에 확인 버튼을 눌렀을 때 실행될 코드를 넣을 수 있습니다. (예: Fragment 종료 등)
            navigateToLoginFragment()
        }
        builder.setCancelable(false)  // 백 버튼 등으로 취소 불가능하게 설정
        builder.show()
    }

    private fun navigateToLoginFragment() {
        val transaction = requireFragmentManager().beginTransaction()
        transaction.replace(R.id.fragment_join, LoginFragment())
        transaction.commit()

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment JoinFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            JoinFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}