package com.xperiencelabs.modelviewer.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.navigation.fragment.findNavController
import com.xperiencelabs.modelviewer.R
import com.xperiencelabs.modelviewer.databinding.FragmentHomeBinding
import com.xperiencelabs.modelviewer.utils.toast
import java.io.File
import java.util.Locale



class HomeFragment : Fragment() {
    private lateinit var binding:FragmentHomeBinding
    private var modelUrl:String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val pickModel = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if(result.resultCode == Activity.RESULT_OK){
                result.data?.data?.let {
                    val modelPath = uriToFile(it)
                    if(modelPath!=null){
                        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToARFragment(modelPath))
                    }

                }
            }
        }


        binding.modelUrl.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
               val newText = s.toString().trim()

                if(newText.isNotEmpty()){
                    modelUrl = newText
                   binding.view.visibility = View.VISIBLE
                } else {
                    binding.view.visibility = View.INVISIBLE
                }
            }
        })

        binding.view.setOnClickListener {
           modelUrl?.let { url->
               if(isValidModelUrl(url)){
                   findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToARFragment(url))
               }
               else{
                   requireContext().toast("Enter Valid Url")
               }
           }
        }

         binding.choose.setOnClickListener {
             val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                 type="*/*"
             }
           pickModel.launch(intent)
         }
    }




    private fun uriToFile(uri:Uri):String? {
        val contentResolver = requireContext().contentResolver
        val inputStream = contentResolver.openInputStream(uri)
        val file = File(requireContext().cacheDir,"temp_model.glb")

        inputStream?.use { input->
            file.outputStream().use { output->
                input.copyTo(output)
            }

        }
        return file.absolutePath
    }

    private fun isValidModelUrl(url:String):Boolean{
        val lowerCaseUrl = url.lowercase(Locale.ROOT)
        return (lowerCaseUrl.endsWith(".glb") || lowerCaseUrl.endsWith(".gltf")) && lowerCaseUrl.startsWith("https")
    }



}