package com.dhbw.brainstorm.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.dhbw.brainstorm.R
import com.dhbw.brainstorm.RoomActivity
import com.dhbw.brainstorm.api.model.Room

class RoomsAdapter(
    private var rooms: MutableList<Room>, var context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_room_card, parent, false)
        return RoomViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = rooms[position]
        val roomViewHolder: RoomViewHolder = holder as RoomViewHolder;
        roomViewHolder.buttonContent.text = currentItem.topic
        roomViewHolder.buttonContent.setOnClickListener {
            val intent = Intent(context, RoomActivity::class.java)
            intent.putExtra("roomId", currentItem.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = rooms.count()


    class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var buttonContent:Button = itemView.findViewById(R.id.roomButton)
    }
}
