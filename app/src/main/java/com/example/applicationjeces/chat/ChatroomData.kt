package com.example.applicationjeces.chat

import com.google.firebase.Timestamp

data class ChatroomData(
    val idx : String,
    val lastComment: String,
    val yourImg: String,
    val yourid: String,
    val n0: String,
    val n1: String,
    val time: Timestamp
    )
