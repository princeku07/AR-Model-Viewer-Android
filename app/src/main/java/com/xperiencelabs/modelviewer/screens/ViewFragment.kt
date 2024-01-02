package com.xperiencelabs.modelviewer.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.xperiencelabs.modelviewer.R
import com.xperiencelabs.modelviewer.databinding.FragmentViewBinding
import kotlinx.coroutines.launch



class ViewFragment : Fragment() {
   private lateinit var binding: FragmentViewBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}