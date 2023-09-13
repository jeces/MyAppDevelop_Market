package com.example.applicationjeces.user

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class LoginRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun createUser(email: String, password: String): Task<AuthResult> {
        return auth.createUserWithEmailAndPassword(email.trim(), password.trim())
    }

    fun sendEmailVerification(): Task<Void>? {
        return auth.currentUser?.sendEmailVerification()
    }

    fun addUserToFirestore(id: String, name: String): Task<DocumentReference> {
        val user = hashMapOf(
            "id" to id,
            "name" to name
        )
        return db.collection("UserInfo").add(user)
    }

    fun loginUser(email: String, password: String, onComplete: (Result<FirebaseUser?>) -> Unit) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(Result.success(task.result?.user))
            } else {
                val exception = task.exception ?: Exception("Unknown error occurred")
                onComplete(Result.failure(exception))
            }
        }
    }
}
