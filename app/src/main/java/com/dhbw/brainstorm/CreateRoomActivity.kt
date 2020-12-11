package com.dhbw.brainstorm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import com.dhbw.brainstorm.api.CommonClient
import com.dhbw.brainstorm.helper.SharedPrefHelper
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_create_room.*
import kotlinx.android.synthetic.main.activity_join.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.createRoomButton
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
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
import java.lang.NumberFormatException
import java.util.concurrent.TimeUnit

class CreateRoomActivity : AppCompatActivity() {

    var roomPasswordVisible: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_room)
        toggleRoomPasswordVisibility()
        publicRoomSwitch.setOnCheckedChangeListener { _, _ ->
            toggleRoomPasswordVisibility()
        }

        createRoomButton.setOnClickListener { onCreateRoomButtonClick() }
    }



    private fun toggleRoomPasswordVisibility(){
        if(roomPasswordHeader.visibility == View.VISIBLE){
            roomPasswordHeader.visibility = View.GONE
            roomPasswordDescription.visibility = View.GONE
            roomPasswordInputFieldBackground.visibility = View.GONE
            roomPasswordInputField.visibility = View.GONE
            roomPasswordVisible = false
            toggleCreateButtonConstraint()
        }

        else{
            roomPasswordHeader.visibility = View.VISIBLE
            roomPasswordDescription.visibility = View.VISIBLE
            roomPasswordInputFieldBackground.visibility = View.VISIBLE
            roomPasswordInputField.visibility = View.VISIBLE
            roomPasswordVisible = true
            toggleCreateButtonConstraint()
        }
    }

    private fun toggleCreateButtonConstraint(){
        val params = createRoomButton.layoutParams as ConstraintLayout.LayoutParams
        if(roomPasswordVisible){
            params.topToBottom = roomPasswordInputFieldBackground.id
        }
        else{
            params.topToBottom = publicRoomSwitch.id
        }
        createRoomButton.requestLayout()
    }

    fun onCreateRoomButtonClick() {
        var topic: String = topicInputField.text.toString()
        var description: String = descriptionInputField.text.toString()
        var moderatorPassword: String = moderatorPasswordInputField.text.toString()
        var roomPassword: String = roomPasswordInputField.text.toString()
        var isRoomPublic: Boolean = roomPasswordVisible

        if(topic != "" && description != "" && moderatorPassword!= ""){
            createRoom(description, isRoomPublic, SharedPrefHelper.getModeratorId(this), topic, roomPassword, moderatorPassword)
        }
    }

    fun createRoom(description: String, isPublic: Boolean, moderatorId: String, topic: String, roomPassword: String, moderatorPassword: String) {
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
        client.createRoom(description, isPublic, moderatorId, topic).enqueue(object : Callback<Int> {
            override fun onResponse(
                call: Call<Int>,
                response: Response<Int>
            ) {

                if (response.code() == 200) {
                    val roomId: Int = response.body()?:"".toInt()
                    setModeratorPassword(roomId, moderatorPassword)
                    if(roomPassword != "" && isPublic){
                        setRoomPassword(roomId, roomPassword)
                    }
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Something went wrong. Please try again or come back later.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<Int>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    applicationContext,
                    "Something went wrong. Please try again or come back later.",
                    Toast.LENGTH_LONG
                ).show()
            }

        })
    }

    fun setModeratorPassword(roomId: Int, moderatorPassword: String){
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
        val httpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val client = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .baseUrl(getString(R.string.backendUrl))
            .build()
            .create(CommonClient::class.java)

        var requestBody: RequestBody =
            moderatorPassword.toRequestBody("text/plain".toMediaTypeOrNull())
        client.setModeratorPassword(requestBody, roomId).enqueue(object : Callback<Boolean> {
            override fun onResponse(
                call: Call<Boolean>,
                response: Response<Boolean>
            ) {
                if (response.code() == 200) {
                    joinCreatedRoom(roomId)
                }
                else {
                    Toast.makeText(
                        applicationContext,
                        "Something went wrong. Please try again or come back later.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    applicationContext,
                    "Something went wrong. Please try again or come back later.",
                    Toast.LENGTH_LONG
                ).show()
            }

        })
    }

    fun setRoomPassword(roomId:Int, roomPassword: String){
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
        val httpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val client = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .baseUrl(getString(R.string.backendUrl))
            .build()
            .create(CommonClient::class.java)
        var requestBody: RequestBody =
            roomPassword.toRequestBody("text/plain".toMediaTypeOrNull())
        client.setRoomPassword(requestBody, roomId).enqueue(object : Callback<Boolean> {
            override fun onResponse(
                call: Call<Boolean>,
                response: Response<Boolean>
            ) {
                if (response.code() == 200) {
                    joinCreatedRoom(roomId)
                }
                else {
                    Toast.makeText(
                        applicationContext,
                        "Something went wrong. Please try again or come back later.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    applicationContext,
                    "Something went wrong. Please try again or come back later.",
                    Toast.LENGTH_LONG
                ).show()
            }

        })
    }
    override fun onBackPressed() {
        super.onBackPressed()
        goToHome()
    }
    // go back to home Activity
    fun goToHome() {
        var intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun joinCreatedRoom(roomId: Int){
        val intent = Intent(this, RoomActivity::class.java)
        intent.putExtra("roomId", roomId)
        startActivity(intent)
    }
}

