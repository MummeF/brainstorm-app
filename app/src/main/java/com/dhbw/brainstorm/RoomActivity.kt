package com.dhbw.brainstorm

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dhbw.brainstorm.adapter.ContributionsAdapter
import com.dhbw.brainstorm.api.CommonClient
import com.dhbw.brainstorm.api.ContributionClient
import com.dhbw.brainstorm.api.RoomClient
import com.dhbw.brainstorm.api.model.Room
import com.dhbw.brainstorm.api.model.RoomState
import com.dhbw.brainstorm.helper.ROOM_ID_INTENT
import com.dhbw.brainstorm.helper.SharedPrefHelper
import com.dhbw.brainstorm.websocket.model.ReceiveMessage
import com.dhbw.brainstorm.websocket.model.SubscribeMessage
import com.gmail.bishoybasily.stomp.lib.Event
import com.gmail.bishoybasily.stomp.lib.StompClient
import com.google.gson.Gson
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_room.*
import kotlinx.android.synthetic.main.dialog_add_contribution.*
import kotlinx.android.synthetic.main.dialog_request_mod_rights.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
//import kotlinx.android.synthetic.main.dialog_add_contribution.*
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
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
    private var isModerator: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        // assing the room ID and validate it
        roomId = intent.getIntExtra(ROOM_ID_INTENT, -1)
        validateRoomId(roomId)
        validateModeratorId(SharedPrefHelper.getModeratorId(this))
        adapter = ContributionsAdapter(applicationContext, this)
        contributionList.adapter = adapter

        // switch to next state of the room
        nextBtn.setOnClickListener {
            upgradeRoomState()
        }

        // add new Contribution
        addContribution.setOnClickListener {
            dialogAddNewContribution()
        }

        // add new comment
        //newCommentBtn.setOnClickListener{
        //   dialogAddNewComment()
        //}

    }


    // dialogs for requesting moderator rights and adding new data (contributions/comments)
    fun dialogRequestModeratorRights() {
        val dialog = Dialog(this)

        dialog.setContentView(R.layout.dialog_request_mod_rights)
        dialog.submitBtnReqModIdDialog.setOnClickListener {
            val editText = dialog.editTextModeratorPassword

            val contentNewContribution = editText.text.toString()
            validateModeratorPassword(contentNewContribution, dialog)
        }
        dialog.show()
    }

    fun upgradeRoomState() {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
        val httpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val client = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .baseUrl(getString(R.string.backendUrl))
            .build()
            .create(RoomClient::class.java)
        client.increaseRoomState(roomId).enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                if (response.code() == 200) {
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Something went wrong.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    "Something went wrong.",
                    Toast.LENGTH_LONG
                ).show()
            }

        })

    }

    // initialize menu bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_create, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.itemShareCreate -> {
                val i = Intent(Intent.ACTION_SEND)
                i.setType("text/plain")
                i.putExtra(Intent.EXTRA_TEXT, getString(R.string.backendUrl) + "/room/" + roomId)
                i.putExtra(Intent.EXTRA_SUBJECT, "Check out this Brainstorm room")
                startActivity(Intent.createChooser(i, "Share link to room via"))
            }
            R.id.itemRequestModerator -> {
                dialogRequestModeratorRights()
            }
            R.id.itemEditRoomSettings -> {
                val intent =
                    Intent(this, RoomSettings::class.java)
                intent.putExtra(ROOM_ID_INTENT, roomId)
                startActivity(intent)
            }
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val reqModItem = menu.findItem(R.id.itemRequestModerator)
        val settingsItem = menu.findItem(R.id.itemEditRoomSettings)
        // reqModItem.setVisible(!isModerator)
        // settingsItem.setVisible(isModerator)
        return true
    }

    fun dialogAddNewContribution() {
        val dialog = Dialog(this)

        dialog.setContentView(R.layout.dialog_add_contribution)
        dialog.submitBtnNewCommentDialog.setOnClickListener {
            val editText = dialog.editTextNewCommentDialog

            val contentNewContribution = editText.text.toString()
            dialog.dismiss()

            addNewContribution(roomId, contentNewContribution)
        }
        dialog.show()

    }

    private fun addNewContribution(roomId: Int, content: String) {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
        val httpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val client = Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(httpClient)
            .baseUrl(getString(R.string.backendUrl))
            .build()
            .create(ContributionClient::class.java)
        var requestBody: RequestBody =
            content.toRequestBody("text/plain".toMediaTypeOrNull())
        client.addContribution(roomId, requestBody).enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {

                if (response.code() != 200) {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.somethingWentWrongLabel),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.somethingWentWrongLabel),
                    Toast.LENGTH_LONG
                ).show()
            }

        })
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
                        getString(R.string.somethingWentWrongLabel),
                        Toast.LENGTH_LONG
                    ).show()
                    goToHome()
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    applicationContext,
                    getString(R.string.somethingWentWrongLabel),
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
                        getString(R.string.somethingWentWrongLabel),
                        Toast.LENGTH_LONG
                    ).show()
                    goToHome()
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.somethingWentWrongLabel),
                    Toast.LENGTH_LONG
                ).show()
                goToHome()
            }

        })

    }

    fun validateModeratorPassword(password: String, dialog: Dialog) {

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
        val httpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val client = Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(httpClient)
            .baseUrl(getString(R.string.backendUrl))
            .build()
            .create(RoomClient::class.java)
        client.validateModeratorPassword(roomId, password)
            .enqueue(object : Callback<Boolean> {
                override fun onResponse(
                    call: Call<Boolean>,
                    response: Response<Boolean>
                ) {
                    if (response.code() == 200) {
                        if (response.body()!!) {
                            setModeratorId()
                            dialog.dismiss();
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Moderator ID was not right",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Something went wrong",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    Toast.makeText(
                        applicationContext,
                        "Something went wrong",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    fun setModeratorId() {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
        val httpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val client = Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(httpClient)
            .baseUrl(getString(R.string.backendUrl))
            .build()
            .create(RoomClient::class.java)
        val moderatorId = SharedPrefHelper.getModeratorId(this)
        client.setModeratorId(roomId, moderatorId)
            .enqueue(object : Callback<String> {
                override fun onResponse(
                    call: Call<String>,
                    response: Response<String>
                ) {
                    if (response.code() == 200) {

                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Something went wrong with setting the moderatod ID",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Toast.makeText(
                        applicationContext,
                        "Something went wrong",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun validateModeratorId(modId: String) {

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
        val httpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val client = Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(httpClient)
            .baseUrl(getString(R.string.backendUrl))
            .build()
            .create(RoomClient::class.java)
        client.validateModeratorId(roomId, modId)
            .enqueue(object : Callback<Boolean> {
                override fun onResponse(
                    call: Call<Boolean>,
                    response: Response<Boolean>
                ) {

                    if (response.code() == 200) {
                        isModerator = response.body()!!
                        if (isModerator) {
                            runOnUiThread {
                                nextBtn.visibility = View.VISIBLE

                            }

                            Toast.makeText(
                                applicationContext,
                                "Moderator ID was right",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            runOnUiThread {
                                nextBtn.visibility = View.GONE
                            }
                            Toast.makeText(
                                applicationContext,
                                "Incorrect moderator password!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Something went wrong",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    Toast.makeText(
                        applicationContext,
                        "Something went wrong",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })
    }

    // handles the websocket connection
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
                                "isAlive" -> println("Heartbeat!")
                                "data" -> {
                                    var room =
                                        Gson().fromJson(message.content, Room::class.java)
                                    runOnUiThread {
                                        if (adapter.room.state != room.state) {
                                            adapter = ContributionsAdapter(applicationContext, this)
                                            contributionList.adapter = adapter

                                            // TODO if room.state == RoomState.Done -> Intent auf ResultActivity
                                            if (room.state.equals(RoomState.DONE)) {
                                                val intent =
                                                    Intent(this, ResultActivity::class.java)
                                                intent.putExtra(ROOM_ID_INTENT, roomId)
                                                startActivity(intent)
                                                // TODO: soll funktionieren
                                                unsubScribeAndDisconnect()
                                            }
                                        }
//                                        adapter = ContributionsAdapter(applicationContext, this)
//                                        contributionList.adapter = adapter
                                        adapter.update(room)

                                        roomHeadline.text = room.topic
                                        roomDescription.text = room.description
                                        when (room.state) {
                                            RoomState.CREATE -> roomPhaseText.text = "Create-Phase"
                                            RoomState.EDIT -> roomPhaseText.text = "Edit-Phase"
                                            RoomState.DONE -> roomPhaseText.text = "Done-Phase"
                                        }
                                    }
                                }
                                "mod-update" -> {
                                    //TODO: Update Mod
                                    validateModeratorId(SharedPrefHelper.getModeratorId(this))
                                }
                                "delete" -> {
                                    runOnUiThread {
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
                                    getString(R.string.somethingWentWrongLabel),
                                    Toast.LENGTH_LONG
                                ).show()
                                goToHome()
                            }
                        })

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
                                getString(R.string.somethingWentWrongLabel),
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
    fun unsubScribeAndDisconnect() {
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
                    getString(R.string.somethingWentWrongLabel),
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

    // go back to home Activity
    fun goToHome() {
        var intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}

