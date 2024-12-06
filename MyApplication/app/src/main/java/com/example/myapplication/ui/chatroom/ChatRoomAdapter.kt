package com.example.myapplication.ui.chatroom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.ChatRoom
import com.example.myapplication.databinding.ItemChatRoomBinding

class ChatRoomAdapter(
    private var chatRooms: List<ChatRoom>,
    private val onChatRoomClick: (ChatRoom) -> Unit,
    private val onAddFriendClick: (ChatRoom) -> Unit // Add the callback for add friend
) : RecyclerView.Adapter<ChatRoomAdapter.ChatViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatRoomBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chatRooms[position])

        holder.itemView.setOnClickListener {
            val currentPosition = position
            if (selectedPosition != currentPosition) {
                selectedPosition = currentPosition
                notifyItemChanged(currentPosition)
                onChatRoomClick(chatRooms[position])
            }
        }
    }

    override fun getItemCount(): Int = chatRooms.size

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val btnAddFriend: TextView = itemView.findViewById(R.id.btnAddFriend)

        fun bind(chatRoom: ChatRoom) {
            tvUsername.text = chatRoom.username
            tvMessage.text = chatRoom.lastMessage
            tvTimestamp.text = chatRoom.lastMessageTime

            if (chatRoom.conversationId == null) {
                tvTimestamp.visibility = View.GONE
                btnAddFriend.visibility = View.VISIBLE
                btnAddFriend.setOnClickListener {
                    onAddFriendClick(chatRoom)
                    Toast.makeText(itemView.context, "Adding Friend: ${chatRoom.username}", Toast.LENGTH_SHORT).show()
                }
            } else {
                btnAddFriend.visibility = View.GONE
            }
        }
    }

    // Call this method to update the list of chat rooms when necessary
    fun updateChatRooms(newChatRooms: List<ChatRoom>) {
        chatRooms = newChatRooms
        notifyDataSetChanged()
    }

    // Optionally reset selected position when navigating back
    fun resetSelection() {
        selectedPosition = -1
        notifyDataSetChanged()
    }
}

