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
                i.setType("text/plain")
                i.putExtra(Intent.EXTRA_TEXT, getString(R.string.backendUrl) + "/room/" + roomId)
                i.putExtra(Intent.EXTRA_SUBJECT, "Check out this Brainstorm room")
                startActivity(Intent.createChooser(i, "Share link to room via"))
            }
            R.id.itemPrintResult -> {
                Toast.makeText(applicationContext, "print result", Toast.LENGTH_SHORT).show()
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
                        adapter.update(room)
                    } else {
                        Toast.makeText(applicationContext, "no room found", Toast.LENGTH_SHORT)
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
            // Stuff that updates the UI
            roomLayout.visibility = View.VISIBLE
            roomProgress.visibility = View.GONE
        }


    }

    fun hideRoom() {
        runOnUiThread {
            roomLayout.visibility = View.GONE
            roomProgress.visibility = View.VISIBLE
        }
    }
}