package com.uts.socialuts.providers

import com.uts.socialuts.models.FCMBody
import com.uts.socialuts.models.FCMResponse
import com.uts.socialuts.retrofit.RetrofitClient
import com.uts.socialuts.retrofit.IFCMApi
import retrofit2.Call

class NotificationProvider {
    private val url = "https://fcm.googleapis.com"
    fun sendNotification(body: FCMBody?): Call<FCMResponse> {
        return RetrofitClient.getClient(url).create(IFCMApi::class.java).send(body)
    }
}