package com.dhbw.brainstorm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_join.*
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.NumberFormatException

class JoinActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)

        roomBtn.setOnClickListener {
            val intent = Intent(this, RoomActivity::class.java)
            var roomId = roomIdText.text.toString();
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
}