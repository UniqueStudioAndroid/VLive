package com.hustunique.vlive.filament

import android.view.MotionEvent
import android.view.View
import com.google.android.filament.Camera
import com.google.android.filament.utils.Float3
import com.google.android.filament.utils.GestureDetector
import com.google.android.filament.utils.Manipulator
import com.google.android.filament.utils.Utils

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 5/2/21
 */
class FilamentCameraController {

    companion object {
        init {
            Utils.init()
        }

        private val kDefaultObjectPosition = Float3(0.0f, 0.0f, -4.0f)
    }

    private val cameraManipulator = Manipulator.Builder()
        .targetPosition(
            kDefaultObjectPosition.x,
            kDefaultObjectPosition.y,
            kDefaultObjectPosition.z
        ).build(Manipulator.Mode.ORBIT)

    private var gestureDetector: GestureDetector? = null

    private val eyePos = DoubleArray(3)
    private val target = DoubleArray(3)
    private val upward = DoubleArray(3)

    fun bind(view: View) {
        gestureDetector = GestureDetector(view, cameraManipulator)
    }

    fun unbind() {
        gestureDetector = null
    }

    fun update(camera: Camera) {
        cameraManipulator.getLookAt(eyePos, target, upward)
        camera.lookAt(
            eyePos[0], eyePos[1], eyePos[2],
            target[0], target[1], target[2],
            upward[0], upward[1], upward[2]
        )
    }

    fun onTouchEvent(event: MotionEvent) {
        gestureDetector?.onTouchEvent(event)
    }

    fun resize(width: Int, height: Int) {
        cameraManipulator.setViewport(width, height)
    }

}