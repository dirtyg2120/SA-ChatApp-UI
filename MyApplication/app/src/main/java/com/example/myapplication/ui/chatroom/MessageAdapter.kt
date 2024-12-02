package com.example.myapplication.ui.chatroom

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.model.Message

class MessageAdapter(
    private val messages: List<Message>
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_bubble, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.tv_message)
        private val avatarImageView: ImageView = itemView.findViewById(R.id.ivAvatar)
        private val imageMessageView: ImageView = itemView.findViewById(R.id.iv_image_message) // ImageView for displaying images

        fun bind(message: Message) {
            // Check if the message content is a URL (image link)
            if (isValidImageUrl(message.content.toString())) {
                // If it's an image URL, load the image
                messageTextView.visibility = View.GONE
                imageMessageView.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(message.content)
                    .into(imageMessageView)
            } else {
                // If it's a text message
                messageTextView.visibility = View.VISIBLE
                imageMessageView.visibility = View.GONE
                messageTextView.text = message.content
            }

            // Align message based on sender
            val layoutParams = messageTextView.layoutParams as ViewGroup.MarginLayoutParams
            val parentLayout = itemView.findViewById<LinearLayout>(R.id.messageContainer)

            if (message.isFromOpponent) {
                // Opponent's message: Left align
                messageTextView.background =
                    ContextCompat.getDrawable(itemView.context, R.drawable.chat_bubble_partner)
                layoutParams.marginStart = 16
                layoutParams.marginEnd = 64
                parentLayout.gravity = Gravity.START
                avatarImageView.visibility = View.VISIBLE
            } else {
                // User's message: Right align
                messageTextView.background =
                    ContextCompat.getDrawable(itemView.context, R.drawable.chat_bubble_user)
                layoutParams.marginStart = 640
                layoutParams.marginEnd = 16
                parentLayout.gravity = Gravity.END
                avatarImageView.visibility = View.GONE
            }
            messageTextView.layoutParams = layoutParams
        }

        // Helper function to check if the URL is a valid image URL
        private fun isValidImageUrl(url: String): Boolean {
            return url.startsWith("https://") && (url.endsWith(".jpg") || url.endsWith(".png") || url.endsWith(".jpeg"))
        }
    }
}
