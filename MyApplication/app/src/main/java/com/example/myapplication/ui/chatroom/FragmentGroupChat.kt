package com.example.myapplication.ui.chatroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentGroupChatBinding
import com.example.myapplication.R

class FragmentGroupChat : Fragment(R.layout.fragment_group_chat) {

    private lateinit var binding: FragmentGroupChatBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentGroupChatBinding.bind(view)

        binding.btnCreateGroup.setOnClickListener {
            Toast.makeText(requireContext(), "Group Chat Created", Toast.LENGTH_SHORT).show()
        }
    }
}