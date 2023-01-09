package com.example.applicationjeces.chat

import com.google.firebase.Timestamp

data class ChatroomData(
    val chatidx: String,
    val id: String,
    val lastcomment: String,
    val n0: String,
    val n1: String,
    val time: Timestamp?
    )
