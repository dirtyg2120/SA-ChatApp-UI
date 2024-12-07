package com.example.myapplication.ui.chatroom

import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
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
import com.example.myapplication.model.Conv

class MessageAdapter(
    private val convs: List<Conv>
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_bubble, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(convs[position])
    }

    override fun getItemCount(): Int = convs.size

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.tv_message)
        private val senderTextView: TextView = itemView.findViewById(R.id.tv_sender_name)
        private val imageMessageView: ImageView = itemView.findViewById(R.id.iv_image_message) // ImageView for displaying images

        fun bind(conv: Conv) {
            // Check if the message content is a URL (image link or other links)
            if (isValidImageUrl(conv.content.toString())) {
                // If it's an image URL, load the image
                messageTextView.visibility = View.GONE
                imageMessageView.visibility = View.VISIBLE
                Glide.with(itemView.context).load(conv.content).dontTransform().into(imageMessageView)
            } else if (isValidUrl(conv.content.toString())) {
                // If it's a URL (not an image), display it as a clickable link
                messageTextView.visibility = View.VISIBLE
                imageMessageView.visibility = View.GONE
                messageTextView.text = conv.content
                makeLinkClickable(conv.content.toString())
            } else {
                // If it's a text message
                messageTextView.visibility = View.VISIBLE
                imageMessageView.visibility = View.GONE
                messageTextView.text = conv.content
            }

            // Align message based on sender
            val layoutParams = messageTextView.layoutParams as ViewGroup.MarginLayoutParams
            val parentLayout = itemView.findViewById<LinearLayout>(R.id.messageContainer)

            if (conv.isFromOpponent) {
                // Opponent's message: Left align
                senderTextView.text = conv.senderName
                messageTextView.background = ContextCompat.getDrawable(itemView.context, R.drawable.chat_bubble_partner)
                imageMessageView.background = ContextCompat.getDrawable(itemView.context, R.drawable.chat_bubble_partner)
                layoutParams.marginStart = 16
                layoutParams.marginEnd = 64
                parentLayout.gravity = Gravity.START
            } else {
                // User's message: Right align
                senderTextView.text = "You"
                messageTextView.background = ContextCompat.getDrawable(itemView.context, R.drawable.chat_bubble_user)
                imageMessageView.background = ContextCompat.getDrawable(itemView.context, R.drawable.chat_bubble_user)
                layoutParams.marginStart = 64
                layoutParams.marginEnd = 16
                parentLayout.gravity = Gravity.END
            }
            messageTextView.layoutParams = layoutParams
        }

        // Helper function to check if the URL is a valid image URL
        private fun isValidImageUrl(url: String): Boolean {
            return url.startsWith("https://") && (url.endsWith(".jpg") || url.endsWith(".png") || url.endsWith(".jpeg"))
        }

        // Helper function to check if the URL is a valid non-image URL
        private fun isValidUrl(url: String): Boolean {
            return url.startsWith("http://") || url.startsWith("https://")
        }

        // Helper function to make the URL clickable
        private fun makeLinkClickable(url: String) {
            val spannable = Spannable.Factory.getInstance().newSpannable(url)
            val span = URLSpan(url)
            spannable.setSpan(span, 0, url.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            messageTextView.text = spannable
            messageTextView.movementMethod = LinkMovementMethod.getInstance()
            messageTextView.setLinkTextColor(ContextCompat.getColor(itemView.context, R.color.purple_700))
        }
    }
}
