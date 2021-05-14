package com.hustunique.vlive.local

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class LocalVideoModule(
    context: AppCompatActivity,
    localVideoSink: LocalVideoSink,
): DefaultLifecycleObserver {
    private var characterPropertyProvider: VirtualCharacterPropertyProvider? = null
    private var cameraFrameProvider: CameraFrameProvider? = null

    init {
        if (localVideoSink.getConsumeType() == LocalVideoType.REALITY) {
            cameraFrameProvider = CameraFrameProvider(context, localVideoSink)
        } else {
            characterPropertyProvider = VirtualCharacterPropertyProvider(context, localVideoSink)
        }
        context.lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        characterPropertyProvider?.resume()
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        characterPropertyProvider?.pause()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        characterPropertyProvider?.destroy()
        cameraFrameProvider?.destroy()
    }
}