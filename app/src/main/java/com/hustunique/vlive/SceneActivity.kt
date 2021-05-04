package com.hustunique.vlive

import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.hustunique.vlive.databinding.ActivitySceneBinding
import com.hustunique.vlive.filament.FilamentCameraController
import com.hustunique.vlive.filament.model_object.SceneModelObject
import com.hustunique.vlive.filament.model_object.VFaceModelObject
import com.hustunique.vlive.util.readCompressedAsset

class SceneActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySceneBinding.inflate(layoutInflater)
    }

    private val controller by lazy {
        FilamentCameraController(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupStatusBar()

        binding.filamentView.apply {
            bindController(controller)
            addModelObject(VFaceModelObject())
            addModelObject(SceneModelObject())
        }
        controller.bindControlView(
            binding.sceneLeft,
            binding.sceneRight,
            binding.sceneForward,
            binding.sceneBack,
            binding.sceneReset,
        )
    }

    private fun setupStatusBar() {
        window.run {
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
            navigationBarColor = Color.TRANSPARENT
        }
    }
}