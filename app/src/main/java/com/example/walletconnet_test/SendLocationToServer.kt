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

data class User(val username: String, val name: String)

interface MyApiService {
    @POST("getUserNameUseByWalletAddress")
    fun getUser(@Body request: WalletAddressRequest): Call<User>
}

data class WalletAddressRequest(val walletAddress: String)

class SendLocationToServer {
    private val apiService: MyApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(MyApiService::class.java)
    }

    fun fetchUser(walletAddress: String) {
        val request = WalletAddressRequest(walletAddress)
        val call = apiService.getUser(request)

        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        Log.v("TestName", user.name.toString())
                    }
                } else {
                    Log.v("API request failed", response.code().toString())
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.v("API request failed", t.message.toString())
            }
        })
    }
}