package com.example.applicationjeces.user

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.applicationjeces.MainActivity
import com.example.applicationjeces.R
import com.example.applicationjeces.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.fragment_login.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        login()

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        pageMove(FirebaseAuth.getInstance().currentUser)
    }

    fun login() {
        binding.loginBtn.setOnClickListener {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(loginTextInputEditText.text.toString().trim(), loginPasswordEditText.text.toString().trim())
                .addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        /* 로그인 */
                        /* 인증받은 사용자인지 확인 */
                        if(FirebaseAuth.getInstance().currentUser?.isEmailVerified!!) {
                            Toast.makeText(context, "로그인 성공", Toast.LENGTH_LONG).show()
                            pageMove(task.result?.user)
                        } else {
                            Toast.makeText(context, task.exception.toString(), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, task.exception.toString(), Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    fun pageMove(user: FirebaseUser?) {
        if(user != null) {
            val it = Intent(context, MainActivity::class.java)
            startActivity(it)

            /* 애니메이션 적용 */
            activity?.overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LoginFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoginFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}