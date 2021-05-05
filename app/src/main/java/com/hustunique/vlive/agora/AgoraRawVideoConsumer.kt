package com.hustunique.vlive.agora

import io.agora.rtc.mediaio.IVideoSink
import java.nio.ByteBuffer

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 5/5/21
 */
class AgoraRawVideoConsumer : IVideoSink{
    override fun consumeByteBufferFrame(
        buffer: ByteBuffer?,
        format: Int,
        width: Int,
        height: Int,
        rotation: Int,
        timestamp: Long
    ) {

    }

    override fun consumeByteArrayFrame(
        data: ByteArray?,
        format: Int,
        width: Int,
        height: Int,
        rotation: Int,
        timestamp: Long
    ) {
    }

    override fun consumeTextureFrame(
        textureId: Int,
        format: Int,
        width: Int,
        height: Int,
        rotation: Int,
        timestamp: Long,
        matrix: FloatArray?
    ) {
    }

    override fun onInitialize(): Boolean {
        TODO("Not yet implemented")
    }

    override fun onStart(): Boolean {
        TODO("Not yet implemented")
    }

    override fun onStop() {
        TODO("Not yet implemented")
    }

    override fun onDispose() {
        TODO("Not yet implemented")
    }

    override fun getEGLContextHandle(): Long {
        TODO("Not yet implemented")
    }

    override fun getBufferType(): Int {
        TODO("Not yet implemented")
    }

    override fun getPixelFormat(): Int {
        TODO("Not yet implemented")
    }
}