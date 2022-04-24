package com.uts.socialuts.models

class Comment {
    var id: String? = null
    var comment: String? = null
    var idUser: String? = null
    var idPost: String? = null
    var timestamp: Long = 0

    constructor() {}
    constructor(id: String?, comment: String?, idUser: String?, idPost: String?, timestamp: Long) {
        this.id = id
        this.comment = comment
        this.idUser = idUser
        this.idPost = idPost
        this.timestamp = timestamp
    }
}