package com.dhbw.brainstorm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.dhbw.brainstorm.adapter.RoomsAdapter
import com.dhbw.brainstorm.helper.FAVORITE_SHARED_PREF
import com.dhbw.brainstorm.helper.SharedPrefHelper
import kotlinx.android.synthetic.main.activity_favorite.*

class FavoriteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)

        val sharedPref = getSharedPreferences(FAVORITE_SHARED_PREF, MODE_PRIVATE)

        roomList.adapter = RoomsAdapter(SharedPrefHelper.getFavorites(this), applicationContext)
        roomList.layoutManager = LinearLayoutManager(applicationContext)

    }
}