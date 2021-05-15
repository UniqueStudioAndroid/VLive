package com.hustunique.vlive

import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.hustunique.vlive.agora.AgoraMessageModule
import com.hustunique.vlive.agora.AgoraModule
import com.hustunique.vlive.databinding.ActivitySceneBinding
import com.hustunique.vlive.filament.FilamentCameraController
import com.hustunique.vlive.filament.FilamentContext
import com.hustunique.vlive.filament.model_object.SceneModelObject
import com.hustunique.vlive.filament.model_object.ScreenModelObject
import com.hustunique.vlive.local.CharacterProperty
import com.hustunique.vlive.local.LocalVideoModule
import com.hustunique.vlive.local.LocalVideoSink
import com.hustunique.vlive.local.LocalVideoType
import com.hustunique.vlive.opengl.GLRender

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

    private lateinit var glRender: GLRender

    private lateinit var agoraModule: AgoraModule

    private lateinit var localVideoModel: LocalVideoModule

    private val agoraMessageModule by lazy {
        AgoraMessageModule(this) {
            Log.i(TAG, "onMessage: $it")
        }
    }

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

        localVideoModel = LocalVideoModule(this, object : LocalVideoSink {
            override fun onFrame(image: Image) {
            }

            override fun onPropertyReady(property: CharacterProperty) {
                agoraMessageModule.sendMessage(property)
            }

            override fun getConsumeType(): LocalVideoType = LocalVideoType.VIRTUAL
        })

        binding.filamentView.apply {
            filamentContext = FilamentContext(this, glRender.getEglContext())
            bindController(controller)

            addModelObject(SceneModelObject())
        }
        controller.bindControlView(binding.sceneReset)
    }

    override fun onDestroy() {
        super.onDestroy()
        agoraModule.destroyAgora()
        glRender.release()
        controller.release()
    }
}