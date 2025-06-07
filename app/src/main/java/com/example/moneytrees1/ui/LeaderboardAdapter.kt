package com.example.moneytrees1.ui

// 🎨 Android UI and graphics imports
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

//♻️ RecyclerView imports
import androidx.recyclerview.widget.RecyclerView

//🏷️ Project-specific imports
import com.example.moneytrees1.R
import com.example.moneytrees1.data.LeaderboardUser
import androidx.core.graphics.toColorInt


class LeaderboardAdapter(
    private val users: List<LeaderboardUser>,
    private val realUserName: String
) : RecyclerView.Adapter<LeaderboardAdapter.UserViewHolder>() {

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rank = view.findViewById<TextView>(R.id.tvRank)
        val name = view.findViewById<TextView>(R.id.tvUsername)
        val score = view.findViewById<TextView>(R.id.tvScore)
               val item = view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard_entry, parent, false)
        return UserViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]

        // Rank number + emoji for top 3
        holder.rank.text = when (position) {
            0 -> "🥇"
            1 -> "🥈"
            2 -> "🥉"
            else -> "#${position + 1}"
        }

        holder.name.text = user.name
        holder.score.text = "R${user.score}"

         // Highlight real user row
        if (user.name == realUserName) {
            holder.item.setBackgroundColor("#fc95c9".toColorInt()) // light pink
            holder.name.setTypeface(null, Typeface.BOLD)
        } else {
            holder.item.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun getItemCount(): Int = users.size
}
