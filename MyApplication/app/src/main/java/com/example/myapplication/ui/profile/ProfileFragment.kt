package com.example.myapplication.ui.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val apiRepository = ApiRepository(RetrofitInstance.apiService)

    // ActivityResultLauncher for picking an image from the gallery
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            // Convert URI to File
            println(uri.toString())
            val file = uri.path?.let { File(it) }
            // Assuming you have the extension (you can extract from the URI or filename)
            val extension = "jpg"
            val part = file?.let { RequestBody.create(MediaType.parse("image/*"), it) }?.let {
                MultipartBody.Part.createFormData("file",
                    file.name, it
                )
            }

            // Get user ID from SharedPreferences
            val sharedPreferences = requireContext().getSharedPreferences("AuthPrefs", MODE_PRIVATE)
            val userId = sharedPreferences.getInt("userId", 1)

            // Call the API to update the avatar
            lifecycleScope.launch {
                try {
                    val response = part?.let { apiRepository.updateAvatar(userId, extension, it) }
                    if (response != null) {
                        if (response.success) {
                            // Update profile photo URL in shared preferences
                            sharedPreferences.edit().putString("profilePhoto", response.link).apply()

                            // Update UI with new avatar (use the link from the response)
                            Glide.with(requireContext())
                                .load(response.link) // Load the new image from S3
                                .into(binding.profileImage)
                        } else {
                            // Handle failure, e.g., show error message
                        }
                    }
                } catch (e: Exception) {
                    // Handle error (e.g., network issue)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val profileViewModel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Get user ID and phone number from SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("AuthPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("userId", 1)
        val phone = sharedPreferences.getString("phone", null)

        // Fetch user info from API
        lifecycleScope.launch {
            try {
                val userResponse = apiRepository.findUser(phone)

                // Find the user with the matching user_id
                val user = userResponse.content.find { it.id == userId }

                user?.let {
                    // Load profile photo if available, otherwise use default avatar
                    val profilePhotoUrl = it.profilePhoto
                    if (!profilePhotoUrl.isNullOrEmpty()) {
                        Glide.with(requireContext())
                            .load(profilePhotoUrl) // Load profile image from S3
                            .into(binding.profileImage)
                    } else {
                        Glide.with(requireContext())
                            .load(R.drawable.ic_avatar_default) // Default avatar
                            .into(binding.profileImage)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
