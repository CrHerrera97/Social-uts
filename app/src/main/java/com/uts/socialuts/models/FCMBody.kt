package com.uts.socialuts.models

class FCMBody(
    var to: String?,
    var priority: String,
    var ttl: String,
    var data: Map<String, String?>
)