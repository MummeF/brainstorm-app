package com.dhbw.brainstorm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.dhbw.brainstorm.adapter.ContributionsAdapter
import com.dhbw.brainstorm.api.RoomClient
import com.dhbw.brainstorm.api.model.Room
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_room.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class  ResultActivity : AppCompatActivity() {
    private var roomId = -1
    private lateinit var adapter: ContributionsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        adapter = ContributionsAdapter(applicationContext, this)
        contributionList.adapter = adapter
        roomId = intent.getIntExtra("roomId", -1)
        getRoom()
    }

    // initialize menu bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_result, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.itemShareResult -> {
                val i = Intent(Intent.ACTION_SEND)
                i.type = "text/plain"
                i.putExtra(Intent.EXTRA_TEXT, getString(R.string.frontendUrl) + "/result/" + roomId)
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.checkoutThisBrainstormRoomLabel))
                startActivity(Intent.createChooser(i, getString(R.string.shareLinkToRoomVia)))
            }
        }
        return true
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
        client.getHistoryRoom(roomId).enqueue(object : Callback<Room> {
            override fun onResponse(
                call: Call<Room>,
                response: Response<Room>
            ) {

                if (response.code() == 200) {
                    var room = response.body()

                    if (room != null) {
                        showRoom()
                        runOnUiThread{
                            adapter.update(room, false)
                            roomHeadline.text = room.topic
                            roomDescription.text = room.description
                        }
                    } else {
                        Toast.makeText(applicationContext, getString(R.string.noRoomFoundLabel), Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {

                }
            }

            override fun onFailure(call: Call<Room>, t: Throwable) {

            }

        })
    }

    fun showRoom() {
        runOnUiThread {
            roomLayout.visibility = View.VISIBLE
            roomProgress.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        goToHome()
    }
    fun goToHome() {
        var intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun hideRoom() {
        runOnUiThread {
            roomLayout.visibility = View.GONE
            roomProgress.visibility = View.VISIBLE
        }
    }
}