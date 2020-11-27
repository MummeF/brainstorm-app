package com.dhbw.brainstorm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RoomActivity : AppCompatActivity() {

    private val item_list = generateDummyList()
    private val adapter_value = MyAdapter(item_list)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)
        findViewById<RecyclerView>(R.id.contributionList).adapter = adapter_value

        findViewById<Button>(R.id.addContribution).setOnClickListener{
            addContribution()
        }
    }


    // generate dummy data
    private fun generateDummyList(): ArrayList<ContributionContent>{
        val tmpContributionList = ArrayList<ContributionContent>()
        for (i in 0 until 5){
            tmpContributionList.add(ContributionContent("Beitrag " + i.toString()))
        }
        return tmpContributionList
    }

    // adding new contribution to the recyclerlist
    private fun addContribution() {
        val newContribution = ContributionContent("the new item")
        item_list.add(item_list.count(), newContribution)
        adapter_value.notifyItemInserted(item_list.count())
    }
}

data class ContributionContent(val textContent: String)

class MyAdapter(private val data_list: List<ContributionContent>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_contribution_card, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount() = data_list.count()

    override fun onBindViewHolder(holder: MyAdapter.MyViewHolder, position: Int) {
        val currentItem = data_list[position]
        holder.textViewContent.text = currentItem.textContent
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewContent: TextView = itemView.findViewById(R.id.contributionTextView)
    }
}