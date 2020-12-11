package com.dhbw.brainstorm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.dhbw.brainstorm.adapter.RoomsAdapter
import com.dhbw.brainstorm.api.CommonClient
import com.dhbw.brainstorm.api.model.Room
import com.dhbw.brainstorm.api.model.RoomState
import kotlinx.android.synthetic.main.activity_join.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.NumberFormatException

class JoinActivity : AppCompatActivity(){
    private lateinit var adapter: RoomsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getRoomList()
        setContentView(R.layout.activity_join)

        joinRoomButton.setOnClickListener {
            val intent = Intent(this, RoomActivity::class.java)
            var roomId = roomIdEditText.text.toString();
            if (!roomId.equals("")) {
                try {
                    var roomIdAsInt = roomId.toInt()
                    intent.putExtra("roomId", roomIdAsInt)
                    startActivity(intent)
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Keine Zahl.", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    fun getRoomList(){
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val httpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val client = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .baseUrl(getString(R.string.backendUrl))
            .build()
            .create(CommonClient::class.java)
        client.getRoomList().enqueue(object : Callback<MutableList<Room>> {
            override fun onResponse(
                call: Call<MutableList<Room>>,
                response: Response<MutableList<Room>>
            ) {

                if (response.code() == 200) {
                    var rooms: MutableList<Room> = response.body()!!
                    var publicRooms: List<Room> = mutableListOf<Room>()

                    publicRooms = rooms.filter { room -> room.public && room.state != RoomState.DONE  }

                    adapter = RoomsAdapter(publicRooms, this@JoinActivity)
                    findViewById<RecyclerView>(R.id.roomList).adapter = adapter
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Something went wrong. Please try again or come back later.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }


            override fun onFailure(call: Call<MutableList<Room>>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    applicationContext,
                    "Something went wrong. Please try again or come back later.",
                    Toast.LENGTH_LONG
                ).show()
            }

        })
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        println("HIIIEIRRR")
    }


    public fun joinRoom(roomId: Int){
        val intent = Intent(this, RoomActivity::class.java)
            try {
                intent.putExtra("roomId", roomId)
                startActivity(intent)
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Der gewählte Raum ist momentan nicht erreichbar!", Toast.LENGTH_LONG).show()
            }
        }
}
