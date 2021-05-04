package com.hustunique.vlive.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventCallback
import android.hardware.SensorManager
import android.util.Log
import com.hustunique.vlive.data.Quaternion
import com.hustunique.vlive.toMString

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 3/26/21
 */
class AngleHandler(
    context: Context
) : SensorEventCallback() {

    companion object {
        private const val TAG = "AngelHandler"
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val rotationVector = FloatArray(4)

    fun start() {
        sensorManager?.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        Log.i(TAG, "start: Listening sensor event")
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
        Log.i(TAG, "start: handler stopped now")
    }

    fun getRotationMatrix(matrix: FloatArray) {
        SensorManager.getRotationMatrixFromVector(matrix, rotationVector)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                event.values.copyInto(rotationVector, 0, 0, 4)
            }
        }
    }
}