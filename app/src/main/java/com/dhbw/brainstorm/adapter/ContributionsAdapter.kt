package com.dhbw.brainstorm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dhbw.brainstorm.R
import com.dhbw.brainstorm.api.model.Contribution
import com.dhbw.brainstorm.api.model.Room
import com.dhbw.brainstorm.api.model.RoomState
import okhttp3.internal.notifyAll


class ContributionsAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    public var room = Room(ArrayList(), "", -1, "", false, RoomState.CREATE, "")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when (room.state) {
            RoomState.CREATE -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_contribution_create, parent, false)
                return ContributionViewHolder(itemView)
            }
            RoomState.EDIT -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_contribution_edit, parent, false)
                return ContributionViewHolder(itemView)
            }
        }

        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contribution_create, parent, false)
        return ContributionViewHolder(itemView)

    }

    override fun getItemCount() = room.contributions.count()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = room.contributions[position]
        when (room.state) {
            RoomState.EDIT -> {
                val contrViewHolder: ContributionViewHolder = holder as ContributionViewHolder;
                contrViewHolder.textViewContent.text = currentItem.content;
            }
            RoomState.CREATE -> {
                val contrViewHolder: ContributionViewHolder = holder as ContributionViewHolder;
                contrViewHolder.textViewContent.text = currentItem.content;
            }
            RoomState.DONE -> {
                val contrViewHolder: ContributionViewHolder = holder as ContributionViewHolder;
                contrViewHolder.textViewContent.text = currentItem.content;
            }
        }
    }

    fun update(room: Room) {

        this.room = room
        notifyDataSetChanged()
    }


    class ContributionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewContent: TextView = itemView.findViewById(R.id.contentTextView)
    }
}