package com.example.applicationjeces.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.applicationjeces.MainActivity
import com.example.applicationjeces.databinding.FragmentJoinBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

        /* 로그인 세센을 체크하는 부분 */
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            Log.e("로그인", "로그인")
            val user = firebaseAuth.currentUser
            if(user != null) {
                val it = Intent(context, MainActivity::class.java)
                startActivity(it)
            }
        }
//
//        authEmail()
        join()
//
//        binding.numberLayout.setEndIconOnClickListener {
//            Toast.makeText(requireContext(), "Clicked", Toast.LENGTH_SHORT).show()
//        }
//
//        binding.textInputEditText.doOnTextChanged {text, start, before, count ->
//            if(text!!.length > 10) {
//                binding.textInputLayout.error = "No More!"
//            } else if(text.length < 10) {
//                binding.textInputLayout.error = null
//            }
//        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()


    }

//    fun authEmail() {
//        binding.authBtn.setOnClickListener {
//            target = true
//        }
//    }
//

    fun join() {
        binding.joinBtn.setOnClickListener {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(textInputEditText.text.toString().trim(), passwordEditText.text.toString().trim())
                .addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        Log.d("가입성공", textInputEditText.text.toString().trim())
                        /* 메일 인증 넣기 */
                        FirebaseAuth.getInstance().currentUser
                            ?.sendEmailVerification()
                            ?.addOnCompleteListener { emailTast ->
                                if(emailTast.isSuccessful) {
                                    val users = hashMapOf(
                                        "id" to textInputEditText.text.toString(),
                                        "name" to "임시이름"
                                    )
                                    FirebaseFirestore.getInstance()!!.collection("UserInfo").add(users)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "회원가입 성공", Toast.LENGTH_LONG).show()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "회원가입 실패", Toast.LENGTH_LONG).show()
                                        }
                                } else {
                                    Toast.makeText(context, task.exception.toString(), Toast.LENGTH_LONG).show()
                                }
                            }
                    } else {
                        Log.d("가입실패", textInputEditText.text.toString().trim())
                        Toast.makeText(context, task.exception.toString(), Toast.LENGTH_LONG).show()

                    }
                }
        }
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