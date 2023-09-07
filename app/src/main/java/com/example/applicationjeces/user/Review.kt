package com.example.applicationjeces.user

import com.google.firebase.Timestamp

data class Review(
    val to: String,
    val from: String,
    val content: String,
    val rating: Float,      // 추천: 영문 변수명을 사용하는 것이 좋습니다 (예: rating)
    val product: String,   // 추천: 영문 변수명을 사용하는 것이 좋습니다 (예: product)
    val time: String // Firebase의 Timestamp를 사용하였습니다.
)

