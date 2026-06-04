package com.example.data

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

interface QuranApiService {
    @GET("surah")
    suspend fun getSurahList(): SurahListResponse

    @GET("surah/{surahNumber}/{reciter}")
    suspend fun getSurahDetail(
        @Path("surahNumber") surahNumber: Int,
        @Path("reciter") reciter: String
    ): SurahDetailResponse
}

object QuranApiClient {
    private const val BASE_URL = "https://api.alquran.cloud/v1/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val service: QuranApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(QuranApiService::class.java)
    }
}
