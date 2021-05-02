package com.hustunique.vlive.filament

import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.MaterialProvider
import com.google.android.filament.gltfio.ResourceLoader
import java.nio.Buffer

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 5/2/21
 */
class FilamentResourceHolder(
    engine: Engine,
    private val onEntityReady: (IntArray) -> Unit,
    private val onEntityRemoved: (IntArray) -> Unit
) {

    var normalizeSkinningWeights = true
    var recomputeBoundingBoxes = false

    private val readyRenderables = IntArray(128) // add up to 128 entities at a time

    private val assetLoader = AssetLoader(engine, MaterialProvider(engine), EntityManager.get())
    private val resourceLoader =
        ResourceLoader(engine, normalizeSkinningWeights, recomputeBoundingBoxes)

    private val assetList = mutableListOf<FilamentAsset>()

    fun update() {
        resourceLoader.asyncUpdateLoad()
        populateScene()
    }

    fun loadResource(buffer: Buffer): Boolean = assetLoader.createAssetFromBinary(buffer)?.run {
        resourceLoader.asyncBeginLoad(this)
        assetList.add(this)
        releaseSourceData()
    } != null


    private fun populateScene() {
        var count = 0
        val popRenderables = {
            count = 0
            assetList.forEach {
                count = it.popRenderables(readyRenderables)
                if (count != 0) {
                    return@forEach
                }
            }
            count != 0
        }
        while (popRenderables()) {
            onEntityReady(readyRenderables.take(count).toIntArray())
        }
//        onEntityReady(asset.lightEntities)
    }

    fun destroy() {
        resourceLoader.run {
            asyncCancelLoad()
            evictResourceData()
            destroy()
        }
        assetLoader.run {
            assetList.forEach {
                onEntityRemoved(it.entities)
                destroyAsset(it)
            }
            destroy()
        }
    }

}