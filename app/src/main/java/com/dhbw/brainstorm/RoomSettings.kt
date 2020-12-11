package com.dhbw.brainstorm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.dhbw.brainstorm.api.RoomClient
import com.dhbw.brainstorm.api.model.Room
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
    val ROOM_ID_INTENT = "roomId"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_settings)
        roomId = intent.getIntExtra(ROOM_ID_INTENT, -1)
        getRoom()
    }
    fun getRoom(){
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
                    var room =response.body()
                    print("look here " + room)
                    if ((room != null) && (room is Room))
                    {
                        newTopicInputField.hint = room.topic
                        newDescriptionInputField.hint = room.description
                    } else{
                        Toast.makeText(applicationContext, "no room found", Toast.LENGTH_SHORT).show()
                    }
                } else {

                }
            }

            override fun onFailure(call: Call<Room>, t: Throwable) {

            }

        })
    }
}