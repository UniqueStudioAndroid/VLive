package com.hustunique.vlive

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.hustunique.vlive.agora.AgoraModule
import com.hustunique.vlive.databinding.ActivitySceneBinding
import com.hustunique.vlive.filament.FilamentCameraController
import com.hustunique.vlive.filament.model_object.ScreenModelObject
import com.hustunique.vlive.local.LocalVideoConsumerStub
import com.hustunique.vlive.local.LocalVideoModule
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

//        glRender = GLRender().apply {
//            init()
//        }

//        agoraModule = AgoraModule(this, {
//            screenModelObject = ScreenModelObject(glRender.getEglContext()?.nativeHandle ?: 0L)
//            agoraModule.setRemoteVideoRender(it, screenModelObject.videoConsumer)
//            binding.filamentView.addModelObject(screenModelObject)
//        }).apply {
//            initAgora()
//        }

        localVideoModel = LocalVideoModule(this, LocalVideoConsumerStub(LocalVideoType.VIRTUAL))

//        binding.filamentView.apply {
//            filamentContext = FilamentContext(this, glRender.getEglContext())
//            bindController(controller)
//
//            addModelObject(SceneModelObject())
//        }
//        controller.bindControlView(binding.sceneReset)
    }

    override fun onDestroy() {
        super.onDestroy()
//        agoraModule.destroyAgora()
//        glRender.release()
//        controller.release()
    }
}