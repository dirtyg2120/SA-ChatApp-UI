package com.example.myapplication.ui.profile

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.api.ApiRepository
import com.example.myapplication.api.RetrofitInstance
import com.example.myapplication.databinding.FragmentProfileBinding
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val apiRepository = ApiRepository(RetrofitInstance.apiService)
    private var isEditing = false

    // ActivityResultLauncher for picking an image from the gallery
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val filePath = getFilePath(requireContext(), uri)
            val file = File(filePath.toString())
            // Assuming you have the extension (you can extract from the URI or filename)
            val extension = "jpg"
            val requestBody = RequestBody.create(MediaType.parse("image/jpg"), file)
            val part = MultipartBody.Part.createFormData("file", file.name, requestBody)

            // Get user ID from SharedPreferences
            val sharedPreferences = requireContext().getSharedPreferences("AuthPrefs", MODE_PRIVATE)
            val userId = sharedPreferences.getInt("userId", 1)

            // Call the API to update the avatar
            lifecycleScope.launch {
                try {
                    val response = part.let { apiRepository.updateAvatar(userId, extension, it) }
                    if (response.success) {
                        sharedPreferences.edit().putString("profilePhoto", response.link).apply()

                        Glide.with(requireContext())
                            .load(response.link).dontTransform()
                            .into(binding.profileImage)
                    } else {
                        // Handle failure, e.g., show error message
                    }
                } catch (e: Exception) {
                    println(e)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val phoneEditText: EditText = binding.phoneEditText
        val emailEditText: EditText = binding.emailEditText

        // Set the "Edit Profile" button listener
        binding.editProfileButton.setOnClickListener {
            if (isEditing) {
                saveProfileChanges(phoneEditText.text.toString(), emailEditText.text.toString())
                setEditMode(false)
            } else {
                // Enable editing
                setEditMode(true)
            }
        }
        initializeProfileFields()

        // Get user ID and phone number from SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("AuthPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("userId", 1)
        val phone = sharedPreferences.getString("phone", null)

        // Fetch user info from API
        lifecycleScope.launch {
            try {
                val userResponse = apiRepository.findUser(phone)
                val user = userResponse.content.find { it.id == userId }

                user?.let {
                    // Load profile photo if available, otherwise use default avatar
                    val profilePhotoUrl = it.profilePhoto
                    if (!profilePhotoUrl.isNullOrEmpty()) {
                        Glide.with(requireContext())
                            .load(profilePhotoUrl).dontTransform()
                            .into(binding.profileImage)
                        Glide.with(requireContext())
                            .load(profilePhotoUrl).dontTransform()
                            .into(requireActivity().findViewById<ShapeableImageView>(R.id.imageAvaView))
                    } else {
                        Glide.with(requireContext())
                            .load(R.drawable.ic_avatar_default)
                            .into(binding.profileImage)
                        Glide.with(requireContext())
                            .load(R.drawable.ic_avatar_default)
                            .into(requireActivity().findViewById<ShapeableImageView>(R.id.imageAvaView))
                    }
                } ?: run {
                    // Handle case where user is not found (e.g., show an error message)
                }
            } catch (e: Exception) {
                // Handle API error (e.g., network issue)
            }
        }


        // Handle click to select a new profile image
        binding.profileImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        return root
    }

    private fun getFilePath(context: Context, uri: Uri): String? {
        // Check if the URI is a document URI (i.e., the file is in external storage)
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // Get the document ID
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":")
            val type = split[0]

            var contentUri: Uri? = null
            if ("primary" == type) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            } else {
                // Handle other types (like cloud storage URIs)
            }

            val selection = "_id=?"
            val selectionArgs = arrayOf(split[1])

            return getDataColumn(context, contentUri, selection, selectionArgs)
        } else if ("content" == uri.scheme) {
            // Handle content URIs (e.g., images in profile)
            return getDataColumn(context, uri, null, null)
        } else if ("file" == uri.scheme) {
            // Handle file URIs (direct paths)
            return uri.path
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
    }

    private fun initializeProfileFields() {
        // Get user data from SharedPreferences or API and populate fields
        val sharedPreferences = requireContext().getSharedPreferences("AuthPrefs", MODE_PRIVATE)
        val phone = sharedPreferences.getString("phone", "")
        val email = sharedPreferences.getString("email", "")

        binding.phoneEditText.setText(phone)
        binding.emailEditText.setText(email)
    }

    private fun setEditMode(isEditing: Boolean) {
        // Toggle EditText enable/disable state
        binding.phoneEditText.isEnabled = isEditing
        binding.emailEditText.isEnabled = isEditing

        // Toggle focusable state
        binding.phoneEditText.isFocusable = isEditing
        binding.phoneEditText.isFocusableInTouchMode = isEditing
        binding.emailEditText.isFocusable = isEditing
        binding.emailEditText.isFocusableInTouchMode = isEditing

        // Update button text
        if (isEditing) {
            binding.editProfileButton.text = "Save Changes"
        } else {
            binding.editProfileButton.text = "Edit Profile"
        }

        // Update editing flag
        this.isEditing = isEditing
    }

    private fun saveProfileChanges(phone: String, email: String) {
        // Implement saving profile changes, e.g., call an API to update the user's profile
        val sharedPreferences = requireContext().getSharedPreferences("AuthPrefs", MODE_PRIVATE)
        sharedPreferences.edit().putString("phone", phone).putString("email", email).apply()

//        // Optionally, call your API to save changes
//        lifecycleScope.launch {
//            try {
//                val userId = sharedPreferences.getInt("userId", 1)
//                val response = apiRepository.updateUserProfile(userId, phone, email)
//                if (response.success) {
//                    // Handle successful update
//                    Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
//                } else {
//                    // Handle error
//                    Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
//                }
//            } catch (e: Exception) {
//                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//        }
    }
}
