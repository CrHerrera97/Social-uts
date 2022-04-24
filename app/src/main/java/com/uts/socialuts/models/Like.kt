package com.uts.socialuts.models

class Like {
    var id: String? = null
    var idPost: String? = null
    var idUser: String? = null
    var timestamp: Long = 0

    constructor() {}
    constructor(id: String?, idPost: String?, idUser: String?, timestamp: Long) {
        this.id = id
        this.idPost = idPost
        this.idUser = idUser
        this.timestamp = timestamp
    }
}