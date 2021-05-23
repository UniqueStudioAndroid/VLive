package com.hustunique.vlive.filament

import android.content.Context
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import com.google.android.filament.Camera
import com.google.android.filament.utils.Float3
import com.google.android.filament.utils.Utils
import com.hustunique.vlive.data.Quaternion
import com.hustunique.vlive.data.Vector3
import com.hustunique.vlive.local.CharacterProperty
import com.hustunique.vlive.util.AngleHandler
import java.nio.FloatBuffer

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 5/2/21
 */
class FilamentLocalController(
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
        private const val TRANSMIT_DATA_SIZE = 12 * 4

        private val UPWARD = Vector3(y = 1f)
        private val FRONT = Vector3(z = -1f)
//        private val RIGHT = Vector3(x = 1f)
    }

    var onUpdate: ((CharacterProperty) -> Unit)? = null

    private val angleHandler = AngleHandler(context).apply { start() }
    private var baseRotation = Quaternion()
    private val rotationMatrix = FloatArray(9)
    private val rotationBuffer = FloatBuffer.allocate(7)

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

    fun resetPosition(v: Vector3) {
        cameraPos.clone(v)
    }

    fun update(camera: Camera) {
        // calculate rotation matrix
        val rotation = Quaternion.mul(baseRotation, angleHandler.getRotation())
//        Log.i(TAG, "update: $rotation")
        rotation.toRotation(rotationMatrix)

        // compute camera's front direction after rotation
        cameraFront.clone(FRONT)
            .applyL(rotationMatrix)
        // compute camera's up direction after rotation
        cameraUP.clone(UPWARD)
            .applyL(rotationMatrix)
        // compute forward step & update last update time
        computeWalk()
        // recompute camera's lookAt matrix
        cameraTarget.addAssign(cameraFront, cameraPos)
        camera.lookAt(
            cameraPos.x.toDouble(), cameraPos.y.toDouble(), cameraPos.z.toDouble(),
            cameraTarget.x.toDouble(), cameraTarget.y.toDouble(), cameraTarget.z.toDouble(),
            cameraUP.x.toDouble(), cameraUP.y.toDouble(), cameraUP.z.toDouble()
        )

        rotationBuffer.rewind()
        rotation.writeToBuffer(rotationBuffer)
        cameraPos.writeToBuffer(rotationBuffer)
        property.objectData = rotationBuffer
        onUpdate?.invoke(property)
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
        baseRotation = angleHandler.getRotation()
        baseRotation.inverse()
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

    private var property: CharacterProperty = CharacterProperty.empty()
    fun onCharacterPropertyReady(property: CharacterProperty) {
        this.property = property
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

private fun matMul(m1: FloatArray, m2: FloatArray) {
    // 0 1 2
    // 3 4 5
    // 6 7 8
    val c1 = m1[0] * m2[0] + m1[1] * m2[3] + m1[2] * m2[6]
    val c2 = m1[0] * m2[1] + m1[1] * m2[4] + m1[2] * m2[7]
    val c3 = m1[0] * m2[2] + m1[1] * m2[5] + m1[2] * m2[8]

    val c4 = m1[3] * m2[0] + m1[4] * m2[3] + m1[5] * m2[6]
    val c5 = m1[3] * m2[1] + m1[4] * m2[4] + m1[5] * m2[7]
    val c6 = m1[3] * m2[2] + m1[4] * m2[5] + m1[5] * m2[8]

    val c7 = m1[6] * m2[0] + m1[7] * m2[3] + m1[8] * m2[6]
    val c8 = m1[6] * m2[1] + m1[7] * m2[4] + m1[8] * m2[7]
    val c9 = m1[6] * m2[2] + m1[7] * m2[5] + m1[8] * m2[8]

    m2[0] = c1
    m2[1] = c2
    m2[2] = c3
    m2[3] = c4
    m2[4] = c5
    m2[5] = c6
    m2[6] = c7
    m2[7] = c8
    m2[8] = c9
}