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
import kotlinx.android.synthetic.main.item_comment_edit.view.contentTextView
import kotlinx.android.synthetic.main.item_comment_edit.view.contributionVotes
import kotlinx.android.synthetic.main.item_contribution_done.view.*
import kotlinx.android.synthetic.main.item_contribution_edit.view.*
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
        var voting: Boolean = false

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
                        0 -> {
                            item.downVoteComment.setColorFilter(Color.RED)
                            item.upVoteComment.setColorFilter(R.color.buttonGrey)
                        }
                        1 -> {
                            item.upVoteComment.setColorFilter(Color.GREEN)
                            item.downVoteComment.setColorFilter(R.color.buttonGrey)
                        }
                        -1 ->{
                            item.upVoteComment.setColorFilter(R.color.buttonGrey)
                            item.downVoteComment.setColorFilter(R.color.buttonGrey)
                        }
                    }

                    item.upVoteComment.setOnClickListener {
                        if (SharedPrefHelper.commentVoted(
                                activity,
                                roomId,
                                contributionId,
                                comment.id
                            ) == -1 && !voting
                        ) {
                            voteComment(roomId, contributionId, comment.id, true, context, activity)
                            item.upVoteComment.setColorFilter(Color.GREEN)
                            item.downVoteComment.setColorFilter(R.color.buttonGrey)
                        }
                    }
                    item.downVoteComment.setOnClickListener {
                        if (SharedPrefHelper.commentVoted(
                                activity,
                                roomId,
                                contributionId,
                                comment.id
                            ) == -1 && !voting
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
                            item.upVoteComment.setColorFilter(R.color.buttonGrey)
                        }
                    }
                }
                RoomState.DONE -> {
                    if(comment.reputation > 0){
                        item.thumbUpDown.setImageResource(R.drawable.ic_baseline_thumb_up_24)
                        item.thumbUpDown.setColorFilter(Color.GREEN)
                    }else if(comment.reputation < 0){
                        item.thumbUpDown.setImageResource(R.drawable.ic_baseline_thumb_down_24)
                        item.thumbUpDown.setColorFilter(Color.RED)
                    }else{
                        item.thumbUpDown.setImageResource(R.drawable.ic_baseline_thumbs_up_down_24)
                        item.thumbUpDown.setColorFilter(R.color.buttonGrey)
                    }
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
            voting = true;
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
            SharedPrefHelper.voteComment(
                activity,
                roomId,
                contributionId,
                commentId,
                voteUp
            )
            call.enqueue(object : Callback<String> {
                override fun onResponse(
                    call: Call<String>,
                    response: Response<String>
                ) {

                    voting = false;
                    if (response.code() != 200) {
                        println("Error")
                    } else {

                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    voting = false;
                    println("Error")
                }

            })

        }
    }
}

