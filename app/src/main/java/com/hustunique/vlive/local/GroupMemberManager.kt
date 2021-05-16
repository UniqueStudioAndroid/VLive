package com.hustunique.vlive.local

import android.util.SparseArray
import com.hustunique.vlive.util.putIfAbsent

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 2021/5/16
 */
class GroupMemberManager {

    private val memberInfo = SparseArray<MemberInfo>()

    fun rtcJoin(uid: Int) {
        memberInfo.putIfAbsent(uid, MemberInfo())
        memberInfo.get(uid).apply { rtcJoined = true }
    }

    fun rtcQuit(uid: Int) {
        memberInfo.get(uid)?.apply { rtcJoined = false }
    }

    fun rtmModeChoose(video: Boolean, uid: Int) {
        memberInfo.putIfAbsent(uid, MemberInfo())
        memberInfo.get(uid).apply { videoMode = video }
    }

    fun onMatrix(characterProperty: CharacterProperty, uid: Int) {
        memberInfo.get(uid)?.onMatrix?.invoke(characterProperty)
    }

}

data class MemberInfo(
    var videoMode: Boolean = false,
    var rtcJoined: Boolean = false,
    var objAdded: Boolean = false,
    var onMatrix: (CharacterProperty) -> Unit = {}
)