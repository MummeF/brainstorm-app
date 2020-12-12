package com.dhbw.brainstorm

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dhbw.brainstorm.adapter.RoomsAdapter
import com.dhbw.brainstorm.api.CommonClient
import com.dhbw.brainstorm.api.RoomClient
import com.dhbw.brainstorm.api.model.Room
import com.dhbw.brainstorm.api.model.RoomState
import com.dhbw.brainstorm.helper.FAVORITE_SHARED_PREF
import com.dhbw.brainstorm.helper.SharedPrefHelper
import kotlinx.android.synthetic.main.activity_favorite.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FavoriteActivity : AppCompatActivity() {
    var favRoomList: MutableList<Room> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)

        val sharedPref = getSharedPreferences(FAVORITE_SHARED_PREF, MODE_PRIVATE)


        checkRoomList(SharedPrefHelper.getFavorites(this))
    }


    fun checkRoomList(rooms: List<Room>){
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
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
                    var backendRooms: MutableList<Room> = response.body()!!
                    rooms.forEach{ room ->
                        if(backendRooms.none { backendRoom ->
                                backendRoom.id == room.id
                            }) {
                            isRoomStateDone(room)
                        }
                        else{
                            favRoomList.add(room)
                        }
                    }
                    setFavRoomList(this@FavoriteActivity)

                } else {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.somethingWentWrongLabel),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }


            override fun onFailure(call: Call<MutableList<Room>>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    applicationContext,
                    getString(R.string.somethingWentWrongLabel),
                    Toast.LENGTH_LONG
                ).show()
            }

        })
    }

    fun isRoomStateDone(room: Room) {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
        val httpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val client = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .baseUrl(getString(R.string.backendUrl))
            .build()
            .create(RoomClient::class.java)
        client.getRoom(room.id).enqueue(object : Callback<Room> {
            override fun onResponse(
                call: Call<Room>,
                response: Response<Room>
            ) {

                if (response.code() == 200) {
                    val returnedRoom: Room = response.body()!!
                    if(returnedRoom.state == RoomState.DONE){
                        favRoomList.add(room)
                    }
                    else{
                        SharedPrefHelper.removeFavorite(this@FavoriteActivity , room.id)
                    }
                } else {
                    SharedPrefHelper.removeFavorite(this@FavoriteActivity , room.id)
                }
            }

            override fun onFailure(call: Call<Room>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    applicationContext,
                    getString(R.string.somethingWentWrongLabel),
                    Toast.LENGTH_LONG
                ).show()
            }

        })
    }

    fun setFavRoomList(context: Context){
        roomList.adapter = RoomsAdapter(favRoomList, context)
        roomList.layoutManager = LinearLayoutManager(applicationContext)
    }
}