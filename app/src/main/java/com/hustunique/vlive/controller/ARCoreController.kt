package com.hustunique.vlive.controller

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import com.google.ar.core.*
import com.hustunique.vlive.data.CameraTextureProvider
import com.hustunique.vlive.data.ObjectMatrixProvider
import com.hustunique.vlive.opengl.LocalFrameManager
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 4/28/21
 */
class ARCoreController(
    context: Context,
    private val localFrameManager: LocalFrameManager
) : Runnable, CameraTextureProvider, ObjectMatrixProvider {

    private lateinit var session: Session
    private val glHandler = localFrameManager.getHandler()

    private val objectMatrixData = FloatArray(16)
    private val objectMatrix = Matrix()

    private var cameraTexture: Int = 0


    private val buffer = ByteBuffer.allocateDirect(
        640 * 480 * 4
    ).order(ByteOrder.nativeOrder())
        .position(0)

    init {
        post {
            cameraTexture = localFrameManager.getOesTexture()

            configSession(context)
        }
    }

    private fun configSession(context: Context) {
        session = Session(context, EnumSet.noneOf(Session.Feature::class.java))
        val cameraConfigFilter = CameraConfigFilter(session)
        cameraConfigFilter.facingDirection = CameraConfig.FacingDirection.FRONT
        val cameraConfigs = session.getSupportedCameraConfigs(cameraConfigFilter)
        if (cameraConfigs.isNotEmpty()) {
            session.cameraConfig = cameraConfigs[0]
        } else {
            throw Exception("Camera Not support")
        }

        val config = Config(session)
        config.augmentedFaceMode = Config.AugmentedFaceMode.MESH3D
        session.configure(config)

        session.setCameraTextureName(cameraTexture)
        Log.i(TAG, "configSession: $cameraTexture")
    }

    fun release() = post {
        session.close()
        glHandler.removeCallbacks(this)
    }

    fun resume() = post {
        Log.i(TAG, "resume: ")
        session.resume()
        glHandler.post(this)
    }

    fun pause() = post {
        session.pause()
        glHandler.removeCallbacks(this)
    }

    private val bitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888)
    private val rotateMatrix = Matrix().apply { postRotate(270f) }

    override fun run() {
        glHandler.post(this)
        session.setCameraTextureName(cameraTexture)
        session.update()
        session.getAllTrackables(AugmentedFace::class.java)
            .firstOrNull { it.trackingState == TrackingState.TRACKING }
            ?.getRegionPose(AugmentedFace.RegionType.NOSE_TIP)
            ?.toMatrix(objectMatrixData, 0)
        objectMatrixData[0] = objectMatrixData[0] * -1
        objectMatrixData[4] = objectMatrixData[4] * -1
        objectMatrixData[8] = objectMatrixData[8] * -1
        objectMatrixData[12] = objectMatrixData[12] * -1
        objectMatrix.setValues(objectMatrixData)
        localFrameManager.refreshImageReader()
    }


    override fun getCameraTextureId() = cameraTexture

    override fun getObjectMatrix() = objectMatrix

    private fun post(action: () -> Unit) = glHandler.post {
        action()
    }

    companion object {
        private const val TAG = "SurfaceTextureHelper"

    }
}