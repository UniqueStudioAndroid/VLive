package com.hustunique.vlive.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hustunique.vlive.data.Vector3

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 2021/5/25
 */
class SceneViewModel : ViewModel() {

    val eventData = MutableLiveData<BaseEvent>()

}

open class BaseEvent()

data class RockerEvent(
    val radians: Float,
    val progress: Float
) : BaseEvent()

data class ModeSwitchEvent(
    val rockerMode: Boolean
) : BaseEvent()

class ResetEvent() : BaseEvent()

data class FlyEvent(
    val uid: Int,
    val pos: Vector3
) : BaseEvent()