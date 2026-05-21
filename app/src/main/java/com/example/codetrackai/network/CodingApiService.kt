package com.example.codetrackai.network

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// LeetCode API Response Model (Puraana Safe)
data class LeetCodeResponse(
    @SerializedName("totalSolved") val totalSolved: Int?
)

interface LeetCodeApiService {
    @GET("{username}")
    suspend fun getLeetCodeStats(@Path("username") username: String): LeetCodeResponse
}

// Codeforces Status Response Model (Puraana Safe)
data class CFStatusResponse(
    val status: String,
    val result: List<CFSubmission>?
)
data class CFSubmission(
    val verdict: String?
)

// 🌟 NEW: Codeforces Rating Fetch karne ke liye naya data model
data class CFRatingResponse(
    val status: String,
    val result: List<CFRatingInfo>?
)
data class CFRatingInfo(
    val handle: String,
    val rating: Int?,
    val maxRating: Int?,
    val rank: String?
)

interface CodeforcesApiService {
    @GET("user.status")
    suspend fun getCodeforcesStatus(@Query("handle") handle: String): CFStatusResponse

    // 🌟 NEW: Codeforces user info aur rating laane ke liye naya endpoint
    @GET("user.info")
    suspend fun getCodeforcesInfo(@Query("handles") handles: String): CFRatingResponse
}

object RetrofitClient {
    val leetCodeApi: LeetCodeApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://leetcode-api-faisalshohag.vercel.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LeetCodeApiService::class.java)
    }

    val codeforcesApi: CodeforcesApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://codeforces.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(CodeforcesApiService::class.java)
    }
}