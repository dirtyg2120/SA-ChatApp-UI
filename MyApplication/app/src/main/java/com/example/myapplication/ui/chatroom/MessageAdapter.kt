package com.example.myapplication.ui.chatroom

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
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

        fun bind(message: Message) {
            messageTextView.text = message.content

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
            } else {
                // User's message: Right align
                messageTextView.background =
                    ContextCompat.getDrawable(itemView.context, R.drawable.chat_bubble_user)
                layoutParams.marginStart = 640
                layoutParams.marginEnd = 16
                parentLayout.gravity = Gravity.END
            }
            messageTextView.layoutParams = layoutParams
        }
    }
}

