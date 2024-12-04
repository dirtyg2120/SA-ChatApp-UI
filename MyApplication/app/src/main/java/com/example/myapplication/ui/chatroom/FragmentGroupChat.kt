package com.example.myapplication.ui.chatroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentGroupChatBinding
import com.example.myapplication.model.ChatRoom
import com.example.myapplication.ui.groupchat.GroupAdapter

class FragmentGroupChat : Fragment() {

    private lateinit var groupAdapter: GroupAdapter
    private var selectedFriends = mutableListOf<ChatRoom>()
    private var allFriends = listOf<ChatRoom>() // Store the full list of friends

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentGroupChatBinding.inflate(inflater, container, false)

        // Initialize GroupAdapter
        groupAdapter = GroupAdapter(emptyList()) { friend, isSelected ->
            if (isSelected) {
                selectedFriends.add(friend)
            } else {
                selectedFriends.remove(friend)
            }
        }

        // Set up RecyclerView
        binding.rvFriendList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = groupAdapter
        }

        // Set up SearchView to filter the list of friends
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterFriends(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterFriends(newText)
                return true
            }
        })

        // Handle Create Group button click
        binding.btnCreateGroup.setOnClickListener {
            // Handle group creation logic
            createGroup()
        }

        // Fetch all friends (You can fetch friends from your database or API)
        fetchFriends()

        return binding.root
    }

    private fun filterFriends(query: String?) {
        val filteredFriends = if (query.isNullOrEmpty()) {
            allFriends
        } else {
            allFriends.filter {
                it.username?.contains(query, ignoreCase = true) == true
            }
        }
        groupAdapter.updateFriends(filteredFriends)
    }

    private fun createGroup() {
        // Handle group creation logic with selectedFriends
        val groupChat = createGroupChat(selectedFriends)
        // Navigate or process the group creation logic
    }

    // Fetch all friends - Replace with actual logic to get your list of friends
    private fun fetchFriends() {
        // Example friends, you can replace this with actual data fetching logic
        allFriends = listOf(
            ChatRoom(username = "Alice"),
            ChatRoom(username = "Bob"),
            ChatRoom(username = "Charlie")
        )
        groupAdapter.updateFriends(allFriends)
    }

    private fun createGroupChat(selectedFriends: List<ChatRoom>): ChatRoom {
        // Example: Create a group with the selected friends
        return ChatRoom(username = "New Group", messages = emptyList())
    }
}

