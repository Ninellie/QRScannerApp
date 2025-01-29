package com.example.qrscanner

import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface ApiService {
    @POST
    suspend fun sendQrCode(
        @Header("Authorization") token: String,
        @Query("QrCode") code: String
    ): Response<QrCodeResponse>

    suspend fun sendQrCodeWithCustomUrl(
        baseUrl: String,
        token: String,
        code: String
    ): Response<QrCodeResponse> {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(provideOkHttpClient())
            .build()

        val dynamicApiService = retrofit.create(ApiService::class.java)
        return dynamicApiService.sendQrCode(token, code)
    }
}

data class QrCodeResponse(
    val status: String,
    val message: String,
    val data: QrCodeResponseData
)

data class QrCodeResponseData(
    val id: Int,
    val user_id: Int,
    val url: String,
    val iic: String,
    val tin: String,
    val prc: String,
    val crtd: String,
    val status: Int,
    val created_at: String,
    val updated_at: String
)

object RetrofitInstance {
    private const val BASE_URL = ""

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Для работы с JSON
            .client(provideOkHttpClient())
            .build()
            .create(ApiService::class.java)
    }
}

private fun provideOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // Увеличиваем время ожидания соединения
        .readTimeout(60, TimeUnit.SECONDS)    // Увеличиваем время ожидания ответа
        .writeTimeout(30, TimeUnit.SECONDS)   // Увеличиваем время ожидания отправки данных
        .build()
}
