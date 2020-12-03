package com.dhbw.brainstorm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dhbw.brainstorm.R
import com.dhbw.brainstorm.api.model.Contribution
import com.dhbw.brainstorm.api.model.RoomState


class ContributionsAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var contributions: List<Contribution> = ArrayList<Contribution>();
    private var roomState: RoomState = RoomState.CREATE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when(roomState){
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

    override fun getItemCount() = contributions.count()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = contributions[position]
        when (roomState) {
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

    fun update(contributions: List<Contribution>, roomState: RoomState) {
        this.roomState = roomState
        this.contributions = contributions
        notifyDataSetChanged()
    }

    class ContributionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewContent: TextView = itemView.findViewById(R.id.contributionTextView)
    }
}