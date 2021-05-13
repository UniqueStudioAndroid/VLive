package com.hustunique.vlive

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.hustunique.vlive.agora.AgoraModule
import com.hustunique.vlive.controller.MLKitController
import com.hustunique.vlive.databinding.ActivitySceneBinding
import com.hustunique.vlive.filament.FilamentCameraController
import com.hustunique.vlive.filament.FilamentContext
import com.hustunique.vlive.filament.model_object.SceneModelObject
import com.hustunique.vlive.filament.model_object.ScreenModelObject
import com.hustunique.vlive.opengl.GLRender
import com.hustunique.vlive.opengl.LocalFrameManager

class SceneActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SceneActivity"
    }

    private val binding by lazy {
        ActivitySceneBinding.inflate(layoutInflater)
    }

    private val controller by lazy {
        FilamentCameraController(this)
    }

    lateinit var screenModelObject: ScreenModelObject

    private val localFrameManager = LocalFrameManager().apply {
        init()
    }

//    private lateinit var arCoreHelper: ARCoreController

    private lateinit var glRender: GLRender

    private lateinit var agoraModule: AgoraModule

    private val mlKit: MLKitController = MLKitController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        glRender = GLRender().apply {
            init()
        }

        agoraModule = AgoraModule(this, {
            screenModelObject = ScreenModelObject(glRender.getEglContext()?.nativeHandle ?: 0L)
            agoraModule.setRemoteVideoRender(it, screenModelObject.videoConsumer)
            binding.filamentView.addModelObject(screenModelObject)
        }).apply {
            initAgora()
        }

        binding.filamentView.apply {
            filamentContext = FilamentContext(this, glRender.getEglContext())
            bindController(controller)

            addModelObject(SceneModelObject())
        }
        controller.bindControlView(binding.sceneReset)

        localFrameManager.onImage = {
            Log.i(TAG, "onCreate: onimage")
            mlKit.process(it)
        }

//        arCoreHelper = ARCoreController(this, localFrameManager)
    }


    override fun onResume() {
        super.onResume()
//        arCoreHelper.resume()
    }

    override fun onPause() {
        super.onPause()
//        arCoreHelper.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        agoraModule.destroyAgora()
//        arCoreHelper.release()
        localFrameManager.release()
    }

}