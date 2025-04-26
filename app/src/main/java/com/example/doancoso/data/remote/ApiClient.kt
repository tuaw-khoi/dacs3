package com.example.doancoso.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "https://travel-ai-api-production.up.railway.app/"

    // Thiết lập OkHttpClient với các timeout
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(100, TimeUnit.SECONDS)  // Thời gian kết nối tối đa 30 giây
        .readTimeout(100, TimeUnit.SECONDS)     // Thời gian đọc dữ liệu tối đa 30 giây
        .writeTimeout(100, TimeUnit.SECONDS)    // Thời gian ghi dữ liệu tối đa 30 giây
        .build()

    // Thiết lập Retrofit với OkHttpClient
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)  // Sử dụng OkHttpClient đã cấu hình
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Tạo ApiClient
    val apiClient: ApiClient by lazy {
        retrofit.create(ApiClient::class.java)
    }
}
