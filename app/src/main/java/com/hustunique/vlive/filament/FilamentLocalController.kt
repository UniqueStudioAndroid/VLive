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
        val rotation = baseRotation * angleHandler.getRotation()
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
        val cameraTarget = cameraFront + cameraPos
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
            MoveDirType.FORWARD -> cameraPos += cameraFront * delta
            MoveDirType.BACK -> cameraPos -= cameraFront * delta
            MoveDirType.LEFT -> cameraPos -= cameraFront * cameraUP * delta
            MoveDirType.RIGHT -> cameraPos += cameraFront * cameraUP * delta
            MoveDirType.UP -> cameraPos += cameraUP * delta
            MoveDirType.DOWN -> cameraPos -= cameraUP * delta
            MoveDirType.PLANE_FORWARD -> {
                val temp = cameraFront.clone()
                temp.y = 0f
                temp.normalize()
                cameraPos += temp * delta
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
