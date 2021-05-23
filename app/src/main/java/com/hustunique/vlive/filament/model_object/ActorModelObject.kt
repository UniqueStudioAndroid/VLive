package com.hustunique.vlive.filament.model_object

import com.google.android.filament.TransformManager

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 2021/5/19
 */
class ActorModelObject : FilamentBaseModelObject("models/actor.glb") {
    private var rootInstance: Int = 0
    private var transformManager: TransformManager? = null

    override fun onAssetSet() {
        asset?.let {
            val tm = filamentContext!!.getTransformManager()
            rootInstance = tm.getInstance(it.root)
            transformManager = tm
        }
    }

    override fun update(frameTimeNanos: Long) {
        transformManager?.let { tm ->
            property?.let {
                tm.setTransform(rootInstance, it.objectData.array())
            }
        }
    }
}