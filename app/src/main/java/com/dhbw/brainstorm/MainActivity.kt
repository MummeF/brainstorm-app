package com.dhbw.brainstorm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.util.JsonReader
import android.view.View
import android.widget.Toast
import com.dhbw.brainstorm.api.CommonClient
import com.dhbw.brainstorm.api.RoomClient
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        checkBackend()

        createRoomButton.setOnClickListener {
            openCreateRoomActivity()
        }




        joinRoomButton.setOnClickListener {
            var intent = Intent(this, JoinActivity::class.java)
            startActivity(intent)
        }

        showFavouriteRoomsButton.setOnClickListener {
            var intent = Intent(this, FavoriteActivity::class.java)
            startActivity(intent)
        }
    }

    fun checkBackend() {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()

        val client = Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .baseUrl(getString(R.string.backendUrl))
            .client(httpClient)
            .build()
            .create(CommonClient::class.java)
        client.isAlive().enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {

                if (response.code() == 200) {
                    progress.visibility = View.GONE
                    wholeLayout.visibility = View.VISIBLE
                } else {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.somethingWentWrongLabel),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    applicationContext,
                    getString(R.string.somethingWentWrongLabel),
                    Toast.LENGTH_LONG
                ).show()
            }

        })
    }

    fun exampleCall() {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
        val httpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val client = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .baseUrl(getString(R.string.backendUrl))
            .build()
            .create(RoomClient::class.java)
        client.increaseRoomState(123).enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {

                if (response.code() == 200) {

                } else {

                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {

            }

        })
    }

    fun openCreateRoomActivity() {
        intent = Intent(this, CreateRoomActivity::class.java)
        startActivity(intent)
    }

}