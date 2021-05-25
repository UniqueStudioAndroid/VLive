package com.hustunique.vlive.filament

import android.content.Context
import android.util.Log
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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
        private const val MOVE_PER_SECOND = 5.0
        private const val FLYING_TIME = 2f
        private const val FLYING_MAX_HEIGHT = 5f
        private const val ROTATION_PER_CALL = Math.PI.toFloat() / 90
        private const val TAG = "FilamentCameraController"

        private val UPWARD = Vector3(y = 1f)
        private val FRONT = Vector3(z = -1f)
//        private val RIGHT = Vector3(x = 1f)
    }

    var onUpdate: ((CharacterProperty) -> Unit)? = null

    private var sensorInitialized = false
    private val angleHandler = AngleHandler(context) {
        Log.i(TAG, "First Callback from AngleHandler")
        baseRotation = it.inverse()
        sensorInitialized = true
    }.apply { start() }

    private var baseRotation = Quaternion()
    private var panRotation = Quaternion()
    private val rotationMatrix = FloatArray(9)
    private val rotationBuffer = FloatBuffer.allocate(7)

    private val cameraFront = Vector3(z = -1f)
    private val cameraUP = Vector3(y = 1f)
    private val cameraPos = Vector3()
    var position: Vector3
        get() = cameraPos.clone()
        set(value) {
            cameraPos.clone(value)
        }

    fun release() {
        angleHandler.stop()
    }

    fun bindControlView(reset: View) {
    }

    fun update(camera: Camera) {
        // calculate rotation
        val rotation = computeRotation()
        // compute camera's front direction after rotation
        cameraFront.clone(FRONT)
            .applyL(rotationMatrix)
        // compute camera's up direction after rotation
        cameraUP.clone(UPWARD)
            .applyL(rotationMatrix)
        // compute forward step & update last update time
        computePos()
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

    private fun computeRotation(): Quaternion {
        if (!sensorInitialized) return Quaternion()
        val q = if (useSensor) {
            baseRotation * angleHandler.getRotation()
        } else {
            panRotation * baseRotation
        }
        q.toRotation(rotationMatrix)
        return q
    }

    private var lastUpdateTime = 0L
    private fun computePos() {
        val now = System.currentTimeMillis()
        val deltaTime = (now - lastUpdateTime) / 1000f

        val animator = flyingAnimator
        if (animator != null) {
            cameraPos.clone(animator.update(deltaTime))
            if (animator.over()) {
                flyingAnimator = null
            }
        } else if (lastUpdateTime > 0 && isSelected) {
            val temp = cameraFront.clone()
            temp.y = 0f
            temp.normalized()
            cameraPos += temp * (deltaTime * MOVE_PER_SECOND).toFloat()
        }

        lastUpdateTime = now
    }

    private var flyingAnimator: FlyingAnimator? = null
    private fun flyTo(v: Vector3): Boolean {
        if ((v - cameraPos).norm() <= 2) {
            Log.i(TAG, "flyTo: too near, no need to fly")
            return false
        }
        val delta = v - cameraPos
        flyingAnimator = FlyingAnimator(
            cameraPos.clone(),
            v - delta.normalized(),
            FLYING_TIME,
            FLYING_MAX_HEIGHT,
        )
        return true
    }

    private fun onRotationEvent(angle: Float, progress: Float) {
        if (useSensor) return
        // TODO: recompute rotation matrix
        val rotAngle = angle + PI.toFloat() / 2
        val u = Vector3(cos(rotAngle), sin(rotAngle), 0f)

        val theta = progress * ROTATION_PER_CALL
        val q = Quaternion(u * sin(theta/2), cos(theta/2))
        panRotation = q * panRotation
    }

    private var useSensor = true
    private fun enableSensor(enable: Boolean) {
        if (useSensor == enable) return
        useSensor = enable
        if (enable) {
            baseRotation = panRotation * baseRotation * angleHandler.getRotation().inverse()
        } else {
            baseRotation *= angleHandler.getRotation()
        }
    }

    fun onEvent(type: Int) {
        when (type) {
            0 -> onRotationEvent(0f, 1f)
            1 -> enableSensor(false)
        }
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
