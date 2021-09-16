package com.simple.messenger.model

class ChatMessage(val id: String,
                  val text: String,
                  val imageUrl: String,
                  val fromId: String,
                  val toId: String,
                  val timestamp: Long,
                  var alreadyRead: Boolean) {
    constructor(): this("", "", "", "", "", 0L, false)
}

