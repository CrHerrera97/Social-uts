package com.uts.socialuts.models

class SliderItem {
    var imageUrl: String? = null
    var timestamp: Long = 0

    constructor() {}
    constructor(imageUrl: String?, timestamp: Long) {
        this.imageUrl = imageUrl
        this.timestamp = timestamp
    }
}