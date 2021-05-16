package com.hustunique.vlive.agora

import android.content.Context
import android.util.Log
import com.hustunique.vlive.R
import com.hustunique.vlive.local.CharacterProperty
import io.agora.rtm.*

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 2021/5/15
 */
class AgoraMessageModule(
    context: Context,
    private val posMessageCallback: (CharacterProperty, Int) -> Unit,
    private val modeMessageCallback: (Boolean, Int) -> Unit
) {

    companion object {
        private const val TAG = "AgoraMessageModule"
    }

    private var rtmClient: RtmClient? = null

    private var rtmChannel: RtmChannel? = null

    var loginSuccess = false
        private set

    var joinChannelSuccess = false
        private set

    private val rtmClientListener = object : RtmClientListener {
        override fun onConnectionStateChanged(p0: Int, p1: Int) {

        }

        override fun onMessageReceived(p0: RtmMessage?, p1: String?) {
        }

        override fun onImageMessageReceivedFromPeer(p0: RtmImageMessage?, p1: String?) {
        }

        override fun onFileMessageReceivedFromPeer(p0: RtmFileMessage?, p1: String?) {
        }

        override fun onMediaUploadingProgress(p0: RtmMediaOperationProgress?, p1: Long) {
        }

        override fun onMediaDownloadingProgress(p0: RtmMediaOperationProgress?, p1: Long) {
        }

        override fun onTokenExpired() {
        }

        override fun onPeersOnlineStatusChanged(p0: MutableMap<String, Int>?) {
        }

    }

    private val rtmChannelListener = object : RtmChannelListener {
        override fun onMemberCountUpdated(p0: Int) {

        }

        override fun onAttributesUpdated(p0: MutableList<RtmChannelAttribute>?) {
        }

        override fun onMessageReceived(p0: RtmMessage?, p1: RtmChannelMember?) {
            if (p0 == null) {
                return
            }
            if (p0.messageType == 2) {
                val msg = CharacterProperty.fromArray(p0.rawMessage)
                Log.i(TAG, "onMessageReceived: $msg")
                posMessageCallback(msg, p1?.userId?.toIntOrNull() ?: 0)
            } else {
                modeMessageCallback(p0.text == "video", p1?.userId?.toIntOrNull() ?: 0)
            }
        }

        override fun onImageMessageReceived(p0: RtmImageMessage?, p1: RtmChannelMember?) {
        }

        override fun onFileMessageReceived(p0: RtmFileMessage?, p1: RtmChannelMember?) {
        }

        override fun onMemberJoined(p0: RtmChannelMember?) {
        }

        override fun onMemberLeft(p0: RtmChannelMember?) {
        }

    }

    init {
        try {
            rtmClient = RtmClient.createInstance(
                context,
                context.getString(R.string.agora_app_id),
                rtmClientListener
            ).apply {
                login(null, AgoraModule.MUID.toString(), object : ResultCallback<Void> {
                    override fun onSuccess(p0: Void?) {
                        loginSuccess = true
                        Log.i(TAG, "onSuccess: login")
                        joinChannel()
                    }

                    override fun onFailure(p0: ErrorInfo?) {
                        Log.i(TAG, "onFailure: login ${p0?.toString()}")
                    }
                })
            }
        } catch (e: Exception) {
            Log.e(TAG, ": create rtm client error", e)
        }

    }

    fun sendMessage(msg: CharacterProperty) {
        if (!joinChannelSuccess) {
            Log.i(TAG, "sendMessage: no channel now")
            return
        }
        rtmClient?.createMessage()?.apply {
            rawMessage = msg.toByteArray()
            rtmChannel?.sendMessage(this, object : ResultCallback<Void> {
                override fun onSuccess(p0: Void?) {
                    Log.d(TAG, "sendMessage: $msg")
                }

                override fun onFailure(p0: ErrorInfo?) {
                    Log.e(TAG, "onFailure: ${p0.toString()}")
                }

            })
        }
    }

    fun sendMessage(isVideo: Boolean) {
        if (!joinChannelSuccess) {
            Log.i(TAG, "sendMessage: no channel now")
            return
        }
        rtmClient?.createMessage(if (isVideo) "video" else "virtual")?.apply {
            rtmChannel?.sendMessage(this, object : ResultCallback<Void> {
                override fun onSuccess(p0: Void?) {
                    Log.d(TAG, "sendMessage: $isVideo")
                }

                override fun onFailure(p0: ErrorInfo?) {
                    Log.e(TAG, "onFailure: ${p0.toString()}")
                }

            })
        }
    }

    private fun joinChannel() {
        try {
            rtmChannel = rtmClient?.createChannel("test1", rtmChannelListener)?.apply {
                join(object : ResultCallback<Void> {
                    override fun onSuccess(p0: Void?) {
                        joinChannelSuccess = true
                        Log.i(TAG, "onSuccess: join channel")
                    }

                    override fun onFailure(p0: ErrorInfo?) {
                        Log.e(TAG, "onFailure: join channel ${p0?.toString()}")
                    }
                })
            }
        } catch (e: Exception) {
            Log.e(TAG, "joinChannel: join channel error", e)
        }
    }
}