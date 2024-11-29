package com.example.myapplication.ui.chatroom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.ChatRoom
import com.example.myapplication.databinding.ItemChatMessageBinding

class ChatRoomAdapter(
    private var chatRooms: List<ChatRoom>,
    private val onChatRoomClick: (ChatRoom) -> Unit
) : RecyclerView.Adapter<ChatRoomAdapter.ChatViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatMessageBinding.inflate(
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

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)

        fun bind(chatRoom: ChatRoom) {
            tvUsername.text = chatRoom.username
            tvMessage.text = chatRoom.message
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
