package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.api.ApiRepository
import com.example.myapplication.api.RetrofitInstance
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.model.ChatRoom
import com.example.myapplication.ui.chatroom.ChatRoomAdapter
import com.example.myapplication.ui.chatroom.ChatRoomFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var chatRoomAdapter: ChatRoomAdapter
    private val apiRepository = ApiRepository(RetrofitInstance.apiService)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        setupChatRecyclerView()
    }

    private fun setupChatRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = ChatRoomAdapter(emptyList()) { chatRoom ->
            openChatRoom(chatRoom)
        }
        recyclerView.adapter = adapter

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Retrieve the access token from SharedPreferences
                val sharedPreferences = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
                val accessToken = sharedPreferences.getString("accessToken", "") ?: ""
                val userId = sharedPreferences.getInt("userId", 0)

                if (accessToken.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Authentication token missing!", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                // API call to fetch messages
                val response = apiRepository.fetchMessages(
                    participantUserIds = userId,
                    page = 0,
                    pageSize = 100,
                    cookie = "$accessToken"
                )

                Log.d("", response.toString())

                // Map API response to ChatRoom model
                val chatRooms = response.content.map { message ->
                    val username = message.participants?.getOrNull(0)?.conversationDisplayName ?: ""
                    val messageText = message.chatMessages?.getOrNull(0)?.content ?: ""
                    ChatRoom(username = username, message = messageText)
                }

                withContext(Dispatchers.Main) {
                    adapter.updateChatRooms(chatRooms)
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
        val fragmentTag = "ChatRoom_${chatRoom.username}"
        val existingFragment = supportFragmentManager.findFragmentByTag(fragmentTag)

        if (existingFragment == null) {
            val chatRoomFragment = ChatRoomFragment.newInstance(chatRoom)

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, chatRoomFragment, fragmentTag)
                .addToBackStack(fragmentTag)
                .commit()
        } else {
            supportFragmentManager.popBackStack(fragmentTag, 0)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
