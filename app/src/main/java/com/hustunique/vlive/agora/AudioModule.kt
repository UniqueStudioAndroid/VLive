package com.hustunique.vlive.agora

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.hustunique.resonance_audio.AudioConfig
import com.hustunique.resonance_audio.AudioRender
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentLinkedQueue

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 2021/5/17
 */
class AudioModule {

    companion object {
        private const val TAG = "AudioModule"

    }

    private val handlerThread = HandlerThread("vlive_audio_thread").apply {
        start()
    }

    private val handler = Handler(handlerThread.looper)

    private val audioRender = AudioRender()

//    private var audioPlayer: AudioPlayer? = null

    private val memberMap = HashMap<Int, Int>()

    private var numFrames = 480

    private val bufferSizePreFrame = AudioConfig.NUM_CHANNELS * 2

    @Volatile
    private var running = false

    private val dataQueue = ConcurrentLinkedQueue<ByteBuffer>()

    private val emptyQueue = ConcurrentLinkedQueue<ByteBuffer>()

    private val processRunnable = object : Runnable {
        override fun run() {
            if (dataQueue.size > 50) {
                handler.postDelayed(this, 100)
                return
            }
            val buffer =
                emptyQueue.poll() ?: ByteBuffer.allocateDirect(bufferSizePreFrame * numFrames)
            buffer.position(0)
            val res = audioRender.getOutput(buffer, numFrames)
            if (!res) {
                emptyQueue.offer(buffer)
                Log.i(TAG, "getData: $res")
            } else {
                dataQueue.offer(buffer)
            }
//            if (running) {
//                handler.postDelayed(this, if (res) 0 else 10)
//            }
        }
    }

    fun init() {
//        handler.post {
//            audioPlayer = AudioPlayer().apply {
//                startPlay()
//            }
//        }
        running = true
//        handler.post(processRunnable)
    }


    fun feedData(uid: Int, frames: Int, array: ByteArray) {
        if (numFrames != frames) {
            numFrames = frames
        }
        var sourceId = memberMap[uid]
        if (sourceId == null) {
            sourceId = audioRender.createRenderSource()
            memberMap[uid] = sourceId
        }
        audioRender.feedData(sourceId, array, frames)
    }

    fun getData(buffer: ByteArray, numFrames: Int) {
        handler.post(processRunnable)
        dataQueue.poll()?.let {
            it.position(0)
            it.get(buffer, 0, buffer.size.coerceAtMost(it.capacity()))
            emptyQueue.offer(it)
            Log.d(TAG, "getData: bfSize: ${buffer.size} dataSize: ${it.capacity()}")
        } ?: Log.i(TAG, "audio getdata null")
    }

    fun release() {
        running = false
        handlerThread.quitSafely()
        audioRender.apply {
            memberMap.forEach { (_, u) ->
                releaseSource(u)
            }
            release()
        }
//        audioPlayer?.release()
    }


}