package com.dhbw.brainstorm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RoomActivity : AppCompatActivity() {

    private val item_list = generateDummyList()
    private val adapter_value = MyAdapter(item_list)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)
        findViewById<RecyclerView>(R.id.contributionList).adapter = adapter_value

        findViewById<View>(R.id.addContribution).setOnClickListener{
            addContribution()
        }

    }


    // generate dummy data
    private fun generateDummyList(): ArrayList<ContributionContent>{
        val tmpContributionList = ArrayList<ContributionContent>()
        for (i in 0 until 5){
            tmpContributionList.add(ContributionContent("Beitrag " + i.toString(), 1))
        }
        return tmpContributionList
    }

    // adding new contribution to the recyclerlist
    private fun addContribution() {
        val newContribution = ContributionContent("the new item", 0)
        item_list.add(item_list.count(), newContribution)
        adapter_value.notifyItemInserted(item_list.count())
    }

    fun confirmNewContribution(view: View){
        // get the text from the EditText
        val editTextView = findViewById<EditText>(R.id.editTextNewContribution)
        val newContent = editTextView.text.toString();

        // remove the item with the EditText from the list
        item_list.removeAt(item_list.count() - 1)
        adapter_value.notifyItemRemoved(item_list.count() -1 )

        // add new Contribution with the text from the EditText
        val newContribution = ContributionContent(newContent, 1)
        item_list.add(item_list.count(), newContribution)
        adapter_value.notifyItemInserted(item_list.count())
    }
}

data class ContributionContent(val textContent: String, val viewType: Int)

class MyAdapter(private val data_list: List<ContributionContent>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    // different types of layout for the single items
    private final val ITEM_TYPE_ADD = 0; // adding new contribution -> EditText
    private final val ITEM_TYPE_CONTRIBUTION = 1; // normal contribution with content -> TextView

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == 0)
        {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_new_contribution, parent, false)
            return AddContributionViewHolder(itemView)
        }
        else
        {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_contribution_card, parent, false)
            return ContributionViewHolder(itemView)
        }

    }

    override fun getItemCount() = data_list.count()

    override fun getItemViewType(position: Int): Int {
        if(data_list[position].viewType == 0)
        {
            return ITEM_TYPE_ADD
        }
        else
        {
            return ITEM_TYPE_CONTRIBUTION
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = data_list[position]
        val currentViewType = getItemViewType(position)
        when(currentViewType){
            0 -> {
                val addContrViewHolder: AddContributionViewHolder = holder as AddContributionViewHolder;
            }
            1 ->{
                val contrViewHolder: ContributionViewHolder = holder as ContributionViewHolder;
                contrViewHolder.textViewContent.text = currentItem.textContent;
            }
        }
    }

    class ContributionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewContent: TextView = itemView.findViewById(R.id.contributionTextView)
    }
    class AddContributionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val editTextView: EditText = itemView.findViewById(R.id.editTextNewContribution)
    }
}