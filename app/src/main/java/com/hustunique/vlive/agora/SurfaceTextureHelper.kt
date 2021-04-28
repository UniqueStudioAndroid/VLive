package com.hustunique.vlive.agora

import android.graphics.SurfaceTexture
import android.opengl.GLES10
import android.os.Handler
import android.os.HandlerThread
import io.agora.rtc.gl.EglBase
import io.agora.rtc.gl.GlUtil

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 4/28/21
 */
class SurfaceTextureHelper(width: Int, height: Int, onInit: (SurfaceTexture) -> Unit) {

    private lateinit var eglBase: EglBase
    private val handlerThread = HandlerThread("agora_helper").apply {
        start()
    }
    private val handler = Handler(handlerThread.looper)

    private var texture: Int = 0

    var onFrame: (SurfaceTexture, Int) -> Unit = { _, _ -> }

    lateinit var surfaceTexture: SurfaceTexture

    init {
        post {
            eglBase = EglBase.create()
            eglBase.createDummyPbufferSurface()
            eglBase.makeCurrent()
            texture = GlUtil.generateTexture(GLES10.GL_TEXTURE_2D)
            GLES10.glTexImage2D(
                GLES10.GL_TEXTURE_2D,
                0,
                GLES10.GL_RGBA,
                width,
                height,
                0,
                GLES10.GL_RGBA,
                GLES10.GL_UNSIGNED_BYTE,
                null
            )
            surfaceTexture = SurfaceTexture(texture).apply {
                setOnFrameAvailableListener({
                    onFrame(it, texture)
                }, handler)
            }
            onInit(surfaceTexture)
        }
    }

    fun release() {
        post {
            surfaceTexture.release()
            eglBase.release()
            handlerThread.quitSafely()
        }
    }

    private fun post(action: () -> Unit) {
        handler.post {
            action()
        }
    }

    fun update() {
        post {
            surfaceTexture.updateTexImage()
        }
    }


}