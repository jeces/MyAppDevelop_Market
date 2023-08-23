package com.example.applicationjeces.chat

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.applicationjeces.product.Product
import com.example.applicationjeces.product.Response
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/* 뷰모델은 DB에 직접 접근하지 않아야함. Repository 에서 데이터 통신 */
class ChatRepositor {


}