package com.dhbw.brainstorm.helper

import android.app.Activity
import android.content.Context
import java.util.*
import kotlin.collections.HashSet

class SharedPrefHelper {
    companion object {
        //-1 = not voted, 0 = down, 1 = up
        fun commentVoted(
            activity: Activity,
            roomId: Int,
            contributionId: Int,
            commentId: Int
        ): Int {
            val sharedPrev = activity.getSharedPreferences("votedComments", Context.MODE_PRIVATE)
            val votedComments = HashSet(sharedPrev.getStringSet("commentList", HashSet<String>())!!)
            for (votedComment in votedComments) {
                if (votedComment.startsWith(("$roomId|$contributionId|$commentId"))) {
                    var tmp = votedComment.split("|")
                    return if (tmp[3] == "up") 1 else 0
                }

            }
            return -1;
        }

        fun voteComment(
            activity: Activity,
            roomId: Int,
            contributionId: Int,
            commentId: Int,
            voteUp: Boolean
        ) {
            val sharedPrev = activity.getSharedPreferences("votedComments", Context.MODE_PRIVATE)

            with(sharedPrev.edit()) {
                val votedComments =
                    HashSet(sharedPrev.getStringSet("commentList", HashSet<String>())!!)
                votedComments.add("$roomId|$contributionId|$commentId|" + if (voteUp) "up" else "down")
                putStringSet("commentList", votedComments)
                apply()
            }
        }

        //-1 = not voted, 0 = down, 1 = up
        fun contributionVoted(activity: Activity, roomId: Int, contributionId: Int): Int {
            val sharedPrev =
                activity.getSharedPreferences("votedContributions", Context.MODE_PRIVATE)
            val votedComments =
                HashSet(sharedPrev.getStringSet("contributionList", HashSet<String>())!!)
            for (votedComment in votedComments) {
                if (votedComment.startsWith(("$roomId|$contributionId"))) {
                    var tmp = votedComment.split("|")
                    return if (tmp[2] == "up") 1 else 0
                }

            }
            return -1;
        }

        fun voteContribution(
            activity: Activity,
            roomId: Int,
            contributionId: Int,
            voteUp: Boolean
        ) {
            val sharedPrev =
                activity.getSharedPreferences("votedContributions", Context.MODE_PRIVATE)

            with(sharedPrev.edit()) {
                val votedContributions =
                    HashSet(sharedPrev.getStringSet("contributionList", HashSet<String>())!!)
                votedContributions.add("$roomId|$contributionId|" + if (voteUp) "up" else "down")
                putStringSet("contributionList", votedContributions)
                apply()
            }
        }

        fun getModeratorId(activity: Activity) : String{
            val sharedPrev = activity.getSharedPreferences("moderator", Context.MODE_PRIVATE)
            var moderatorId = sharedPrev.getString("moderatorId", "")
            if(moderatorId == ""){
                moderatorId = UUID.randomUUID().toString()
                with(sharedPrev.edit()){
                    putString("moderatorId", moderatorId)
                    apply()
                }
            }
            return moderatorId!!
        }
    }


}