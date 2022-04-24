package com.uts.socialuts.models

import java.util.ArrayList

class FCMResponse(
    var multicast_id: Long,
    var success: Int,
    var failure: Int,
    var canonical_ids: Int,
    results: ArrayList<Any>
) {
    var results = ArrayList<Any>()

    init {
        this.results = results
    }
}