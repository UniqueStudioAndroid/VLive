package com.hustunique.vlive.filament.model_object

import android.util.Log
import android.view.Display
import com.hustunique.vlive.CameraBgHelper

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 5/3/21
 */
class ScreenModelObject(private val cameraBgHelper: CameraBgHelper) : FilamentBaseModelObject("models/screen.glb") {

    companion object {
        private const val TAG = "ScreenModelObject"
    }

//    private val cameraBgHelper by lazy {
//        CameraBgHelper(filamentContext!!.engine, filamentContext!!.materialHolder.videoMaterial!!, display).apply {
//            initHelper()
//        }
//    }

    val surface by lazy { cameraBgHelper.surface }

    override val scaleBase: Int
        get() = 1

    private var screenEntity: Int = 0

    override fun onAssetSet() {
        super.onAssetSet()
        screenEntity = asset?.getFirstEntityByName("screen") ?: 0
        setMaterial()
    }

    private fun setMaterial() {
        filamentContext?.run {
            Log.i(TAG, "setMaterial: $screenEntity ${materialHolder.videoMaterial}")
            getRenderableManager().run {
                setMaterialInstanceAt(getInstance(screenEntity), 0, materialHolder.videoMaterial!!)
            }
        }

    }

    override fun update(frameTimeNanos: Long) {
        super.update(frameTimeNanos)
        cameraBgHelper.pushExternalImageToFilament()
    }
}