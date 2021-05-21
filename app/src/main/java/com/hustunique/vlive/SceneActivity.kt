package com.hustunique.vlive

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hustunique.vlive.agora.AgoraMessageModule
import com.hustunique.vlive.agora.AgoraModule
import com.hustunique.vlive.databinding.ActivitySceneBinding
import com.hustunique.vlive.filament.FilamentContext
import com.hustunique.vlive.filament.FilamentLocalController
import com.hustunique.vlive.filament.model_object.SceneModelObject
import com.hustunique.vlive.filament.model_object.ScreenModelObject
import com.hustunique.vlive.local.GroupMemberManager
import com.hustunique.vlive.local.VirtualCharacterPropertyProvider
import com.hustunique.vlive.opengl.GLRender
import com.hustunique.vlive.remote.Service
import com.hustunique.vlive.ui.ChannelListActivity
import com.hustunique.vlive.util.ToastUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SceneActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SceneActivity"

        private const val CHANNEL_ID_STR = "channel_id"

        fun startActivity(context: Context, channelId: String) {
            context.startActivity(Intent(context, SceneActivity::class.java).apply {
                putExtra(CHANNEL_ID_STR, channelId)
            })
        }
    }

    private val channelId by lazy { intent.getStringExtra(CHANNEL_ID_STR) ?: "" }

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

    private val groupMemberManager by lazy {
        GroupMemberManager(
            binding.filamentView::addModelObject,
            binding.filamentView::removeModelObject
        )
    }

    private val agoraMessageModule by lazy {
        AgoraMessageModule(
            this,
            channelId,
            groupMemberManager::onMatrix,
            groupMemberManager::rtmModeChoose
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        localController.onUpdate = agoraMessageModule::sendMessage

        glRender = GLRender().apply {
            init()
        }

        agoraModule =
            AgoraModule(this, groupMemberManager::rtcJoin, groupMemberManager::rtcQuit).apply {
                initAgora()
                joinChannel(channelId)
            }.also { groupMemberManager.agoraModule = it }

        binding.filamentView.apply {
            filamentContext = FilamentContext(this, glRender.getEglContext())
            bindController(localController)

//            addModelObject(ActorModelObject())
            addModelObject(SceneModelObject())
        }
        localController.bindControlView(binding.sceneReset)
        enterRoom()
    }

    private fun enterRoom() {
        lifecycleScope.launchWhenCreated {
            Service.channelJoin(channelId).apply {
                if (!successful) {
                    ToastUtil.makeShort("加入房间失败")
                    finish()
                    return@apply
                }
                data?.let {
                    it.memberList.forEach {
                        groupMemberManager.rtmModeChoose(it.videoMode, it.uid.toIntOrNull() ?: 0)
                    }
                }
            }
        }
    }

    private fun leaveRoom() {
        GlobalScope.launch {
            Service.channelLeave(channelId)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        leaveRoom()
        agoraModule.destroyAgora()
        agoraMessageModule.release()
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