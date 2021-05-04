package com.hustunique.vlive.filament

import com.google.android.filament.Colors
import com.google.android.filament.Engine
import com.google.android.filament.Material
import com.google.android.filament.MaterialInstance
import java.nio.Buffer

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 5/3/21
 */
class MaterialHolder(private val engine: Engine) {

    var videoMaterial: MaterialInstance? = null


    fun loadMaterial(buffer: Buffer) {
        val material = Material.Builder().payload(buffer, buffer.remaining()).build(engine)
        val materialInstance = material.createInstance()
        materialInstance.setParameter("baseColor", Colors.RgbType.SRGB, 1.0f, 0.85f, 0.57f)
        materialInstance.setParameter("roughness", 0.3f)
        videoMaterial = materialInstance
    }
}