package com.example.myapplication.ui.chatroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentChatRoomBinding
import com.example.myapplication.model.ChatRoom

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

        // Set up the toolbar as the app bar with the back button
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbarChatRoom)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = chatRoom?.username ?: "Chat Room"
        }

        // Handle back button click
        binding.toolbarChatRoom.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

