package com.dhbw.brainstorm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.dhbw.brainstorm.adapter.ContributionsAdapter
import com.dhbw.brainstorm.api.CommonClient
import com.dhbw.brainstorm.api.RoomClient
import com.dhbw.brainstorm.api.model.Room
import com.dhbw.brainstorm.api.model.RoomState
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_room.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

class RoomActivity : AppCompatActivity() {
    private lateinit var adapter: ContributionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)
        val roomId = intent.getIntExtra("roomId", -1)
        validateRoomId(roomId)
        adapter = ContributionsAdapter()
        findViewById<RecyclerView>(R.id.contributionList).adapter = adapter


    }

    fun validateRoomId(roomId: Int) {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        val client = Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .baseUrl(getString(R.string.backendUrl))
            .client(httpClient)
            .build()
            .create(CommonClient::class.java)
        client.validateRoomId(roomId).enqueue(object : Callback<Boolean> {
            override fun onResponse(
                call: Call<Boolean>,
                response: Response<Boolean>
            ) {
                if (response.code() == 200) {
                    if (!response.body()!!) {
                        Toast.makeText(
                            applicationContext,
                            "Invalid Room-ID",
                            Toast.LENGTH_LONG
                        ).show()
                        goToHome()
                    } else {
                        validatePassword(roomId)
                    }
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Something went wrong. Please try again or come back later.",
                        Toast.LENGTH_LONG
                    ).show()
                    goToHome()
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    applicationContext,
                    "Something went wrong. Please try again or come back later.",
                    Toast.LENGTH_LONG
                ).show()
                goToHome()
            }

        })
    }

    fun validatePassword(roomId: Int) {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
        val httpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val client = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .baseUrl(getString(R.string.backendUrl))
            .build()
            .create(CommonClient::class.java)
        client.hasPassword(roomId).enqueue(object : Callback<Boolean> {
            override fun onResponse(
                call: Call<Boolean>,
                response: Response<Boolean>
            ) {

                if (response.code() == 200) {
                    if(response.body()!!){
                        // TODO: Passworteingabe und PW prüfen

                    }else{
                        fetchRoom(roomId)
                    }
                } else {

                    Toast.makeText(
                        applicationContext,
                        "Something went wrong. Please try again or come back later.",
                        Toast.LENGTH_LONG
                    ).show()
                    goToHome()
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    "Something went wrong. Please try again or come back later.",
                    Toast.LENGTH_LONG
                ).show()
                goToHome()
            }

        })

    }

    fun fetchRoom(roomId: Int) {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
        val httpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val client = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .baseUrl(getString(R.string.backendUrl))
            .build()
            .create(RoomClient::class.java)
        client.getRoom(roomId).enqueue(object : Callback<Room> {
            override fun onResponse(
                call: Call<Room>,
                response: Response<Room>
            ) {

                if (response.code() == 200) {
                    roomProgress.visibility = View.GONE
                    roomLayout.visibility = View.VISIBLE
                    var room = response.body()!!
                    adapter.update(room.contributions, room.state)
                    Thread.sleep(2000)
                    fetchRoom(roomId)
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Something went wrong. Please try again or come back later.",
                        Toast.LENGTH_LONG
                    ).show()
                    goToHome()
                }
            }

            override fun onFailure(call: Call<Room>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    "Something went wrong. Please try again or come back later.",
                    Toast.LENGTH_LONG
                ).show()
                goToHome()
            }

        })
    }

    fun goToHome() {
        var intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}

