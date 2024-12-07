package com.example.myapplication.ui.chatroom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
        private val ivAvatar: ImageView = itemView.findViewById(R.id.ivAvatar)
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val btnAddFriend: TextView = itemView.findViewById(R.id.btnAddFriend)
        private val btnMore: TextView = itemView.findViewById(R.id.btnMore)

        fun bind(chatRoom: ChatRoom) {
            tvUsername.text = chatRoom.username
            tvMessage.text = chatRoom.lastMessage
            tvTimestamp.text = chatRoom.lastMessageTime
            Glide.with(itemView.context)
                .load(chatRoom.avatarUrl)
                .placeholder(R.drawable.ic_avatar_default)
                .error(R.drawable.ic_avatar_default)
                .dontTransform()
                .into(ivAvatar)

            if (chatRoom.conversationId == null) {
                tvTimestamp.visibility = View.GONE
                btnMore.visibility = View.GONE
                btnAddFriend.visibility = View.VISIBLE
                btnAddFriend.setOnClickListener {
                    onAddFriendClick(chatRoom)
                    btnAddFriend.visibility = View.GONE
                    Toast.makeText(itemView.context, "Adding Friend: ${chatRoom.username}", Toast.LENGTH_SHORT).show()
                }
            } else {
                btnAddFriend.visibility = View.GONE
            }

            // Setup btnMore to show PopupMenu
            btnMore.setOnClickListener { view ->
                // Create a PopupMenu for the btnMore button
                val popupMenu = PopupMenu(itemView.context, view)
                val inflater = popupMenu.menuInflater
                inflater.inflate(R.menu.room_more_options, popupMenu.menu)

                // Handle menu item clicks
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.menu_edit -> {
                            // Handle Edit action
                            Toast.makeText(itemView.context, "Edit clicked for: ${chatRoom.username}", Toast.LENGTH_SHORT).show()
                            true
                        }
                        R.id.menu_remove -> {
                            // Handle Remove action
                            Toast.makeText(itemView.context, "Remove clicked for: ${chatRoom.username}", Toast.LENGTH_SHORT).show()
                            true
                        }
                        else -> false
                    }
                }

                // Show the menu
                popupMenu.show()
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

