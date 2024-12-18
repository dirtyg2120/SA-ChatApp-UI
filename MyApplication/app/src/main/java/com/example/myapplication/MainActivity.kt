package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.api.ApiRepository
import com.example.myapplication.api.RetrofitInstance
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.model.ChatRoom
import com.example.myapplication.ui.authen.LoginActivity
import com.example.myapplication.ui.chatroom.ChatRoomAdapter
import com.example.myapplication.ui.chatroom.ChatRoomFragment
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    lateinit var chatRoomAdapter: ChatRoomAdapter
    private val apiRepository = ApiRepository(RetrofitInstance.apiService)
    private lateinit var fab: FloatingActionButton
    private lateinit var fabGroupChat: ExtendedFloatingActionButton
    private lateinit var navView: NavigationView
    private lateinit var myMenu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        setupFABs()

        updateAvatar()

        // Drawer and Navigation View setup
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_profile, R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Initialize RecyclerView for chat rooms
        setupChatRecyclerView()

        // Handling changes in the destination fragment
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
            val params = recyclerView.layoutParams

            when (destination.id) {
                R.id.nav_home -> {
                    params.height = ConstraintLayout.LayoutParams.MATCH_PARENT
                    recyclerView.layoutParams = params
                    fab.visibility = View.VISIBLE
                    fabGroupChat.visibility = View.GONE
                }
                R.id.nav_profile -> {
                    params.height = 1
                    recyclerView.layoutParams = params
                    fab.visibility = View.GONE
                    fabGroupChat.visibility = View.GONE
                }
                else -> {
                    params.height = 1
                    recyclerView.layoutParams = params
                    fab.visibility = View.GONE
                    fabGroupChat.visibility = View.GONE
                }
            }
        }
    }


    private fun setupChatRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        chatRoomAdapter = ChatRoomAdapter(
            chatRooms = emptyList(),
            onChatRoomClick = { chatRoom ->
                openChatRoom(chatRoom)
            },
            onAddFriendClick = { chatRoom ->
                handleAddFriendClick(chatRoom)
                fetchChatRooms()
            }
        )
        recyclerView.adapter = chatRoomAdapter
        fetchChatRooms()
    }

    // Function to fetch chat rooms after login or initialization
    fun fetchChatRooms(phone: String? = null, name: String? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Retrieve the access token from SharedPreferences
                val sharedPreferences = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
                val accessToken = sharedPreferences.getString("accessToken", "") ?: ""
                val userId = sharedPreferences.getInt("userId", 1)

                if (accessToken.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Authentication token missing!", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                // API call to fetch messages, with phone/name as a search parameter if available
                val response = apiRepository.fetchMessages(
                    participantUserIds = userId,
                    phone = phone,
                    name = name,
                    privateChat = null,
                    cookie = "$accessToken"
                )

                // If chat rooms exist, update the UI with the existing ones
                val chatRooms = response.content.map { message ->
                    val participant = message.participants?.find { it.userId == userId }
                    val username = if ((message.participants?.size ?: 0) > 2) {
                        message.participants?.take(3)?.joinToString(", ") { it.participantName ?: "" } ?: ""
                    } else {
                        participant?.conversationDisplayName ?: ""
                    }

                    val partner = message.participants?.find { it.userId != userId }
                    val avatarUrl = if ((message.participants?.size ?: 0) > 2) {
                        null
                    } else {
                        partner?.profilePhoto
                    }
                    val lastMessage = message.chatMessages?.lastOrNull()?.content ?: ""
                    val lastMessageTime = message.lastMessageTime?.dropLast(9) ?: ""
                    val messages = message.chatMessages ?: emptyList()
                    val conversationId = participant?.conversationId

                    ChatRoom(username = username, lastMessage = lastMessage, conversationId = conversationId, messages = messages, lastMessageTime=lastMessageTime, avatarUrl = avatarUrl)
                }

                if (chatRooms.isEmpty() && phone != null) {
                    // If no chat rooms found and phone is provided, call findUser to create a new chat room
                    val userResponse = apiRepository.findUser(phone)
                    val newChatRooms = userResponse.content.map { user ->
                        val userId = user.id
                        val username = user.UserName
                        ChatRoom(username = username, lastMessage = null, conversationId = null, messages = emptyList(), userId = userId)
                    }
                    // Add the newly created chat room
                    withContext(Dispatchers.Main) {
                        chatRoomAdapter.updateChatRooms(newChatRooms)
                    }
                } else {
                    // Otherwise, update the chat rooms with the fetched data
                    withContext(Dispatchers.Main) {
                        chatRoomAdapter.updateChatRooms(chatRooms)
                    }
                }
            } catch (e: Exception) {
                e.message?.let { Log.e("", it) }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Failed to load messages: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun openChatRoom(chatRoom: ChatRoom) {
        val sharedPreferences = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("userId", 1)

        val fragmentTag = "ChatRoom_${chatRoom.username}"
        val existingFragment = supportFragmentManager.findFragmentByTag(fragmentTag)

        if (existingFragment == null) {
            val chatRoomFragment = ChatRoomFragment.newInstance(chatRoom, userId)

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, chatRoomFragment, fragmentTag)
                .addToBackStack(fragmentTag)
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .show(existingFragment)
                .commit()
        }
    }

    // Handling search query in onCreateOptionsMenu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        myMenu = menu

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                println("Query: $query")
                if (query.isNullOrEmpty()) {
                    fetchChatRooms()
                } else if (query.startsWith("0")) {
                    fetchChatRooms(phone = query)
                } else {
                    fetchChatRooms(name = query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                println("Query changed: $newText")
                if (newText.isNullOrEmpty()) {
                    fetchChatRooms()
                } else if (newText.startsWith("0")) {
                    fetchChatRooms(phone = newText)
                } else {
                    fetchChatRooms(name = newText)
                }
                return true
            }
        })
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_logout -> {
                val sharedPref = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
                with(sharedPref.edit()) {
                    clear()
                    apply()
                }

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun handleAddFriendClick(chatRoom: ChatRoom) {
        val sharedPreferences = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
        val adminId = sharedPreferences.getInt("userId", 1)
        val opponentUserId = chatRoom.userId
        if (opponentUserId != null) {
            createConversation(adminId, opponentUserId)
        }
    }

    private fun createConversation(adminId: Int, opponentUserId: Int) {
        val participants = listOf(opponentUserId) + adminId

        CoroutineScope(Dispatchers.IO).launch {
            try {
                apiRepository.generateConversation(adminId, participants, null)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Friend added successfully!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupFABs() {
        // Initialize the FABs and set up their click logic
        fab = findViewById(R.id.fab)
        fabGroupChat = findViewById(R.id.fab_group_chat)

        fab.visibility = View.VISIBLE
        fabGroupChat.visibility = View.GONE

        // FAB click logic to show/hide group chat FAB
        fab.setOnClickListener {
            if (fabGroupChat.visibility == View.GONE) {
                fabGroupChat.visibility = View.VISIBLE
            } else {
                fabGroupChat.visibility = View.GONE
            }
        }

        fabGroupChat.setOnClickListener {
            fabGroupChat.visibility = View.GONE
            fab.visibility = View.GONE
            val navController = findNavController(R.id.nav_host_fragment_content_main)
            navController.navigate(R.id.fragmentGroupChat)
        }
    }

    private fun updateAvatar() {
        navView = findViewById(R.id.nav_view)
        val headerView = navView.getHeaderView(0)
        val sharedPreferences = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("userId", 1)
        val phone = sharedPreferences.getString("phone", null)
        lifecycleScope.launch {
            try {
                val userResponse = apiRepository.findUser(phone)
                val user = userResponse.content.find { it.id == userId }

                user?.let {
                    // Load profile photo if available, otherwise use default avatar
                val profilePhotoUrl = it.profilePhoto
                if (!profilePhotoUrl.isNullOrEmpty()) {
                    Glide.with(this@MainActivity)
                        .load(profilePhotoUrl).dontTransform()
                        .into(headerView.findViewById<ShapeableImageView>(R.id.imageAvaView))
                } else {
                    Glide.with(this@MainActivity)
                        .load(R.drawable.ic_avatar_default)
                        .into(headerView.findViewById<ShapeableImageView>(R.id.imageAvaView))
                }
                } ?: run {
                    // Handle case where user is not found (e.g., show an error message)
                }
            } catch (e: Exception) {
                println(e)
            }
        }
    }
}
