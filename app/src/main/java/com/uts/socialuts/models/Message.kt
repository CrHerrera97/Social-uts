package com.uts.socialuts.models

class Message {
    var id: String? = null
    var idSender: String? = null
    var idReceiver: String? = null
    var idChat: String? = null
    var message: String? = null
    var timestamp: Long = 0
    var isViewed = false

    constructor() {}
    constructor(
        id: String?,
        idSender: String?,
        idReceiver: String?,
        idChat: String?,
        message: String?,
        timestamp: Long,
        viewed: Boolean
    ) {
        this.id = id
        this.idSender = idSender
        this.idReceiver = idReceiver
        this.idChat = idChat
        this.message = message
        this.timestamp = timestamp
        isViewed = viewed
    }
}