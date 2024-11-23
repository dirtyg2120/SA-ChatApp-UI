package com.example.myapplication.ui.chatroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.databinding.FragmentChatRoomBinding
import com.example.myapplication.model.ChatRoom
import com.example.myapplication.model.Message
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    private val messages = mutableListOf<Message>() // Stores chat messages

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            chatRoom = it.getParcelable(ARG_CHAT_ROOM)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

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
            activity?.onBackPressed()
        }

        // Initialize RecyclerView
        messageAdapter = MessageAdapter(messages)
        binding.rvMessages.adapter = messageAdapter
        binding.rvMessages.layoutManager = LinearLayoutManager(context)

        // Handle "Send" button click
        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                addMessage(message, isFromOpponent = false) // User message
                binding.etMessage.text.clear()
            }
        }

        // Simulate incoming opponent messages
        simulateIncomingMessages()
    }

    private fun addMessage(content: String, isFromOpponent: Boolean) {
        messages.add(Message(content, isFromOpponent))
        messageAdapter.notifyItemInserted(messages.size - 1)
        binding.rvMessages.scrollToPosition(messages.size - 1)
    }

    private fun simulateIncomingMessages() {
        viewLifecycleOwner.lifecycleScope.launch {
            while (_binding != null) {
                addMessage("Hi, this is a message from your mate!", isFromOpponent = true)
                delay(10000) // Wait for 5 seconds before adding the next message
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
