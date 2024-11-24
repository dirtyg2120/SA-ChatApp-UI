// ChatAdapter.kt
package com.example.myapplication.ui.chatroom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.ChatRoom
import com.example.myapplication.databinding.ItemChatMessageBinding

class ChatRoomAdapter(
    private val chatRooms: List<ChatRoom>,
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
                notifyItemChanged(currentPosition) // Update the background of clicked item
//                notifyItemChanged(selectedPosition) // Update the background of previously selected item (if any)
                onChatRoomClick(chatRooms[position])
//                onClickListener?.onClick(chatRooms[currentPosition]) // Call the click listener with the clicked chat room
            }
        }
    }

    override fun getItemCount(): Int = chatRooms.size

//    fun addMessages(newMessages: List<ChatMessage>) {
//        val startPosition = chatMessages.size
//        chatMessages.addAll(newMessages)
//        notifyItemRangeInserted(startPosition, newMessages.size)
//    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        val cardView: CardView = itemView.findViewById(R.id.cardView)

        fun bind(chatRoom: ChatRoom) {
            tvUsername.text = chatRoom.username
            tvMessage.text = chatRoom.message
        }
    }
}
