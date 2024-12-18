package com.example.myapplication.ui.chatroom

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.MainActivity
import com.example.myapplication.api.ApiRepository
import com.example.myapplication.api.RetrofitInstance
import com.example.myapplication.databinding.FragmentChatRoomBinding
import com.example.myapplication.model.ChatRoom
import com.example.myapplication.model.Conv
import com.example.myapplication.model.WebSocketMessage
import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


class ChatRoomFragment : Fragment() {

    companion object {
        private const val ARG_CHAT_ROOM = "chat_room"
        private const val ARG_USER_ID = "user_id"
        private const val ARG_CONVERSATION_ID = "conversation_id"

        fun newInstance(chatRoom: ChatRoom, userId: Int): ChatRoomFragment {
            val fragment = ChatRoomFragment()
            val args = Bundle()
            args.putParcelable(ARG_CHAT_ROOM, chatRoom)
            args.putInt(ARG_USER_ID, userId)
            args.putInt(ARG_CONVERSATION_ID, chatRoom.conversationId!!)
            fragment.arguments = args
            return fragment
        }
    }

    private var chatRoom: ChatRoom? = null
    private var userId: Int = 1
    private var conversationId: Int? = null
    private var _binding: FragmentChatRoomBinding? = null
    private val binding get() = _binding!!

    private lateinit var messageAdapter: MessageAdapter
    private val convs = mutableListOf<Conv>()
    private val userMapping = mutableMapOf<Int, String>()

    // WebSocket client setup
    private val client = HttpClient(CIO) {
        install(WebSockets)
    }
    private lateinit var session: WebSocketSession
    private val gson = Gson()

    private var isSessionConnected = false
    private var isSessionInitialized = false

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { uploadFile(it) }
    }

    // Camera capture launcher
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        println("yayyyyyyy")
        println(imgUri?.path)
        if (isSuccess) {
            imgUri?.let { uploadFile(it) }
        } else {
            Log.e("Camera", "Failed to capture image.")
        }
    }

    private var imgUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            chatRoom = it.getParcelable(ARG_CHAT_ROOM)
        }
    }

    private fun fetchUsers() {
        lifecycleScope.launch {
            try {
                val users = ApiRepository(RetrofitInstance.apiService).findUser(phone = null)
                users.content.forEach { user ->
                    userMapping[user.id] = user.UserName
                }
                updateMessages()
            } catch (e: Exception) {
                Log.e("ChatRoomFragment", "Error fetching users: ${e.message}")
            }
        }
    }

    private fun updateMessages() {
        chatRoom?.messages?.let { chatMessages ->
            chatMessages.forEach { chatMessage ->
                val isFromOpponent = chatMessage.sender != userId
                val name = userMapping[chatMessage.sender] ?: "Unknown"
                addMessage(content = chatMessage.content.toString(), isFromOpponent = isFromOpponent, name = name)
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        arguments?.let {
            userId = it.getInt(ARG_USER_ID)
            conversationId = it.getInt(ARG_CONVERSATION_ID)
        }

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
        messageAdapter = MessageAdapter(convs)
        binding.rvMessages.adapter = messageAdapter
        binding.rvMessages.layoutManager = LinearLayoutManager(context)

        fetchUsers()
//        arguments?.getParcelable<ChatRoom>(ARG_CHAT_ROOM)?.let {
//            chatRoom = it
//            convs.clear()
//            chatRoom?.messages?.let { chatMessages ->
//                chatMessages.forEach { chatMessage ->
//                    val isFromOpponent = chatMessage.sender != userId
//                    val name = userMapping[chatMessage.sender] ?: "Unknown"
//                    addMessage(content = chatMessage.content.toString(), isFromOpponent = isFromOpponent, name = name)
//                }
//            }
//        }


        // Handle "Send" button click
        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                if (!isSessionInitialized) {
                    sendMessageOverWebSocket(userId, "INIT_SESSION")
                    isSessionInitialized = true
                }
                sendMessage(senderId = userId, contentType = "TEXT", content = message, conversationId = conversationId)
                addMessage(message, isFromOpponent = false)
                binding.etMessage.text.clear()
            }
        }

        // Handle "Attach File" button click
        binding.btnAttachFile.setOnClickListener {
            pickFileLauncher.launch("image/*")
        }

        // Handle "Camera" button click
        binding.btnCamera.setOnClickListener {
            val photoFile = File(requireContext().cacheDir, "photo.jpg")
            if (!photoFile.exists()) {
                try {
                    photoFile.createNewFile()
                } catch (e: Exception) {
                    Log.e("Camera", "Error creating photo file", e)
                }
            }

            imgUri = FileProvider.getUriForFile(
                requireContext(),
                "com.example.myapplication.fileprovider",
                photoFile
            )
            takePictureLauncher.launch(imgUri)
        }

        // Connect to WebSocket
        connectToWebSocket()
    }


    private fun connectToWebSocket() {
        lifecycleScope.launch {
            try {
                // Connect to WebSocket server
                session = client.webSocketSession(host = "157.245.156.156", port = 8082, path = "text")
                isSessionConnected = true
                Log.d("WebSocket", "Connected to WebSocket")

                // Listen for incoming messages
                while (true) {
                    val frame = session.incoming.receive()
                    when (frame) {
                        is Frame.Text -> {
                            val jsonMessage = frame.readText()
                            Log.d("WebSocket", "Message received: $jsonMessage")
                            if (jsonMessage != "Server recieved message") { handleIncomingMessage(jsonMessage) }
                        }
                        else -> {
                            Log.d("WebSocket", "Non-text frame received")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("WebSocket", "Error: ${e.localizedMessage}")
                isSessionConnected = false
            }
        }
    }


    private fun handleIncomingMessage(jsonMessage: String) {
        try {
            val message = gson.fromJson(jsonMessage, WebSocketMessage::class.java)
            Log.d("WebSocket", "Parsed message: $message")
            val senderName = userMapping[message.senderId] ?: "Unknown Sender"
            message.content?.let {
                addMessage(content = it, isFromOpponent = true, name = senderName)
            }
        } catch (e: Exception) {
            Log.e("WebSocket", "Error parsing incoming message: ${e.localizedMessage}")
        }
    }

    private fun sendMessage(senderId: Int, contentType: String, content: String? = null, conversationId: Int? = null) {
        // Check if the session is active
        if (this::session.isInitialized && session.isActive) {
            // If session is active, send the message normally
            Log.d("WebSocket", "session is active")
            sendMessageOverWebSocket(senderId, contentType, content, conversationId)
        } else {
            // If session is not active, send a message with "INIT_SESSION" and reconnect
            Log.d("WebSocket", "Session is inactive, reconnecting...")
            reconnectWebSocket() // Attempt to reconnect
            sendMessageOverWebSocket(senderId, "INIT_SESSION", content, conversationId)
        }
    }

    private fun sendMessageOverWebSocket(senderId: Int, contentType: String, content: String? = null, conversationId: Int? = null) {
        val message = WebSocketMessage(
            senderId = senderId,
            conversationId = conversationId,
            contentType = contentType,
            content = content
        )

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

    private fun reconnectWebSocket() {
        lifecycleScope.launch {
            try {
                session.close()
                session = client.webSocketSession(host = "157.245.156.156", port = 8082, path = "text")
                Log.d("WebSocket", "Reconnected to WebSocket")
            } catch (e: Exception) {
                Log.e("WebSocket", "Error reconnecting to WebSocket: ${e.localizedMessage}")
            }
        }
    }

    private fun addMessage(content: String, isFromOpponent: Boolean, name: String?="") {
        convs.add(Conv(content = content, isFromOpponent = isFromOpponent, senderName=name))
        messageAdapter.notifyItemInserted(convs.size - 1)
        binding.rvMessages.scrollToPosition(convs.size - 1)
    }

    private fun uploadFile(uri: Uri) {
        val filePath = getFilePath(requireContext(), uri)
        val file = File(filePath!!)

        if (!file.exists()) {
            Log.e("FileUpload", "File not found at path: $filePath")
            return
        }

        // Prepare the file for upload
        val requestBody = RequestBody.create(MediaType.parse("image/jpeg"), file)
        val filePart = MultipartBody.Part.createFormData("file", file.name, requestBody)
        val contentType = RequestBody.create(MediaType.parse("text/plain"), "IMAGE")
        val extension = RequestBody.create(MediaType.parse("text/plain"), "jpg")

        lifecycleScope.launch {
            try {
                val response = ApiRepository(RetrofitInstance.apiService).uploadFile(
                    cookie = "JSESSIONID=A35EAB74E5BF83D81A888CF6A6D51FB6",
                    contentType = contentType,
                    extension = extension,
                    file = filePart
                )

                if (response.success) {
                    val fileLink = response.link
                    sendMessage(senderId = userId, contentType = "IMAGE", content = fileLink, conversationId = conversationId)
                    addMessage(fileLink, isFromOpponent = false)
                } else {
                    Log.e("FileUpload", "File upload failed")
                }
            } catch (e: Exception) {
                Log.e("FileUpload", "Error uploading file: ${e.localizedMessage}")
            }
        }
    }


    private fun getFilePath(context: Context, uri: Uri): String? {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":")
            val type = split[0]

            var contentUri: Uri? = null
            if ("primary" == type) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            } else {
                // Handle other types (like cloud storage URIs, if needed)
            }

            val selection = "_id=?"
            val selectionArgs = arrayOf(split[1])

            return getDataColumn(context, contentUri, selection, selectionArgs)
        } else if (uri.scheme == "content") {
            return getDataFromContentUri(context, uri)
        } else if (uri.scheme == "file") {
            return uri.path
        }
        return null
    }

    private fun getDataFromContentUri(context: Context, uri: Uri): String? {
        // Try to open an InputStream from the content URI
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                // You can now read the InputStream or copy it to a file as needed
                val tempFile = File(context.cacheDir, "temp_image.jpg")
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                return tempFile.absolutePath  // Return the path to the temporary file
            }
        } catch (e: Exception) {
            Log.e("FileUpload", "Error accessing content URI: ${e.localizedMessage}")
        }
        return null
    }

    private fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        client.close()
    }

}