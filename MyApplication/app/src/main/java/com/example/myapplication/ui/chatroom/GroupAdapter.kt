package com.example.myapplication.ui.groupchat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.ChatRoom

class GroupAdapter(
    private var friends: List<ChatRoom>,
    private val onFriendSelect: (ChatRoom, Boolean) -> Unit
) : RecyclerView.Adapter<GroupAdapter.FriendViewHolder>() {

    private val selectedFriendIds = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_group, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]
        holder.bind(friend)
    }

    override fun getItemCount(): Int = friends.size

    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFriendName: TextView = itemView.findViewById(R.id.tvFriendName)
        private val checkboxSelectFriend: CheckBox = itemView.findViewById(R.id.checkboxSelectFriend)

        fun bind(friend: ChatRoom) {
            tvFriendName.text = friend.username
            checkboxSelectFriend.isChecked = selectedFriendIds.contains(friend.userId)

            checkboxSelectFriend.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    friend.userId?.let { selectedFriendIds.add(it) }
                } else {
                    friend.userId?.let { selectedFriendIds.remove(it) }
                }
                onFriendSelect(friend, isChecked)
            }
        }
    }

    fun updateFriends(newFriends: List<ChatRoom>) {
        friends = newFriends
        notifyDataSetChanged()
    }

    fun getSelectedFriendIds(): MutableList<Int> {
        return selectedFriendIds.toMutableList()
    }
}
