package com.example.kotlinmessenger.model

class ChatMessage(val id: String,
                  val text: String,
                  val imageUrl: String,
                  val fromId: String,
                  val toId: String,
                  val timestamp: Long) {
    constructor(): this("", "", "", "", "", -1)
}

