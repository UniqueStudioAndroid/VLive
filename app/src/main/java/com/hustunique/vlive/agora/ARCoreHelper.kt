package com.hustunique.vlive.agora

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import com.google.ar.core.*
import com.hustunique.vlive.util.ShaderUtil
import io.agora.rtc.gl.EglBase
import io.agora.rtc.gl.GlUtil
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
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

    private lateinit var session: Session

    val objectMatrix = FloatArray(16)

    private var cameraTexture: Int = 0
    private var cameraProgram: Int = -1
    private var cameraPositionAttrib: Int = -1
    private var cameraTexCoordAttrib: Int = -1
    private var cameraTextureUniform: Int = -1

    init {
        post {
            eglBase = EglBase.create()
            if (surface == null) {
                eglBase.createDummyPbufferSurface()
            } else {
                eglBase.createSurface(surface)
            }
            eglBase.makeCurrent()
            cameraTexture = GlUtil.generateTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES)
            compileAndLoadShaderProgram(context)

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
        session.setCameraTextureName(cameraTexture)
        session.update()
        session.getAllTrackables(AugmentedFace::class.java)
            .firstOrNull { it.trackingState == TrackingState.TRACKING }
            ?.getRegionPose(AugmentedFace.RegionType.NOSE_TIP)
            ?.toMatrix(objectMatrix, 0)
        objectMatrix[0] = objectMatrix[0] * -1
        objectMatrix[4] = objectMatrix[4] * -1
        objectMatrix[8] = objectMatrix[8] * -1
        objectMatrix[12] = objectMatrix[12] * -1

        renderCamera()
    }

    private fun renderCamera() {
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthMask(false)

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTexture)
        GLES20.glUseProgram(cameraProgram)
        GLES20.glUniform1i(cameraTextureUniform, 0)

        QUAD_COORDS.position(0)
        QUAD_TEX_COORDS.position(0)
        // Set the vertex positions and texture coordinates.
        GLES20.glVertexAttribPointer(
            cameraPositionAttrib,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            0,
            QUAD_COORDS
        )
        GLES20.glVertexAttribPointer(
            cameraTexCoordAttrib,
            TEXCOORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            0,
            QUAD_TEX_COORDS
        )
        GLES20.glEnableVertexAttribArray(cameraPositionAttrib)
        GLES20.glEnableVertexAttribArray(cameraTexCoordAttrib)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        // Restore the depth state for further drawing.
        GLES20.glDepthMask(true)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        ShaderUtil.checkGLError(TAG, "RenderCamera")
        eglBase.swapBuffers()
    }

    @Throws(IOException::class)
    private fun compileAndLoadShaderProgram(context: Context) {
        val defineValuesMap: Map<String, Int> = TreeMap()
        val vertexShader: Int = ShaderUtil.loadGLShader(
            TAG,
            context,
            GLES20.GL_VERTEX_SHADER,
            VERTEX_SHADER_NAME
        )
        val fragmentShader: Int = ShaderUtil.loadGLShader(
            TAG,
            context,
            GLES20.GL_FRAGMENT_SHADER,
            FRAGMENT_SHADER_NAME,
            defineValuesMap
        )
        cameraProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(cameraProgram, vertexShader)
        GLES20.glAttachShader(cameraProgram, fragmentShader)
        GLES20.glLinkProgram(cameraProgram)
        GLES20.glUseProgram(cameraProgram)
        ShaderUtil.checkGLError(TAG, "Program creation")

        cameraPositionAttrib = GLES20.glGetAttribLocation(cameraProgram, "a_Position")
        cameraTexCoordAttrib = GLES20.glGetAttribLocation(cameraProgram, "a_TexCoord")
        ShaderUtil.checkGLError(TAG, "Program creation")

        cameraTextureUniform = GLES20.glGetUniformLocation(cameraProgram, "sTexture")
        ShaderUtil.checkGLError(TAG, "Program parameters")
    }

    private fun post(action: () -> Unit) = handler.post {
        action()
    }

    companion object {
        private const val TAG = "SurfaceTextureHelper"
        private const val VERTEX_SHADER_NAME = "shaders/common.vert"
        private const val FRAGMENT_SHADER_NAME = "shaders/common.frag"
        private const val COORDS_PER_VERTEX = 2
        private const val TEXCOORDS_PER_VERTEX = 2
        private const val FLOAT_SIZE = 4
        private val QUAD_COORDS = ByteBuffer
            .allocateDirect(8 * FLOAT_SIZE)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(
                floatArrayOf(
                    -1.0f, -1.0f,
                    +1.0f, -1.0f,
                    -1.0f, +1.0f,
                    +1.0f, +1.0f
                )
            )
        private val QUAD_TEX_COORDS = ByteBuffer
            .allocateDirect(8 * FLOAT_SIZE)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(
                floatArrayOf(
                    0f, 1f,
                    1f, 1f,
                    0f, 0f,
                    1f, 0f
                )
            )
    }
}