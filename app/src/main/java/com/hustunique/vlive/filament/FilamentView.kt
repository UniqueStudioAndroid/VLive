package com.hustunique.vlive.filament

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Choreographer
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.hustunique.vlive.filament.model_object.FilamentBaseModelObject
import java.nio.ByteBuffer
import java.nio.channels.Channels

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 5/2/21
 */
class FilamentView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), LifecycleObserver {

    companion object {
        private const val TAG = "FilamentView"
    }

    val filamentContext = FilamentContext(this)

    val modelObjectList = mutableListOf<FilamentBaseModelObject>()

    private var controller: FilamentCameraController? = null

    private val choreographer = Choreographer.getInstance()

    private val frameCallback = object : Choreographer.FrameCallback {

        fun a() {}

        override fun doFrame(frameTimeNanos: Long) {
            choreographer.postFrameCallback(this)
            controller?.update(filamentContext.camera)
            modelObjectList.forEach { it.update(frameTimeNanos) }

            filamentContext.render(frameTimeNanos)
        }

    }

    init {
        (context as? LifecycleOwner)?.lifecycle?.addObserver(this)
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {

            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                controller?.resize(width, height)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
            }

        })
        filamentContext.apply {
            val ibl = "default_env"
            setIndirectLight(readCompressedAsset("envs/$ibl/${ibl}_ibl.ktx"))
            setSkyBox(readCompressedAsset("envs/$ibl/${ibl}_skybox.ktx"))

            materialHolder.loadMaterial(readUncompressedAsset("materials/lit.filamat"))
        }
    }

    fun bindController(filamentCameraController: FilamentCameraController) {
        this.controller = filamentCameraController.apply {
            bind(this@FilamentView)
        }
    }

    fun addModelObject(obj: FilamentBaseModelObject) {
        obj.run {
            bindToContext(filamentContext)
            asset = filamentContext.loadGlb(readCompressedAsset(resourcePath))
        }
        modelObjectList.add(obj)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            controller?.onTouchEvent(it)
        }
        return true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        choreographer.postFrameCallback(frameCallback)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        choreographer.removeFrameCallback(frameCallback)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        Log.i(TAG, "onDestroy: ")
        controller?.release()
        choreographer.removeFrameCallback(frameCallback)
        modelObjectList.forEach { it.destroy() }
        filamentContext.release()
    }


    fun readUncompressedAsset(@Suppress("SameParameterValue") assetName: String): ByteBuffer =
        context.assets.openFd(assetName).use { fd ->
            val input = fd.createInputStream()
            val dst = ByteBuffer.allocate(fd.length.toInt())

            val src = Channels.newChannel(input)
            src.read(dst)
            src.close()

            return dst.apply { rewind() }
        }


    fun readCompressedAsset(assetName: String): ByteBuffer =
        context.assets.open(assetName).use { input ->
            val bytes = ByteArray(input.available())
            input.read(bytes)
            ByteBuffer.wrap(bytes)
        }
}