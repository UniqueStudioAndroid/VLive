package com.hustunique.vlive.filament.model_object

import android.util.Log
import androidx.annotation.CallSuper
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.utils.*
import com.hustunique.vlive.filament.FilamentCameraController
import com.hustunique.vlive.filament.FilamentContext
import com.hustunique.vlive.filament.FilamentModelObjectController

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 5/3/21
 */
abstract class FilamentBaseModelObject(val resourcePath: String) {

    companion object {
        private const val TAG = "FilamentBaseModelObject"
    }

    protected val controllerList = mutableListOf<FilamentModelObjectController>()

    protected abstract val scaleBase: Int

    var asset: FilamentAsset? = null
        set(value) {
            field = value
            if (value != null) {
                onAssetSet()
            }
        }

    protected var filamentContext: FilamentContext? = null

    protected open fun onAssetSet() {
        transformToCube()
    }


    fun addController(controller: FilamentModelObjectController) {
        controllerList.add(controller)
    }

    fun removeController(controller: FilamentModelObjectController) {
        controllerList.remove(controller)
    }

    fun bindToContext(context: FilamentContext) {
        filamentContext = context
    }

    @CallSuper
    open fun update(frameTimeNanos: Long) {

    }

    fun destroy() {
        asset = null
        filamentContext = null
    }

    private fun transformToCube(centerPoint: Float3 = FilamentCameraController.kDefaultObjectPosition) {
        if (filamentContext == null) {
            throw IllegalStateException("do not attached to a engine")
        }
        asset?.let { asset ->
            val tm = filamentContext!!.getTransformManager()
            var center = asset.boundingBox.center.let { v -> Float3(v[0], v[1], v[2]) }
            val halfExtent = asset.boundingBox.halfExtent.let { v -> Float3(v[0], v[1], v[2]) }
            Log.i(
                TAG,
                "transformToCube: ${halfExtent.x} ${halfExtent.y} ${halfExtent.z} $resourcePath"
            )
            Log.i(TAG, "transformToCube: ${center.x} ${center.y} ${center.z} $resourcePath")
            val maxExtent = 2.0f * max(halfExtent)
            val scaleFactor = 2.0f / maxExtent * scaleBase
            center -= centerPoint / scaleFactor
            val transform = scale(Float3(scaleFactor)) * translation(-center)
            tm.setTransform(tm.getInstance(asset.root), transpose(transform).toFloatArray())
        }
    }
}