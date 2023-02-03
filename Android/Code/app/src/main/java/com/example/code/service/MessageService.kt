package com.example.code.service

class MessageService {
    companion object{
        fun parseMessage(buffer: ByteArray): String {
            // TODO parse status

            return String(buffer) + "\n"
        }
    }
}