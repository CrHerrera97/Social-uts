package com.uts.socialuts.models

class User {
    var id: String? = null
    var email: String? = null
    var username: String? = null
    var phone: String? = null
    var imageProfile: String? = null
    var imageCover: String? = null
    var timestamp: Long = 0
    var lastConnection: Long = 0
    var isOnline = false

    constructor() {}
    constructor(
        id: String?,
        email: String?,
        username: String?,
        phone: String?,
        imageProfile: String?,
        imageCover: String?,
        timestamp: Long,
        lastConnection: Long,
        online: Boolean
    ) {
        this.id = id
        this.email = email
        this.username = username
        this.phone = phone
        this.imageProfile = imageProfile
        this.imageCover = imageCover
        this.timestamp = timestamp
        this.lastConnection = lastConnection
        isOnline = online
    }


}