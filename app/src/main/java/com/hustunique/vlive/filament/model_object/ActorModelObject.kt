package com.hustunique.vlive.filament.model_object

import com.google.android.filament.TransformManager
import com.hustunique.vlive.data.MathUtil
import com.hustunique.vlive.data.Quaternion
import com.hustunique.vlive.data.Vector3

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 2021/5/19
 */
open class ActorModelObject(path: String = "models/actor.glb") : FilamentBaseModelObject(path) {
    private var rootInstance: Int = 0
    private var transformManager: TransformManager? = null
    private val transformMatrix = FloatArray(16)

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
                val buffer = it.objectData
                buffer.rewind()
                val q = Quaternion.readFromBuffer(buffer)
                val pos = Vector3.readFromBuffer(buffer)
                MathUtil.packRotationAndPosT(q, pos, transformMatrix)
                tm.setTransform(rootInstance, transformMatrix)
            }
        }
    }
}