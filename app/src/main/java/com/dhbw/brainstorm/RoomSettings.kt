package com.dhbw.brainstorm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.dhbw.brainstorm.api.CommonClient
import com.dhbw.brainstorm.api.RoomClient
import com.dhbw.brainstorm.api.model.Room
import com.dhbw.brainstorm.helper.ROOM_ID_INTENT
import kotlinx.android.synthetic.main.activity_room.*
import kotlinx.android.synthetic.main.activity_room_settings.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RoomSettings : AppCompatActivity() {
    private var roomId = -1
    private lateinit var updatedRoom: Room

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_settings)
        roomId = intent.getIntExtra(ROOM_ID_INTENT, -1)
        getRoom()

        closeRoomBtn.setOnClickListener {
            closeRoom()
        }
        submitChangesBtn.setOnClickListener {
            submitChanges()
        }
    }

    fun getRoom() {
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
                    updatedRoom = response.body()!!
                    newTopicInputField.setText(updatedRoom.topic)
                    newDescriptionInputField.setText(updatedRoom.description)
                    showRoom()
                } else {
                    Toast.makeText(applicationContext, "no room found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Room>, t: Throwable) {
                Toast.makeText(applicationContext, "problem with the API Call", Toast.LENGTH_SHORT).show()
            }

        })
    }

    fun closeRoom() {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
        val httpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val client = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .baseUrl(getString(R.string.backendUrl))
            .build()
            .create(RoomClient::class.java)
        client.deleteRoom(roomId).enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                if (response.code() == 200) {
                    Toast.makeText(applicationContext, "Room closed", Toast.LENGTH_SHORT).show()
                    goToRoom()
                } else {
                    Toast.makeText(applicationContext, "could not close Room", Toast.LENGTH_SHORT)
                        .show()
                    goToRoom()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    "Something went wrong with the API Call",
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }

    fun submitChanges() {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
        val httpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val client = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .baseUrl(getString(R.string.backendUrl))
            .build()
            .create(RoomClient::class.java)
        updatedRoom.topic = newTopicInputField.text.toString()
        updatedRoom.description = newDescriptionInputField.text.toString()
        print(updatedRoom)
        client.updateRoom(updatedRoom).enqueue(object : Callback<Boolean> {
            override fun onResponse(
                call: Call<Boolean>,
                response: Response<Boolean>
            ) {
                if (response.code() == 200) {
                    Toast.makeText(applicationContext, "changes applied", Toast.LENGTH_SHORT).show()
                    goToRoom()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "could not apply changes",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    "Something went wrong with the API Call",
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }


    fun showRoom() {
        runOnUiThread {
            // Stuff that updates the UI
            roomSettingsLayout.visibility = View.VISIBLE
        }
    }
    // go back to home Activity
    fun goToRoom() {
        var intent = Intent(this, RoomActivity::class.java)
        startActivity(intent)
    }
}