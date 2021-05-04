package com.hustunique.vlive.filament

import android.content.Context
import android.hardware.SensorManager
import android.view.MotionEvent
import android.view.View
import com.google.android.filament.Camera
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

        private const val MOVE_DELTA = 1f
        private const val TAG = "FilamentCameraController"

        private val UPWARD = Vector3(y = 1f)
        private val FRONT = Vector3(z = -1f)
//        private val RIGHT = Vector3(x = 1f)
    }

//    private val cameraManipulator = Manipulator.Builder()
//        .targetPosition(
//            kDefaultObjectPosition.x,
//            kDefaultObjectPosition.y,
//            kDefaultObjectPosition.z
//        ).build(Manipulator.Mode.ORBIT)

//    private var gestureDetector: GestureDetector? = null
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

    fun bindControlView(left: View, right: View, forward: View, back: View, reset: View) {
        left.setOnClickListener { onMove(MoveType.LEFT) }
        right.setOnClickListener { onMove(MoveType.RIGHT) }
        forward.setOnClickListener { onMove(MoveType.FORWARD) }
        back.setOnClickListener { onMove(MoveType.BACK) }
        reset.setOnClickListener { resetCalibration() }
    }

    fun update(camera: Camera) {
        // calculate rotation matrix
        SensorManager.getRotationMatrixFromVector(rotationMatrix, angleHandler.rotationVector)
        // compute camera Front direction and up direction
        cameraFront.clone(FRONT)
            .applyL(rotationMatrix)
            .applyL(baseMatrix)
        cameraUP.clone(UPWARD)
            .applyL(rotationMatrix)
            .applyL(baseMatrix)
        cameraTarget.addAssign(cameraFront, cameraPos)
        camera.lookAt(
            cameraPos.x.toDouble(), cameraPos.y.toDouble(), cameraPos.z.toDouble(),
            cameraTarget.x.toDouble(), cameraTarget.y.toDouble(), cameraTarget.z.toDouble(),
            cameraUP.x.toDouble(), cameraUP.y.toDouble(), cameraUP.z.toDouble()
        )
    }

    private fun onMove(type: MoveType) {
        when (type) {
            MoveType.FORWARD -> cameraPos.add(cameraFront, MOVE_DELTA)
            MoveType.BACK -> cameraPos.sub(cameraFront, MOVE_DELTA)
            MoveType.LEFT -> {
                tempVector.crossAssign(cameraFront, cameraUP)
                cameraPos.sub(tempVector, MOVE_DELTA)
            }
            MoveType.RIGHT -> {
                tempVector.crossAssign(cameraFront, cameraUP)
                cameraPos.add(tempVector, MOVE_DELTA)
            }
        }
    }

    private fun resetCalibration() {
        SensorManager.getRotationMatrixFromVector(baseMatrix, angleHandler.rotationVector)
        baseMatrix.transpose()
    }

    enum class MoveType {
        FORWARD, BACK, LEFT, RIGHT
    }

    fun bind(view: View) {
    }

    fun onTouchEvent(event: MotionEvent) {
//        gestureDetector?.onTouchEvent(event)
    }

    fun resize(width: Int, height: Int) {
//        cameraManipulator.setViewport(width, height)
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