package com.dhbw.brainstorm.adapter

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.MultiAutoCompleteTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dhbw.brainstorm.R
import com.dhbw.brainstorm.api.CommentClient
import com.dhbw.brainstorm.api.ContributionClient
import com.dhbw.brainstorm.api.model.Contribution
import com.dhbw.brainstorm.api.model.Room
import com.dhbw.brainstorm.api.model.RoomState
import com.dhbw.brainstorm.helper.SharedPrefHelper
import kotlinx.android.synthetic.main.dialog_add_comment.*
import kotlinx.android.synthetic.main.dialog_add_comment.submitBtnNewCommentDialog
import kotlinx.android.synthetic.main.dialog_add_subject.*
import kotlinx.android.synthetic.main.item_comment_edit.view.contributionVotes
import kotlinx.android.synthetic.main.item_contribution_create.view.contentTextView
import kotlinx.android.synthetic.main.item_contribution_done.view.*
import kotlinx.android.synthetic.main.item_contribution_edit.view.*
import kotlinx.android.synthetic.main.item_contribution_edit.view.commentList
import kotlinx.android.synthetic.main.item_contribution_edit.view.commentsHeading
import kotlinx.android.synthetic.main.item_contribution_subject.view.*
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
import java.lang.RuntimeException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ContributionsAdapter(private var context: Context, private var activity: Activity) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_SUBJECT = 0
    private val TYPE_ITEM = 1

    var room = Room(ArrayList(), "", -1, "", false, RoomState.CREATE, "")
    var subjects = HashMap<Int, String?>()
    var checkIfIsSubject = ArrayList<Boolean>()
    var isModerator = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_ITEM) {
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
        } else if (viewType == TYPE_SUBJECT) {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_contribution_subject, parent, false)
            return SubjectViewHolder(itemView)
        }
        throw RuntimeException("Missing Type")
    }

    override fun getItemCount() = room.contributions.count() + subjects.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is SubjectViewHolder) {
            holder.initSubject(getSubjectForPosition(position))
        } else if (holder is ContributionViewHolder) {
            holder.initContribution(
                room.id,
                room.contributions[position - countSubjectsBeforePosition(position)],
                room.state,
                isModerator,
                context,
                subjects.values,
                activity
            )
        } else {
            throw RuntimeException("Invalid ViewHolder")
        }
    }


    override fun getItemViewType(position: Int): Int {
        if (checkIsSubject(position)) {
            return TYPE_SUBJECT
        } else {
            return TYPE_ITEM
        }
    }

    fun getSubjectForPosition(position: Int): String {
        for ((pos, subject) in subjects) {
            if (position - countSubjectsBeforePosition(position) == pos) {
                return subject ?: context.getString(R.string.noSubjectLabel)
            }
        }
        return ""
    }

    fun countSubjectsBeforePosition(position: Int): Int {
        var count = 0

        var sortedSubjects = TreeMap(subjects)
        sortedSubjects.forEach { (pos, subject) ->
            if (pos + count < position) {
                count++
            }
        }
        return count
    }

    fun checkIsSubject(position: Int): Boolean {
        return checkIfIsSubject.get(position)
    }

    fun update(room: Room, isModerator: Boolean) {
        if (room.contributions.size < this.room.contributions.size) {
            notifyItemRemoved(0)
        }
        this.isModerator = isModerator
        this.room = room
        this.room.contributions.sortBy { contribution -> contribution.subject }


        // move null values to the end
        var nullContributions =
            this.room.contributions.filter { contribution -> contribution.subject == null }
        this.room.contributions.removeAll(nullContributions)
        this.room.contributions.addAll(nullContributions)
        var numberOfPrevSubjects = this.subjects.size
        this.subjects = HashMap()
        this.checkIfIsSubject = ArrayList()
        for (i in 0 until room.contributions.size) {
            if (!this.subjects.containsValue(room.contributions[i].subject)) {
                this.subjects.put(i, room.contributions[i].subject)
                this.checkIfIsSubject.add(true)
            }
            this.checkIfIsSubject.add(false)
        }

        if (this.subjects.size < numberOfPrevSubjects) {
            notifyItemRemoved(0)
        }

        notifyItemRangeChanged(0, room.contributions.size + subjects.size)

    }


    class ContributionViewHolder(val item: View) : RecyclerView.ViewHolder(item) {
        var voting = false
        fun initContribution(
            roomId: Int,
            contribution: Contribution,
            roomState: RoomState,
            isModerator: Boolean,
            context: Context,
            subjects: MutableCollection<String?>,
            activity: Activity
        ) {

            item.contentTextView.text = contribution.content;
            when (roomState) {
                RoomState.CREATE -> {
                }
                RoomState.EDIT -> {


                    item.commentsHeading.text =
                        context.getString(R.string.commentNumber, contribution.comments.size)

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
                        0 -> {
                            item.downVoteContribution.setColorFilter(Color.RED)
                            item.upVoteContribution.setColorFilter(R.color.buttonGrey)
                        }
                        1 -> {
                            item.upVoteContribution.setColorFilter(Color.GREEN)
                            item.downVoteContribution.setColorFilter(R.color.buttonGrey)
                        }
                        -1 -> {
                            item.upVoteContribution.setColorFilter(R.color.buttonGrey)
                            item.downVoteContribution.setColorFilter(R.color.buttonGrey)
                        }
                    }


                    item.upVoteContribution.setOnClickListener {
                        if (SharedPrefHelper.contributionVoted(
                                activity,
                                roomId,
                                contribution.id,
                            ) == -1 && !voting
                        ) {
                            voteContribution(roomId, contribution.id, true, context, activity)
                            item.upVoteContribution.setColorFilter(Color.GREEN)
                            item.downVoteContribution.setColorFilter(R.color.buttonGrey)
                        }
                    }
                    item.downVoteContribution.setOnClickListener {
                        if (SharedPrefHelper.contributionVoted(
                                activity,
                                roomId,
                                contribution.id,
                            ) == -1 && !voting
                        ) {
                            voteContribution(roomId, contribution.id, false, context, activity)
                            item.downVoteContribution.setColorFilter(Color.RED)
                            item.upVoteContribution.setColorFilter(R.color.buttonGrey)
                        }
                    }

                    item.deleteContributionBtn.setOnClickListener {
                        AlertDialog.Builder(activity)
                            .setTitle(context.getString(R.string.deleteContributionLabel))
                            .setMessage(context.getString(R.string.areUSureLabel))
                            .setPositiveButton(
                                context.getString(R.string.yes),
                                DialogInterface.OnClickListener { dialog, which ->
                                    deleteContribution(roomId, contribution.id, context)
                                })
                            .setNegativeButton(context.getString(R.string.no), null)
                            .show()
                    }

                    item.editContributionBtn.setOnClickListener {
                        var dialog = Dialog(activity)
                        dialog.setContentView(R.layout.dialog_edit_contribution)
                        dialog.editTextNewCommentDialog.setText(contribution.content)
                        dialog.submitBtnNewCommentDialog.setOnClickListener {
                            updateContribution(
                                roomId,
                                contribution.id,
                                contribution.subject,
                                dialog.editTextNewCommentDialog.text.toString(),
                                context
                            )
                            dialog.dismiss()
                        }
                        dialog.show()
                    }
                    // copy needed, cause of call by reference
                    var subjectCopy = subjects.toMutableList()
                    subjectCopy.remove(null)
                    item.subjectContributionBtn.setOnClickListener {
                        var dialog = Dialog(activity)
                        dialog.setContentView(R.layout.dialog_add_subject)
                        dialog.subjectMultiAutoComplete.setAdapter(
                            ArrayAdapter<String>(
                                activity,
                                R.layout.dialog_add_subject_dropdown_item,
                                subjectCopy.toTypedArray()
                            )
                        )
                        dialog.submitBtnNewCommentDialog.setOnClickListener {
                            addSubject(
                                roomId,
                                contribution.id,
                                dialog.subjectMultiAutoComplete.text.toString(),
                                context
                            )
                            dialog.dismiss()
                        }
                        dialog.show()
                    }


                    if (!isModerator) {
                        item.deleteContributionBtn.visibility = View.GONE
                    } else {
                        item.deleteContributionBtn.visibility = View.VISIBLE
                    }

                }
                RoomState.DONE -> {
                    item.commentsHeading.text =
                        context.getString(R.string.commentNumber, contribution.comments.size)
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

                    if (contribution.reputation > 0) {
                        item.thumbUpDown.setImageResource(R.drawable.ic_baseline_thumb_up_24)
                        item.thumbUpDown.setColorFilter(Color.GREEN)
                    } else if (contribution.reputation < 0) {
                        item.thumbUpDown.setImageResource(R.drawable.ic_baseline_thumb_down_24)
                        item.thumbUpDown.setColorFilter(Color.RED)
                    } else {
                        item.thumbUpDown.setImageResource(R.drawable.ic_baseline_thumbs_up_down_24)
                        item.thumbUpDown.setColorFilter(R.color.buttonGrey)
                    }


                }
            }
        }

        private fun addComment(
            roomId: Int,
            contributionId: Int,
            content: String,
            context: Context
        ) {
            voting = true
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
                        voting = false
                        if (response.code() != 200) {
                            println("Error")
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        voting = false
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
            SharedPrefHelper.voteContribution(
                activity,
                roomId,
                contributionId,
                voteUp
            )
            call.enqueue(object : Callback<String> {
                override fun onResponse(
                    call: Call<String>,
                    response: Response<String>
                ) {

                    if (response.code() != 200) {
                        println("Error")
                    } else {

                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    println("Error")
                }

            })

        }

        private fun deleteContribution(
            roomId: Int,
            contributionId: Int,
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
                .create(ContributionClient::class.java)


            client.deleteContribution(roomId, contributionId).enqueue(object : Callback<String> {
                override fun onResponse(
                    call: Call<String>,
                    response: Response<String>
                ) {

                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                }

            })
        }

        private fun updateContribution(
            roomId: Int,
            contributionId: Int,
            subject: String,
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
                .create(ContributionClient::class.java)
            client.updateContribution(roomId, contributionId, subject, content)
                .enqueue(object : Callback<String> {
                    override fun onResponse(
                        call: Call<String>,
                        response: Response<String>
                    ) {

                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                    }

                })
        }

        private fun addSubject(
            roomId: Int,
            contributionId: Int,
            subject: String,
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
                .create(ContributionClient::class.java)
            client.addContributionSubject(roomId, contributionId, subject)
                .enqueue(object : Callback<String> {
                    override fun onResponse(
                        call: Call<String>,
                        response: Response<String>
                    ) {

                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                    }

                })
        }


    }

    class SubjectViewHolder(val item: View) : RecyclerView.ViewHolder(item) {
        fun initSubject(subject: String) {
            item.subjectText.text = subject
        }
    }

}