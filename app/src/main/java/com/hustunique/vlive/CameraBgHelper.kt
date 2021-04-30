package com.hustunique.vlive

import android.graphics.ImageFormat
import android.hardware.HardwareBuffer
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.util.Size
import android.view.Display
import android.view.Surface
import androidx.annotation.RequiresApi
import com.google.android.filament.*

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 4/30/21
 */
class CameraBgHelper(
    private val filamentEngine: Engine,
    private val filamentMaterial: MaterialInstance,
    private val display: Display
) {

    companion object {
        private const val kLogTag = "CameraHelper"
        private const val kImageReaderMaxImages = 7
    }

    private var resolution = Size(640, 480)
    private var filamentTexture: Texture? = null
    private var filamentStream: Stream? = null

    @RequiresApi(Build.VERSION_CODES.Q)
    private val imageReader = ImageReader.newInstance(
        resolution.width,
        resolution.height,
        ImageFormat.PRIVATE,
        kImageReaderMaxImages,
        HardwareBuffer.USAGE_GPU_SAMPLED_IMAGE
    )

    val surface: Surface
        get() = imageReader.surface


    fun initHelper() {
        filamentStream = Stream.Builder().build(filamentEngine)
        filamentTexture = Texture.Builder()
            .sampler(Texture.Sampler.SAMPLER_EXTERNAL)
            .format(Texture.InternalFormat.RGB8)
            .build(filamentEngine)

        val sampler = TextureSampler(
            TextureSampler.MinFilter.LINEAR,
            TextureSampler.MagFilter.LINEAR,
            TextureSampler.WrapMode.CLAMP_TO_EDGE
        )
        filamentTexture!!.setExternalStream(filamentEngine, filamentStream!!)
        filamentMaterial.setParameter("videoTexture", filamentTexture!!, sampler)
    }

    fun pushExternalImageToFilament() {
        val stream = filamentStream
        if (stream != null) {
//            imageReader.acquireLatestImage()?.also {
//                stream.setAcquiredImage(it.hardwareBuffer, Handler()) { it.close() }
//            }
        }
    }
}