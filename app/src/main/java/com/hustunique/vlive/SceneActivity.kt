package com.hustunique.vlive

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.hustunique.vlive.agora.AgoraMessageModule
import com.hustunique.vlive.agora.AgoraModule
import com.hustunique.vlive.databinding.ActivitySceneBinding
import com.hustunique.vlive.filament.FilamentContext
import com.hustunique.vlive.filament.FilamentLocalController
import com.hustunique.vlive.filament.model_object.ActorModelObject
import com.hustunique.vlive.filament.model_object.SceneModelObject
import com.hustunique.vlive.filament.model_object.ScreenModelObject
import com.hustunique.vlive.local.GroupMemberManager
import com.hustunique.vlive.local.VirtualCharacterPropertyProvider
import com.hustunique.vlive.opengl.GLRender
import com.hustunique.vlive.ui.ChannelListActivity

class SceneActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SceneActivity"
    }

    private val binding by lazy {
        ActivitySceneBinding.inflate(layoutInflater)
    }

    private val localController by lazy {
        FilamentLocalController(this)
    }
    private val characterPropertyProvider by lazy {
        if (ChannelListActivity.videoMode) {
            VirtualCharacterPropertyProvider(this, localController::onCharacterPropertyReady)
        } else null
    }

    lateinit var screenModelObject: ScreenModelObject

    private lateinit var glRender: GLRender

    private lateinit var agoraModule: AgoraModule

    private val groupMemberManager = GroupMemberManager()

    private val agoraMessageModule by lazy {
        AgoraMessageModule(this, groupMemberManager::onMatrix, groupMemberManager::rtmModeChoose)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        glRender = GLRender().apply {
            init()
        }

        agoraModule =
            AgoraModule(this, groupMemberManager::rtcJoin, groupMemberManager::rtcQuit).apply {
                initAgora()
            }
//            screenModelObject = ScreenModelObject(glRender.getEglContext()?.nativeHandle ?: 0L)
//            agoraModule.setRemoteVideoRender(it, screenModelObject.videoConsumer)
//            binding.filamentView.addModelObject(screenModelObject)

        binding.filamentView.apply {
            filamentContext = FilamentContext(this, glRender.getEglContext())
            bindController(localController)

            addModelObject(ActorModelObject())
            addModelObject(SceneModelObject())
        }
        localController.bindControlView(binding.sceneReset)

    }

    override fun onDestroy() {
        super.onDestroy()
        agoraModule.destroyAgora()
        glRender.release()
        localController.release()
        characterPropertyProvider?.destroy()
    }

    override fun onResume() {
        super.onResume()
        characterPropertyProvider?.resume()
    }

    override fun onPause() {
        super.onPause()
        characterPropertyProvider?.pause()
    }
}