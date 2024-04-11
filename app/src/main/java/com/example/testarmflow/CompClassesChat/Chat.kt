package com.example.testarmflow.CompClassesChat

class Chat {
    private lateinit var sender: String
    private lateinit var message: String
    private lateinit var receiver: String
    private var isseen = false
    private lateinit var url: String
    private lateinit var messageID: String

    constructor()
    constructor(
        sender: String,
        message: String,
        receiver: String,
        isseen: Boolean,
        url: String,
        messageID: String
    ) {
        this.sender = sender
        this.message = message
        this.receiver = receiver
        this.isseen = isseen
        this.url = url
        this.messageID = messageID
    }

    fun getSender(): String? {
        return sender
    }

    fun setSender(sender: String?){
        this.sender = sender!!
    }

    fun getMessage(): String? {
        return message
    }

    fun setMessage(message: String?){
        this.message = message!!
    }

    fun getReceiver(): String? {
        return receiver
    }

    fun setReceiver(receiver: String?){
        this.receiver = receiver!!
    }

    fun getIsSeen(): Boolean {
        return isseen
    }

    fun setIsSeen(isseen: Boolean?){
        this.isseen = isseen!!
    }

    fun getUrl(): String? {
        return url
    }

    fun setUrl(url: String?){
        this.url = url!!
    }

    fun getMessageID(): String? {
        return messageID
    }

    fun setMessageID(messageID: String?){
        this.messageID = messageID!!
    }

}