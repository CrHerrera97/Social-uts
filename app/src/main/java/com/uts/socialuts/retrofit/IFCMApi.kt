package com.uts.socialuts.retrofit

import com.uts.socialuts.models.FCMBody
import com.uts.socialuts.models.FCMResponse
import retrofit2.http.POST
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers

interface IFCMApi {
    @Headers(
        "Content-Type:application/json",
        "Authorization:key=AAAAbSRAigw:APA91bH2qgfj_M_t3pAAsN82EXIrfYPtMGixfBKNSzQOnBwPVQazuZmTXMnfoSDYfoluEB5vVdsXJGMq_r8ku3rZ9QnmeQ9CTriqYsGX8CA0mFwGXUc8dMgsRQzNFVi49RiPjd7W2_cY"
    )
    @POST("fcm/send")
    fun send(@Body body: FCMBody?): Call<FCMResponse>
}