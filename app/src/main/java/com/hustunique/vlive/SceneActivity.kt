package com.hustunique.vlive

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.hustunique.vlive.controller.ARCoreController
import com.hustunique.vlive.databinding.ActivitySceneBinding
import com.hustunique.vlive.filament.FilamentCameraController
import com.hustunique.vlive.filament.model_object.SceneModelObject
import com.hustunique.vlive.filament.model_object.ScreenModelObject

class SceneActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySceneBinding.inflate(layoutInflater)
    }

    private val controller by lazy {
        FilamentCameraController(this)
    }

    lateinit var screenModelObject: ScreenModelObject

    private lateinit var arCoreHelper: ARCoreController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupStatusBar()

        binding.filamentView.apply {
            bindController(controller)
            screenModelObject = ScreenModelObject(windowManager.defaultDisplay)
            addModelObject(screenModelObject)
            addModelObject(SceneModelObject())
        }
        controller.bindControlView(
            binding.sceneLeft,
            binding.sceneRight,
            binding.sceneForward,
            binding.sceneBack,
            binding.sceneReset,
        )

        arCoreHelper = ARCoreController(this, Handler(), screenModelObject.surface)
    }


    override fun onResume() {
        super.onResume()
        arCoreHelper.resume()
    }

    override fun onPause() {
        super.onPause()
        arCoreHelper.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        arCoreHelper.release()
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