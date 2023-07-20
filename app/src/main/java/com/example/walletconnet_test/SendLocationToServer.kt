package com.example.walletconnet_test

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class DataResponse(val address: String, val X: Double, val Y: Double)

data class DataRequest(val address: String, val X: Double, val Y: Double)

interface MyApiService {
    @POST("current-deliver-location")
    fun getUser(@Body request: DataRequest): Call<DataResponse>
}

class SendLocationToServer {
    private val apiService: MyApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://quickertest.shop/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(MyApiService::class.java)
    }

    fun fetchUser(walletAddress: String, X: Double, Y: Double) {
        val request = DataRequest(walletAddress, X, Y)
        val call = apiService.getUser(request)

        call.enqueue(object : Callback<DataResponse> {
            override fun onResponse(call: Call<DataResponse>, response: Response<DataResponse>) {
                if (response.isSuccessful) {
                    val result = response.body()
                    if (result != null) {
                        Log.v("API request succeed", result.toString())
                    }
                } else {
                    Log.v("API request failed", response.code().toString())
                }
            }

            override fun onFailure(call: Call<DataResponse>, t: Throwable) {
                Log.v("API request failed", t.message.toString())
            }
        })
    }
}