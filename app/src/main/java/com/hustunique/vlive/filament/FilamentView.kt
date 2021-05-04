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

    private var controller: FilamentCameraController? = null

    private val choreographer = Choreographer.getInstance()

    private val frameCallback = object : Choreographer.FrameCallback {

        fun a() {}

        override fun doFrame(frameTimeNanos: Long) {
            choreographer.postFrameCallback(this)
            controller?.update(filamentContext.camera)

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
    }

    fun bindController(filamentCameraController: FilamentCameraController) {
        this.controller = filamentCameraController.apply {
            bind(this@FilamentView)
        }
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
        filamentContext.release()
    }

}