package com.uts.socialuts.models

import java.util.ArrayList

class Chat {
    var id: String? = null
    var idUser1: String? = null
    var idUser2: String? = null
    var idNotification = 0
    var isWriting = false
    var timestamp: Long = 0
    var ids: ArrayList<String>? = null

    constructor() {}
    constructor(
        id: String?,
        idUser1: String?,
        idUser2: String?,
        idNotification: Int,
        isWriting: Boolean,
        timestamp: Long,
        ids: ArrayList<String>?
    ) {
        this.id = id
        this.idUser1 = idUser1
        this.idUser2 = idUser2
        this.idNotification = idNotification
        this.isWriting = isWriting
        this.timestamp = timestamp
        this.ids = ids
    }
}