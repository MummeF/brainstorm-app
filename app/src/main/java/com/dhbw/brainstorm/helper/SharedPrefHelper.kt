package com.dhbw.brainstorm.helper

import android.app.Activity
import android.content.Context
import com.dhbw.brainstorm.api.model.Room
import com.dhbw.brainstorm.api.model.RoomState
import java.util.*
import kotlin.collections.ArrayList
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
            val sharedPrev = activity.getSharedPreferences(VOTED_COMMENTS_SHARED_PREF, Context.MODE_PRIVATE)
            val votedComments = HashSet(sharedPrev.getStringSet(COMMENT_LIST_SHARED_PREF, HashSet<String>())!!)
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
            val sharedPrev = activity.getSharedPreferences(VOTED_COMMENTS_SHARED_PREF, Context.MODE_PRIVATE)

            with(sharedPrev.edit()) {
                val votedComments =
                    HashSet(sharedPrev.getStringSet(COMMENT_LIST_SHARED_PREF, HashSet<String>())!!)
                votedComments.add("$roomId|$contributionId|$commentId|" + if (voteUp) "up" else "down")
                putStringSet(COMMENT_LIST_SHARED_PREF, votedComments)
                apply()
            }
        }

        //-1 = not voted, 0 = down, 1 = up
        fun contributionVoted(activity: Activity, roomId: Int, contributionId: Int): Int {
            val sharedPrev =
                activity.getSharedPreferences(VOTED_CONTRIBUTIONS_SHARED_PREF, Context.MODE_PRIVATE)
            val votedComments =
                HashSet(sharedPrev.getStringSet(CONTRIBUTION_LIST_SHARED_PREF, HashSet<String>())!!)
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
                activity.getSharedPreferences(VOTED_CONTRIBUTIONS_SHARED_PREF, Context.MODE_PRIVATE)

            with(sharedPrev.edit()) {
                val votedContributions =
                    HashSet(sharedPrev.getStringSet(CONTRIBUTION_LIST_SHARED_PREF, HashSet<String>())!!)
                votedContributions.add("$roomId|$contributionId|" + if (voteUp) "up" else "down")
                putStringSet(CONTRIBUTION_LIST_SHARED_PREF, votedContributions)
                apply()
            }
        }

        fun getModeratorId(activity: Activity): String {
            val sharedPrev = activity.getSharedPreferences(MODERATOR_SHARED_PREF, Context.MODE_PRIVATE)
            var moderatorId = sharedPrev.getString(MODERATOR_ID_SHARED_PREF, "")
            if (moderatorId == "") {
                moderatorId = UUID.randomUUID().toString()
                with(sharedPrev.edit()) {
                    putString(MODERATOR_ID_SHARED_PREF, moderatorId)
                    apply()
                }
            }
            return moderatorId!!
        }

        fun addFavorite(activity: Activity, roomId: Int, topic: String) {
            val sharedPref = activity.getSharedPreferences(FAVORITE_SHARED_PREF, Context.MODE_PRIVATE)

            with(sharedPref.edit()) {
                val favoriteList =
                    HashSet(sharedPref.getStringSet(FAVORITE_LIST_SHARED_PREF, HashSet<String>())!!)
                favoriteList.add("$roomId|$topic")
                putStringSet(FAVORITE_LIST_SHARED_PREF, favoriteList)
                apply()
            }
        }

        fun removeFavorite(activity: Activity, roomId: Int): Boolean {
            val sharedPref = activity.getSharedPreferences(FAVORITE_SHARED_PREF, Context.MODE_PRIVATE)
            var response = false;
            with(sharedPref.edit()) {
                val favoriteList =
                    HashSet(sharedPref.getStringSet(FAVORITE_LIST_SHARED_PREF, HashSet<String>())!!)
                favoriteList.forEach { room ->
                    if (room!!.startsWith(roomId.toString(), true)) {
                        response = favoriteList.remove(room)
                    }
                }
                putStringSet(FAVORITE_LIST_SHARED_PREF, favoriteList)
                apply()
            }
            return response;
        }

        fun getFavorites(activity: Activity): List<Room> {
            val sharedPref = activity.getSharedPreferences(FAVORITE_SHARED_PREF, Context.MODE_PRIVATE)
            val favoriteList = HashSet(sharedPref.getStringSet(FAVORITE_LIST_SHARED_PREF, HashSet<String>())!!)
            var result = favoriteList.map { favorite ->
                var tmp = favorite.split("|")
                Room(ArrayList(), "", tmp[0].toInt(), "", false, RoomState.CREATE, tmp[1])
            }
            return result
        }

        fun isFavorite(activity: Activity, roomId: Int): Boolean{
            val favorites = getFavorites(activity)
            var isFav = false;
            favorites.forEach{ room ->
                if(room.id == roomId)
                    isFav = true
            }
            return isFav
        }
    }


}