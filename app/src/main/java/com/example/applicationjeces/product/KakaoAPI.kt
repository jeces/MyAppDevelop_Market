package com.example.applicationjeces.product

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface KakaoAPI {
    @Headers("Authorization: KakaoAK 103b3b720839955ba692e653677062b7")
    @GET("v2/local/geo/coord2address.json")
    fun getReverseGeo(
        @Query("x") longitude: Double,
        @Query("y") latitude: Double
    ): Call<KakaoResponse>
}
