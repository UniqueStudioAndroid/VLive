package com.hustunique.vlive.local

import android.util.Log
import android.util.SparseArray
import com.hustunique.vlive.agora.AgoraModule
import com.hustunique.vlive.filament.model_object.ActorModelObject
import com.hustunique.vlive.filament.model_object.FilamentBaseModelObject
import com.hustunique.vlive.filament.model_object.ScreenModelObject
import com.hustunique.vlive.util.ThreadUtil
import com.hustunique.vlive.util.putIfAbsent

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 2021/5/16
 */
class GroupMemberManager(
    private val addObj: (FilamentBaseModelObject) -> Unit,
    private val removeObj: (FilamentBaseModelObject) -> Unit
) {

    companion object {
        private const val TAG = "GroupMemberManager"
    }

    var agoraModule: AgoraModule? = null

    private val memberInfo = SparseArray<MemberInfo>()

    @Synchronized
    fun rtcJoin(uid: Int) {
        memberInfo.putIfAbsent(uid, MemberInfo())
        memberInfo.get(uid).apply { rtcJoined = true }
    }

    @Synchronized
    fun rtcQuit(uid: Int) {
        memberInfo.get(uid)?.apply {
            rtcJoined = false
            modelObject?.run(removeObj)
        }
        Log.i(TAG, "rtcQuit: removeObj $uid")
        memberInfo.remove(uid)
    }

    @Synchronized
    fun rtmModeChoose(video: Boolean, uid: Int) {
        Log.i(TAG, "rtmModeChoose() called with: video = $video, uid = $uid")
        memberInfo.putIfAbsent(uid, MemberInfo())
        memberInfo.get(uid).apply { videoMode = video }
    }

    fun onMatrix(characterProperty: CharacterProperty, uid: Int) {
        memberInfo.get(uid)?.apply {
            ThreadUtil.execUi {
                if (rtcJoined && videoMode != null && modelObject == null) {
                    Log.i(TAG, "addModelObject: $uid")
                    modelObject =
                        (if (videoMode == true) ScreenModelObject().apply {
                            agoraModule?.setRemoteVideoRender(
                                uid,
                                videoConsumer
                            )
                        } else ActorModelObject()).also(addObj)
                }
                modelObject?.onProperty(characterProperty)
            }
        }
    }

}

data class MemberInfo(
    var videoMode: Boolean? = null,
    var rtcJoined: Boolean = false,
    var modelObject: FilamentBaseModelObject? = null,
    var onMatrix: (CharacterProperty) -> Unit = {}
)