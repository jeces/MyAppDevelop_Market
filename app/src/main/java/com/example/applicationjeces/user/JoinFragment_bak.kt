package com.example.applicationjeces.user

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.applicationjeces.MainActivity
import com.example.applicationjeces.R
import com.example.applicationjeces.databinding.FragmentJoinBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * A simple [Fragment] subclass.
 * Use the [JoinFragment_bak.newInstance] factory method to
 * create an instance of this fragment.
 */
/* 회원가입 */
class JoinFragment_bak : Fragment() {

    private var _binding: FragmentJoinBinding? = null
    private val binding get() = _binding!!

    /* firebase Auth */
    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentJoinBinding.inflate(inflater, container, false)

        /* 로그인 세센을 체크하는 부분 */
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            Log.e("로그인", "로그인")
            val user = firebaseAuth.currentUser
            if(user != null) {
                val it = Intent(context, MainActivity::class.java)
                startActivity(it)
            }
        }

//        join()

        return binding.root
    }

    override fun onStart() {
        super.onStart()

    }
//
//    fun join() {
//        binding.joinBtn.setOnClickListener {
//            FirebaseAuth.getInstance()
//                .createUserWithEmailAndPassword(textInputEditText.text.toString().trim(), passwordEditText.text.toString().trim())
//                .addOnCompleteListener { task ->
//                    if(task.isSuccessful) {
//                        Log.d("가입성공", textInputEditText.text.toString().trim())
//                        showEmailVerificationDialog()
//                        /* 메일 인증 넣기 */
//                        FirebaseAuth.getInstance().currentUser
//                            ?.sendEmailVerification()
//                            ?.addOnCompleteListener { emailTast ->
//                                if(emailTast.isSuccessful) {
//                                    val users = hashMapOf(
//                                        "id" to textInputEditText.text.toString(),
//                                        "name" to nameEditText.text.toString()
//                                    )
//                                    FirebaseFirestore.getInstance()!!.collection("UserInfo").add(users)
//                                        .addOnSuccessListener {
//                                            Toast.makeText(context, "회원가입 성공", Toast.LENGTH_LONG).show()
//                                        }
//                                        .addOnFailureListener {
//                                            Toast.makeText(context, "회원가입 실패", Toast.LENGTH_LONG).show()
//                                        }
//                                } else {
//                                    Toast.makeText(
//                                        context,
//                                        task.exception.toString(),
//                                        Toast.LENGTH_LONG
//                                    ).show()
//                                }
//                            }
//                    } else {
//                        Log.d("가입실패", textInputEditText.text.toString().trim())
//                        Toast.makeText(context, task.exception.toString(), Toast.LENGTH_LONG).show()
//
//                    }
//                }
//        }
//    }

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
            JoinFragment_bak().apply {
                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
                }
            }
    }
}