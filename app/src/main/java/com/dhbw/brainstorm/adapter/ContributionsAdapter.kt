package com.dhbw.brainstorm.adapter

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dhbw.brainstorm.R
import com.dhbw.brainstorm.api.CommentClient
import com.dhbw.brainstorm.api.ContributionClient
import com.dhbw.brainstorm.api.model.Contribution
import com.dhbw.brainstorm.api.model.Room
import com.dhbw.brainstorm.api.model.RoomState
import com.dhbw.brainstorm.helper.SharedPrefHelper
import kotlinx.android.synthetic.main.dialog_add_contribution.*
import kotlinx.android.synthetic.main.item_comment_edit.view.contributionVotes
import kotlinx.android.synthetic.main.item_contribution_create.view.contentTextView
import kotlinx.android.synthetic.main.item_contribution_edit.view.*
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

class ContributionsAdapter(private var context: Context, private var activity: Activity) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    var room = Room(ArrayList(), "", -1, "", false, RoomState.CREATE, "")

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
            RoomState.DONE -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_contribution_done, parent, false)
                return ContributionViewHolder(itemView)
            }
        }
    }

    override fun getItemCount() = room.contributions.count()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ContributionViewHolder).initContribution(
            room.id,
            room.contributions[position],
            room.state,
            context,
            activity
        )
    }

    fun update(room: Room) {

        this.room = room
        notifyDataSetChanged()
    }


    class ContributionViewHolder(val item: View) : RecyclerView.ViewHolder(item) {
        fun initContribution(
            roomId: Int,
            contribution: Contribution,
            roomState: RoomState,
            context: Context,
            activity: Activity
        ) {

            item.contentTextView.text = contribution.content;
            when (roomState) {
                RoomState.CREATE -> {
                }
                RoomState.EDIT -> {
                    item.contributionVotes.text = contribution.reputation.toString()
                    item.commentList.adapter =
                        CommentAdapter(
                            contribution.comments,
                            roomState,
                            roomId,
                            contribution.id,
                            context,
                            activity
                        )
                    item.commentList.layoutManager = LinearLayoutManager(context)
                    item.addCommentButton.setOnClickListener {

                        val dialog = Dialog(activity)
                        dialog.setContentView(
                            activity.layoutInflater.inflate(
                                R.layout.dialog_add_comment,
                                null
                            )
                        )
                        dialog.submitBtnNewCommentDialog.setOnClickListener {
                            val editText = dialog.editTextNewCommentDialog

                            val contentNewComment = editText.text.toString()
                            dialog.dismiss()

                            addComment(roomId, contribution.id, contentNewComment, context)
                        }
                        dialog.show()

                    }

                    when (SharedPrefHelper.contributionVoted(
                        activity,
                        roomId,
                        contribution.id,
                    )) {
                        0 -> item.downVoteContribution.setColorFilter(Color.RED)
                        1 -> item.upVoteContribution.setColorFilter(Color.GREEN)
                    }


                    item.upVoteContribution.setOnClickListener {
                        if (SharedPrefHelper.contributionVoted(
                                activity,
                                roomId,
                                contribution.id,
                            ) == -1
                        ) {
                            voteContribution(roomId, contribution.id, true, context, activity)
                            item.upVoteContribution.setColorFilter(Color.GREEN)
                        }
                    }
                    item.downVoteContribution.setOnClickListener {
                        if (SharedPrefHelper.contributionVoted(
                                activity,
                                roomId,
                                contribution.id,
                            ) == -1
                        ) {
                            voteContribution(roomId, contribution.id, false, context, activity)
                            item.downVoteContribution.setColorFilter(Color.RED)
                        }
                    }
                }
                RoomState.DONE -> {
                    item.contributionVotes.text = contribution.reputation.toString()
                    item.commentList.adapter =
                        CommentAdapter(
                            contribution.comments,
                            roomState,
                            roomId,
                            contribution.id,
                            context,
                            activity
                        )
                    item.commentList.layoutManager = LinearLayoutManager(context)

                }
            }
        }

        private fun addComment(
            roomId: Int,
            contributionId: Int,
            content: String,
            context: Context
        ) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BASIC
            val httpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
            val client = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .baseUrl(context.getString(R.string.backendUrl))
                .build()
                .create(CommentClient::class.java)
            var requestBody: RequestBody =
                content.toRequestBody("text/plain".toMediaTypeOrNull())
            client.addComment(roomId, contributionId, requestBody)
                .enqueue(object : Callback<String> {
                    override fun onResponse(
                        call: Call<String>,
                        response: Response<String>
                    ) {

                        if (response.code() != 200) {
                            println("Error")
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        println("Error")
                    }

                })
        }


        private fun voteContribution(
            roomId: Int,
            contributionId: Int,
            voteUp: Boolean,
            context: Context,
            activity: Activity
        ) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BASIC
            val httpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
            val client = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .baseUrl(context.getString(R.string.backendUrl))
                .build()
                .create(ContributionClient::class.java)
            var call: Call<String>
            if (voteUp) {
                call = client.voteContributionUp(roomId, contributionId)
            } else {
                call = client.voteContributionDown(roomId, contributionId)
            }

            call.enqueue(object : Callback<String> {
                override fun onResponse(
                    call: Call<String>,
                    response: Response<String>
                ) {

                    if (response.code() != 200) {
                        println("Error")
                    } else {
                        SharedPrefHelper.voteContribution(
                            activity,
                            roomId,
                            contributionId,
                            voteUp
                        )
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    println("Error")
                }

            })

        }
    }
}