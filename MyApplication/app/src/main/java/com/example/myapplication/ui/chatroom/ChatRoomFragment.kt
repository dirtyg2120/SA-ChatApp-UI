package com.example.myapplication.ui.chatroom

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.MainActivity
import com.example.myapplication.databinding.FragmentChatRoomBinding
import com.example.myapplication.model.ChatRoom
import com.example.myapplication.model.Message
import com.example.myapplication.model.WebSocketMessage
import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*

class ChatRoomFragment : Fragment() {

    companion object {
        private const val ARG_CHAT_ROOM = "chat_room"

        fun newInstance(chatRoom: ChatRoom): ChatRoomFragment {
            val fragment = ChatRoomFragment()
            val args = Bundle()
            args.putParcelable(ARG_CHAT_ROOM, chatRoom)
            fragment.arguments = args
            return fragment
        }
    }

    private var chatRoom: ChatRoom? = null
    private var _binding: FragmentChatRoomBinding? = null
    private val binding get() = _binding!!

    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<Message>()

    // WebSocket client setup
    private val client = HttpClient(CIO) {
        install(WebSockets)
    }
    private lateinit var session: WebSocketSession
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            chatRoom = it.getParcelable(ARG_CHAT_ROOM)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var isSessionConnected = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the toolbar
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbarChatRoom)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = chatRoom?.username ?: "Chat Room"
        }

        // Handle back button click
        binding.toolbarChatRoom.setNavigationOnClickListener {
            (requireActivity() as? MainActivity)?.fetchChatRooms()
            (requireActivity() as? MainActivity)?.chatRoomAdapter?.resetSelection()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Initialize RecyclerView
        messageAdapter = MessageAdapter(messages)
        binding.rvMessages.adapter = messageAdapter
        binding.rvMessages.layoutManager = LinearLayoutManager(context)

        // Handle "Send" button click
        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
//                val contentType = if (isSessionConnected) {
//                    "TEXT" // Session is open, send a regular message
//                } else {
//                    "INIT_SESSION" // Session is not open, send an initial session message
//                }
                sendMessage(senderId = 1, contentType = "INIT_SESSION")
                sendMessage(senderId = 1, contentType = "TEXT", content = message, conversationId = 1)
                addMessage(message, isFromOpponent = false) // User message
                binding.etMessage.text.clear()
                // closeSession()
            }
        }

        // Connect to WebSocket
        connectToWebSocket()
    }


    private fun connectToWebSocket() {
        lifecycleScope.launch {
            try {
                // Connect to WebSocket server
                session = client.webSocketSession(host = "128.199.91.226", port = 8082, path = "text")
                isSessionConnected = true // Set the flag to true when connected

                // Listen for incoming messages
                while (true) {
                    val frame = session.incoming.receive()
                    when (frame) {
                        is Frame.Text -> {
                            val jsonMessage = frame.readText()
                            Log.d("WebSocket", "Message received: $jsonMessage")
                            handleIncomingMessage(jsonMessage) // Handle the incoming message
                        }
                        else -> {
                            Log.d("WebSocket", "Non-text frame received")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("WebSocket", "Error: ${e.localizedMessage}")
                isSessionConnected = false // Set the flag to false if the connection fails
            }
        }
    }


    private fun handleIncomingMessage(jsonMessage: String) {
        try {
            // Deserialize the incoming JSON string to WebSocketMessage using Gson
            val message = gson.fromJson(jsonMessage, WebSocketMessage::class.java)
            // Handle the message here (e.g., update the UI)
            Log.d("WebSocket", "Parsed message: $message")
            message.content?.let { addMessage(it, isFromOpponent = true) }
        } catch (e: Exception) {
            Log.e("WebSocket", "Error parsing incoming message: ${e.localizedMessage}")
        }
    }

    private fun sendMessage(senderId: Int, contentType: String, content: String? = null, conversationId: Int? = null) {
        // Create the message object
        val message = WebSocketMessage(
            senderId = senderId,
            conversationId = conversationId,
            contentType = contentType,
            content = content
        )

        // Serialize the message to JSON using Gson
        val jsonMessage = gson.toJson(message)

        // Send the message over WebSocket
        lifecycleScope.launch {
            try {
                session.send(jsonMessage) // Send message as JSON
                Log.d("WebSocket", "Message sent: $jsonMessage")
            } catch (e: Exception) {
                Log.e("WebSocket", "Error sending message: ${e.localizedMessage}")
            }
        }
    }

    private fun addMessage(content: String, isFromOpponent: Boolean) {
        messages.add(Message(content = content, isFromOpponent = isFromOpponent))
        messageAdapter.notifyItemInserted(messages.size - 1)
        binding.rvMessages.scrollToPosition(messages.size - 1)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        client.close()
    }

    private fun closeSession() {
        lifecycleScope.launch {
            try {
                // Check if the session is connected before trying to close it
                if (isSessionConnected) {
                    session.close() // Close the WebSocket session
                    isSessionConnected = false
                    Log.d("WebSocket", "Session closed after sending message.")
                }
            } catch (e: Exception) {
                Log.e("WebSocket", "Error while closing session: ${e.localizedMessage}")
            }
        }
    }
}
