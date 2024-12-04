package com.example.myapplication.ui.chatroom

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.api.ApiRepository
import com.example.myapplication.api.RetrofitInstance
import com.example.myapplication.databinding.FragmentGroupChatBinding
import com.example.myapplication.model.ChatRoom
import com.example.myapplication.ui.groupchat.GroupAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentGroupChat : Fragment() {

    private lateinit var groupAdapter: GroupAdapter
    private var selectedFriends = mutableListOf<ChatRoom>()
    private var allFriends = listOf<ChatRoom>()
    private val apiRepository = ApiRepository(RetrofitInstance.apiService)

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
            createConversation(groupAdapter.getSelectedFriendIds())
            view?.findNavController()?.navigateUp()
        }

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

    private fun createConversation(participants: MutableList<Int>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sharedPreferences = requireContext().getSharedPreferences("AuthPrefs", MODE_PRIVATE)
                val adminId = sharedPreferences.getInt("userId", 1)
                participants.add(adminId)
                apiRepository.generateConversation(adminId, participants, null)
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(this, "Friend added successfully!", Toast.LENGTH_SHORT).show()
//                }
                println("Create Group Success")
            } catch (e: Exception) {
                println(e)
            }
        }
    }

    // Fetch all friends - Replace with actual logic to get your list of friends
    private fun fetchFriends() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Retrieve the access token from SharedPreferences
                val sharedPreferences = requireContext().getSharedPreferences("AuthPrefs", MODE_PRIVATE)
                val accessToken = sharedPreferences.getString("accessToken", "") ?: ""
                val userId = sharedPreferences.getInt("userId", 1)

                val response = apiRepository.fetchMessages(
                    participantUserIds = userId,
                    phone = null,
                    name = null,
                    privateChat = true,
                    cookie = "$accessToken"
                )

                // If chat rooms exist, update the UI with the existing ones
                val allFriends = response.content.map { message ->
                    val participant = message.participants?.find { it.userId == userId }
                    val username = participant?.conversationDisplayName ?: ""
                    val conversationId = participant?.conversationId

                    ChatRoom(username = username, conversationId = conversationId)
                }

                withContext(Dispatchers.Main) {
                    groupAdapter.updateFriends(allFriends)
                }
            } catch (e: Exception) {
                e.message?.let { Log.e("", it) }
            }
        }
    }
}

