package com.uts.socialuts.models

class Post {
    var id: String? = null
    var title: String? = null
    var description: String? = null
    var image1: String? = null
    var image2: String? = null
    var idUser: String? = null
    var category: String? = null
    var timestamp: Long = 0

    constructor() {}
    constructor(
        id: String?,
        title: String?,
        description: String?,
        image1: String?,
        image2: String?,
        idUser: String?,
        category: String?,
        timestamp: Long
    ) {
        this.id = id
        this.title = title
        this.description = description
        this.image1 = image1
        this.image2 = image2
        this.idUser = idUser
        this.category = category
        this.timestamp = timestamp
    }
}