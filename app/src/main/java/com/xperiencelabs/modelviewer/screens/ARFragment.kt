package com.xperiencelabs.modelviewer.screens


import android.animation.ObjectAnimator
import android.app.Fragment
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.filament.utils.Float3
import com.google.android.filament.utils.HDRLoader
import com.google.android.filament.utils.scale
import com.google.android.material.chip.Chip
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Config.LightEstimationMode
import com.google.ar.core.Config.PlaneFindingMode
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.SceneView
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.RenderableInstance
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.gorisse.thomas.sceneform.environment
import com.gorisse.thomas.sceneform.environment.loadEnvironment
import com.gorisse.thomas.sceneform.light.LightEstimationConfig
import com.gorisse.thomas.sceneform.lightEstimationConfig
import com.gorisse.thomas.sceneform.scene.await
import com.xperiencelabs.modelviewer.R

import com.xperiencelabs.modelviewer.databinding.FragmentARBinding
import com.xperiencelabs.modelviewer.utils.toast
import java.io.FileNotFoundException


class ARFragment : Fragment()  {

     private val data:ARFragmentArgs by navArgs()
     private lateinit var binding:FragmentARBinding
     private lateinit var arFragment: ArFragment
     private val sceneView:ArSceneView get() = arFragment.arSceneView
     private val scene:Scene get() = sceneView.scene
     private var model:Renderable? = null
     private lateinit var animList:ListView
     private var currentAnimator:ObjectAnimator? = null
     private var anchorNode:AnchorNode? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentARBinding.inflate(layoutInflater)
        return  binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arFragment = (childFragmentManager.findFragmentById(R.id.arFragment) as ArFragment).apply {
             setOnSessionConfigurationListener { session,config->
                 config.lightEstimationMode = LightEstimationMode.ENVIRONMENTAL_HDR
                 config.planeFindingMode = PlaneFindingMode.HORIZONTAL_AND_VERTICAL

             }
            setOnViewCreatedListener { arSceneView->
                arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL)

            }



            setOnTapArPlaneListener(::onTapPlane)
        }
        animList = binding.animList

        lifecycleScope.launchWhenCreated {

            loadModels()
//            sceneView.environment = HDRLoader.loadEnvironment(requireContext(),"https://sceneview.github.io/assets/environments/evening_meadow_2k.hdr")
        }



    }

    private suspend fun loadModels(){
        try {
            model = ModelRenderable.builder()
                .setSource(context, Uri.parse(data.data))
                .setIsFilamentGltf(true)
                .await()
        } catch (e:FileNotFoundException){
            e.printStackTrace()
        }


    }

    private fun onTapPlane(hitResult: HitResult, plane: Plane, motionEvent: MotionEvent) {

        if(model == null){
            requireContext().toast("Loading..")
            return
        }

        if(anchorNode!=null){
            scene.removeChild(anchorNode)
            anchorNode = null
        }



        anchorNode = AnchorNode(hitResult.createAnchor()).apply {
            localScale = Vector3(0.5f,0.5f,0.5f)
            isEnabled = true

        }
        TransformableNode(arFragment.transformationSystem).apply {
            parent = anchorNode
            renderable = model

            renderable?.isShadowCaster = false
            renderableInstance.setCulling(false)
            this.select()



            if (renderableInstance.hasAnimations()){
                val anims = renderableInstance.animationNames
                binding.animList.visibility = View.VISIBLE
                currentAnimator = ModelAnimator.ofAnimation(renderableInstance,anims[0])
                currentAnimator?.start()
                manageAnimations(renderableInstance,anims)
            }
            manageVariants(renderableInstance)



        }

        scene.addChild(anchorNode)


    }

    private var selectedPosition = 0 // Initially, the first item is selected

    private fun manageAnimations(renderableInstance: RenderableInstance, anims: List<String>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, anims)

        animList.adapter = adapter

        // Set the background color of the initially selected item
        animList.post {
            animList.getChildAt(selectedPosition)?.setBackgroundResource(R.color.grey)
        }

        animList.setOnItemClickListener { _, view, position, l ->
            // Cancel the current animation if any
            currentAnimator?.cancel()

            // Reset the background color of the previously selected item
            animList.getChildAt(selectedPosition)?.setBackgroundResource(android.R.color.transparent)

            // Set the background color of the selected item
            view.setBackgroundResource(R.color.grey)

            // Update the selected position
            selectedPosition = position

            // Start the new animation
            val anim = ModelAnimator.ofAnimation(renderableInstance, anims[position])
            anim.start()

            // Update the current animator
            currentAnimator = anim
        }
    }


    private fun manageVariants(renderableInstance: RenderableInstance){
        renderableInstance.filamentAsset?.applyMaterialVariant(0)
        val variants = renderableInstance.filamentAsset?.materialVariantNames
        binding.variants.adapter = variants?.let {
            VariantsAdapter(requireContext(), it){position ->
                renderableInstance.filamentAsset?.applyMaterialVariant(position)
            }
        }

    }


}


