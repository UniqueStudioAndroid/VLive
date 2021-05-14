package com.hustunique.vlive.filament

import android.content.Context
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import com.google.android.filament.Camera
import com.google.android.filament.utils.Float3
import com.google.android.filament.utils.Utils
import com.hustunique.vlive.data.Vector3
import com.hustunique.vlive.util.AngleHandler

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 5/2/21
 */
class FilamentCameraController(
    context: Context
) {

    companion object {
        init {
            Utils.init()
        }

        val kDefaultObjectPosition = Float3(0.0f, 0.0f, -4.0f)
        private const val MOVE_DELTA = 1f
        private const val MOVE_PER_MS_BASE = 0.01f
        private const val TAG = "FilamentCameraController"

        private val UPWARD = Vector3(y = 1f)
        private val FRONT = Vector3(z = -1f)
//        private val RIGHT = Vector3(x = 1f)
    }

    private val angleHandler = AngleHandler(context).apply { start() }
    private val baseMatrix = FloatArray(9).apply {
        this[0] = 1f
        this[4] = 1f
        this[8] = 1f
    }
    private val rotationMatrix = FloatArray(9)

    private val cameraPos = Vector3()
    private val cameraFront = Vector3(z = -1f)
    private val cameraUP = Vector3(y = 1f)
    private val cameraTarget = Vector3()
    private val tempVector = Vector3()

    fun release() {
        angleHandler.stop()
    }

    fun bindControlView(reset: View) {
        reset.setOnClickListener { resetCalibration() }
    }

    fun update(camera: Camera) {
        // calculate rotation matrix
        angleHandler.getRotationMatrix(rotationMatrix)
        // compute camera's front direction after rotation
        cameraFront.clone(FRONT)
            .applyL(rotationMatrix)
            .applyL(baseMatrix)
        // compute camera's up direction after rotation
        cameraUP.clone(UPWARD)
            .applyL(rotationMatrix)
            .applyL(baseMatrix)
        // compute forward step & update last update time
        computeWalk()
        // recompute camera's lookAt matrix
        cameraTarget.addAssign(cameraFront, cameraPos)
        camera.lookAt(
            cameraPos.x.toDouble(), cameraPos.y.toDouble(), cameraPos.z.toDouble(),
            cameraTarget.x.toDouble(), cameraTarget.y.toDouble(), cameraTarget.z.toDouble(),
            cameraUP.x.toDouble(), cameraUP.y.toDouble(), cameraUP.z.toDouble()
        )
    }

    private var lastUpdateTime = 0L
    private fun computeWalk() {
        val now = System.currentTimeMillis()
        if (lastUpdateTime > 0 && isSelected) {
            val delta = (now - lastUpdateTime) * MOVE_PER_MS_BASE
            onMove(MoveDirType.PLANE_FORWARD, delta)
        }
        lastUpdateTime = now
    }

    private fun onMove(type: MoveDirType, delta: Float = MOVE_DELTA) {
        when (type) {
            MoveDirType.FORWARD -> cameraPos.add(cameraFront, delta)
            MoveDirType.BACK -> cameraPos.sub(cameraFront, delta)
            MoveDirType.LEFT -> {
                tempVector.crossAssign(cameraFront, cameraUP)
                cameraPos.sub(tempVector, delta)
            }
            MoveDirType.RIGHT -> {
                tempVector.crossAssign(cameraFront, cameraUP)
                cameraPos.add(tempVector, delta)
            }
            MoveDirType.UP -> cameraPos.add(cameraUP, delta)
            MoveDirType.DOWN -> cameraPos.sub(cameraUP, delta)
            MoveDirType.PLANE_FORWARD -> {
                tempVector.clone(cameraFront)
                tempVector.y = 0f
                tempVector.normalize()
                cameraPos.add(tempVector, delta)
            }
        }
    }

    private fun resetCalibration() {
        angleHandler.getRotationMatrix(baseMatrix)
        baseMatrix.transpose()
    }

    enum class MoveDirType {
        FORWARD, BACK, LEFT, RIGHT, UP, DOWN, PLANE_FORWARD
    }

    private var isSelected = false
    fun onTouchEvent(event: MotionEvent) {
        when (event.actionMasked) {
            ACTION_DOWN -> isSelected = true
            ACTION_UP, ACTION_CANCEL -> isSelected = false
        }
    }
}

private fun FloatArray.transpose() {
    if (size == 9) {
        // 0 1 2
        // 3 4 5
        // 6 7 8
        val temp1 = this[1]
        val temp2 = this[2]
        val temp5 = this[5]
        this[1] = this[3]
        this[2] = this[6]
        this[5] = this[7]
        this[3] = temp1
        this[6] = temp2
        this[7] = temp5
    }
}