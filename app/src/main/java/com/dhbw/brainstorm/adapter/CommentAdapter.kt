package com.dhbw.brainstorm.adapter

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dhbw.brainstorm.R
import com.dhbw.brainstorm.api.CommentClient
import com.dhbw.brainstorm.api.model.Comment
import com.dhbw.brainstorm.api.model.RoomState
import com.dhbw.brainstorm.helper.SharedPrefHelper
import kotlinx.android.synthetic.main.item_comment_edit.view.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CommentAdapter(
    private var comments: List<Comment>,
    var roomState: RoomState,
    var roomId: Int,
    var contributionId: Int,
    var context: Context,
    var activity: Activity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (roomState) {
            RoomState.EDIT -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_comment_edit, parent, false)
                return CommentViewHolder(itemView)
            }
            RoomState.DONE -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_comment_done, parent, false)
                return CommentViewHolder(itemView)

            }
            else -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_comment_done, parent, false)
                return CommentViewHolder(itemView)

            }
        }

    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as CommentViewHolder).initComment(
            comments[position],
            roomId,
            contributionId,
            roomState,
            context,
            activity
        )
    }

    override fun getItemCount() = comments.count()


    class CommentViewHolder(val item: View) : RecyclerView.ViewHolder(item) {
        fun initComment(
            comment: Comment,
            roomId: Int,
            contributionId: Int,
            roomState: RoomState,
            context: Context,
            activity: Activity
        ) {
            item.contentTextView.text = comment.content
            item.contributionVotes.text = comment.reputation.toString()
            when (roomState) {
                RoomState.EDIT -> {
                    when (SharedPrefHelper.commentVoted(
                        activity,
                        roomId,
                        contributionId,
                        comment.id
                    )) {
                        0 -> item.downVoteComment.setColorFilter(Color.RED)
                        1 -> item.upVoteComment.setColorFilter(Color.GREEN)
                    }


                    item.upVoteComment.setOnClickListener {
                        if (SharedPrefHelper.commentVoted(
                                activity,
                                roomId,
                                contributionId,
                                comment.id
                            ) == -1
                        ) {
                            voteComment(roomId, contributionId, comment.id, true, context, activity)
                            item.upVoteComment.setColorFilter(Color.GREEN)
                        }
                    }
                    item.downVoteComment.setOnClickListener {
                        if (SharedPrefHelper.commentVoted(
                                activity,
                                roomId,
                                contributionId,
                                comment.id
                            ) == -1
                        ) {
                            voteComment(
                                roomId,
                                contributionId,
                                comment.id,
                                false,
                                context,
                                activity
                            )
                            item.downVoteComment.setColorFilter(Color.RED)
                        }
                    }
                }
                RoomState.DONE -> {
                    //TODO: Icons
                }
                else -> {
                    println("Fehler!")
                }
            }
        }

        private fun voteComment(
            roomId: Int,
            contributionId: Int,
            commentId: Int,
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
                .create(CommentClient::class.java)
            var call: Call<String>
            if (voteUp) {
                call = client.voteCommentUp(roomId, contributionId, commentId)
            } else {
                call = client.voteCommentDown(roomId, contributionId, commentId)
            }

            call.enqueue(object : Callback<String> {
                override fun onResponse(
                    call: Call<String>,
                    response: Response<String>
                ) {

                    if (response.code() != 200) {
                        println("Error")
                    } else {
                        SharedPrefHelper.voteComment(
                            activity,
                            roomId,
                            contributionId,
                            commentId,
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

