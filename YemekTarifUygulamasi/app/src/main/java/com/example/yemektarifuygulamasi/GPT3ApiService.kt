package com.example.yemektarifuygulamasi

import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface GPT3ApiService {
    @POST("v1/chat/completions")
    @Headers("Authorization: Bearer YOUR_KEY")
    fun generateRecipe(@Body body: RequestBody): Call<ResponseBody>
    companion object {
        private const val BASE_URL = "https://api.openai.com/"
        fun create(): GPT3ApiService {
            val client = OkHttpClient.Builder().build()
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(GPT3ApiService::class.java)
        }
    }
}
