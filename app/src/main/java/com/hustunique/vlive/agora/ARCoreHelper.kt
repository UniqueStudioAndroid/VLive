package com.hustunique.vlive.agora

import android.content.Context
import android.opengl.GLES11Ext
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import com.google.ar.core.*
import io.agora.rtc.gl.EglBase
import io.agora.rtc.gl.GlUtil
import java.util.*

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 4/28/21
 */
class ARCoreHelper(
    context: Context,
    surface: Surface? = null
) : Runnable {

    private lateinit var eglBase: EglBase
    private val handlerThread = HandlerThread("ar_core_helper").apply {
        start()
    }
    private val handler = Handler(handlerThread.looper)

    private var texture: Int = 0
    private lateinit var session: Session

    val viewMatrix = FloatArray(16)
    private val _projectionMatrix = FloatArray(16)
    val projectionMatrix
        get() = _projectionMatrix.map { it.toDouble() }.toDoubleArray()
    val objectMatrix = FloatArray(16)

    init {
        post {
            eglBase = EglBase.create()
            if (surface == null) {
                eglBase.createDummyPbufferSurface()
            } else {
                eglBase.createSurface(surface)
            }
            eglBase.makeCurrent()
            texture = GlUtil.generateTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES)
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

        session.setCameraTextureName(texture)
        Log.i(TAG, "configSession: $texture")
    }

    fun release() = post {
        session.close()
        eglBase.release()
        handler.removeCallbacks(this)
        handlerThread.quitSafely()
    }

    fun resume() = post {
        Log.i(TAG, "resume: ")
        session.resume()
        handler.post(this)
    }

    fun pause() = post {
        session.pause()
        handler.removeCallbacks(this)
    }

    override fun run() {
        handler.post(this)
        session.setCameraTextureName(texture)
        val frame = session.update()
        val camera = frame.camera
        camera.getViewMatrix(viewMatrix, 0)
        camera.getProjectionMatrix(_projectionMatrix, 0, 0.01f, 100f)
        session.getAllTrackables(AugmentedFace::class.java)
            .firstOrNull { it.trackingState == TrackingState.TRACKING }
            ?.getRegionPose(AugmentedFace.RegionType.NOSE_TIP)
            ?.toMatrix(objectMatrix, 0)
//        apply(0.01f, objectMatrix)

        render()
    }

    private fun render() {

    }

    private fun apply(factor: Float, array: FloatArray) {
        array[0] *= factor
        array[5] *= factor
        array[10] *= factor
    }

    private fun post(action: () -> Unit) = handler.post {
        action()
    }

    companion object {
        private const val TAG = "SurfaceTextureHelper"
    }
}