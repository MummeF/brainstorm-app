package com.dhbw.brainstorm

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.dhbw.brainstorm.adapter.ContributionsAdapter
import com.dhbw.brainstorm.api.CommonClient
import com.dhbw.brainstorm.api.model.Room
import com.dhbw.brainstorm.websocket.model.ReceiveMessage
import com.dhbw.brainstorm.websocket.model.SubscribeMessage
import com.gmail.bishoybasily.stomp.lib.Event
import com.gmail.bishoybasily.stomp.lib.StompClient
import com.google.gson.Gson
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
    private lateinit var stompConnection: Disposable
    private lateinit var stomp: StompClient
    private lateinit var topic: Disposable
    private var roomId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)
        roomId = intent.getIntExtra("roomId", -1)
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
                    if (response.body()!!) {
                        // TODO: Passworteingabe und PW prüfen
                        fetchRoom(roomId)
                    } else {
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

        val url = getString(R.string.backendWebSocketUrl)
        val intervalMillis = 5000L
        val client = OkHttpClient.Builder()
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .build()

        stomp = StompClient(client, intervalMillis).apply { this@apply.url = url }

        // connect
        stompConnection = stomp.connect().subscribe {
            when (it.type) {
                Event.Type.OPENED -> {

                    // subscribe
                    topic = stomp.join(getString(R.string.wsTopic))
                        .subscribe({
                            var message = Gson().fromJson(it, ReceiveMessage::class.java)
                            when (message.type) {
                                "isAlive" -> println("Hearbeat!")
                                "data" -> {
                                    var room =
                                        Gson().fromJson(message.content, Room::class.java)
                                    runOnUiThread {
                                        adapter.update(room.contributions, room.state)
                                    }
                                }
                                "mod-update" -> {
                                    //TODO: Update Mod
                                }
                                "delete" -> {
                                    runOnUiThread{
                                        Toast.makeText(
                                            applicationContext,
                                            "Room was deleted by the moderator.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        goToHome()
                                    }
                                }
                            }
                        }, { error ->
                            error.printStackTrace()
                            runOnUiThread {
                                Toast.makeText(
                                    applicationContext,
                                    "Something went wrong. Please try again or come back later.",
                                    Toast.LENGTH_LONG
                                ).show()
                                goToHome()
                            }
                        })

                    println("subscribed")
                    stomp.send(
                        getString(R.string.wsSub),
                        Gson().toJson(SubscribeMessage(roomId))
                    ).subscribe({
                        if (it) {
                            println("Successfully Subscribed!")
                            showRoom()
                        }
                    }, { error ->
                        runOnUiThread {
                            error.printStackTrace()
                            Toast.makeText(
                                applicationContext,
                                "Something went wrong. Please try again or come back later.",
                                Toast.LENGTH_LONG
                            ).show()
                            goToHome()
                        }
                    })


                }
                Event.Type.CLOSED -> {
                    unsubScribeAndDisconnect()
                }
                Event.Type.ERROR -> {
                    unsubScribeAndDisconnect()
                }
            }
        }


    }

    override fun onPause() {
        if (stompConnection != null) {
            unsubScribeAndDisconnect()
        }
        hideRoom()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        fetchRoom(roomId)
    }

    fun showRoom() {

        runOnUiThread {
            // Stuff that updates the UI
            roomLayout.visibility = View.VISIBLE
            roomProgress.visibility = View.GONE
        }


    }

    @SuppressLint("CheckResult")
    fun unsubScribeAndDisconnect(){
        stomp.send(
            getString(R.string.wsUnSub),
            Gson().toJson(SubscribeMessage(roomId))
        ).subscribe({
            topic.dispose()
            stompConnection.dispose()
        }, { error ->
            runOnUiThread {
                error.printStackTrace()
                Toast.makeText(
                    applicationContext,
                    "Something went wrong. Please try again or come back later.",
                    Toast.LENGTH_LONG
                ).show()
                goToHome()
            }
        })
    }

    fun hideRoom() {
        runOnUiThread {
            roomLayout.visibility = View.GONE
            roomProgress.visibility = View.VISIBLE
        }
    }

    fun goToHome() {
        var intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}

