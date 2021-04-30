package com.hustunique.vlive

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.media.ImageReader
import android.os.Bundle
import android.util.Log
import android.view.Choreographer
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.filament.RenderableManager
import com.google.android.filament.utils.KtxLoader
import com.google.android.filament.utils.Utils
import com.hustunique.vlive.agora.AgoraModule
import com.hustunique.vlive.agora.BufferSource
import com.hustunique.vlive.agora.ARCoreHelper
import com.hustunique.vlive.databinding.ActivityModelBinding
import com.hustunique.vlive.filament.ModelViewer
import java.nio.ByteBuffer
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


class ModelActivity : AppCompatActivity() {

    companion object {
        init {
            Utils.init()
        }

        private const val TAG = "MainActivity"
    }

    private lateinit var choreographer: Choreographer
    private val frameScheduler = FrameCallback()
    private val binding by lazy { ActivityModelBinding.inflate(layoutInflater) }

    private lateinit var modelViewer: ModelViewer

    //    private lateinit var videoSource: IVideoSource
    private lateinit var videoSource: BufferSource
    private lateinit var agoraModule: AgoraModule
    private var bm = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    private var reader: ImageReader? = null

    private lateinit var arCoreHelper: ARCoreHelper

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        choreographer = Choreographer.getInstance()

        modelViewer = ModelViewer(binding.mainSv)

        binding.mainSv.setOnTouchListener { _, event ->
            modelViewer.onTouchEvent(event)
            true
        }

        binding.mainSv.post {
            reader = ImageReader.newInstance(
                binding.mainSv.width,
                binding.mainSv.height,
                PixelFormat.RGBA_8888,
                2
            )
            modelViewer.addRenderTarget(reader!!.surface)
//            val mask =  0xf.inv()
//            videoSource =
//                FilamentTextureSource((binding.mainSv.width and mask), (binding.mainSv.height and mask), modelViewer)
////            FilamentTextureSource(360, 640, modelViewer)
            videoSource = BufferSource()
            agoraModule = AgoraModule(this).apply {
                val view = initAgora(videoSource)
                binding.frameContainer.addView(view)
            }
            reader!!.setOnImageAvailableListener({
                val i = it.acquireNextImage()
                val pixelStride: Int = i.planes[0].pixelStride
                val rowStride: Int = i.planes[0].rowStride
                val rowPadding: Int = rowStride - pixelStride * i.width
                val buffer = i.planes[0].buffer
                val w = i.width + rowPadding / pixelStride
//                bm = Bitmap.createBitmap(
//                    i.width + rowPadding / pixelStride, i.height,
//                    Bitmap.Config.ARGB_8888
//                )
//                bm.copyPixelsFromBuffer(buffer)
                Log.i(TAG, "onFrame: $w, ${i.height}")
                videoSource.onBuffered(buffer, w, i.height)

                i.close()
            }, null)
        }

        createRenderables()
        createIndirectLight()

        val dynamicResolutionOptions = modelViewer.view.dynamicResolutionOptions
        dynamicResolutionOptions.enabled = true
        modelViewer.view.dynamicResolutionOptions = dynamicResolutionOptions

        val ssaoOptions = modelViewer.view.ambientOcclusionOptions
        ssaoOptions.enabled = true
        modelViewer.view.ambientOcclusionOptions = ssaoOptions

        val bloomOptions = modelViewer.view.bloomOptions
        bloomOptions.enabled = true
        modelViewer.view.bloomOptions = bloomOptions

        arCoreHelper = ARCoreHelper(this)
    }

    private fun createRenderables() {
        val buffer = assets.open("models/nfface.glb").use { input ->
//            val buffer = assets.open("models/RobotExpressive.glb").use { input ->
//            val buffer = assets.open("models/scene.gltf").use { input ->
            val bytes = ByteArray(input.available())
            input.read(bytes)
            Log.i(TAG, "createRenderables: ${bytes.size}")
            ByteBuffer.wrap(bytes)
        }
        RenderableManager.PrimitiveType.POINTS

        modelViewer.loadModelGlb(buffer)
//        modelViewer.transformToUnitCube()
    }

    private fun createIndirectLight() {
        val engine = modelViewer.engine

        val scene = modelViewer.scene
        val ibl = "default_env"
        readCompressedAsset("envs/$ibl/${ibl}_ibl.ktx").let {
            scene.indirectLight = KtxLoader.createIndirectLight(engine, it)
            scene.indirectLight!!.intensity = 30_000.0f
        }
        readCompressedAsset("envs/$ibl/${ibl}_skybox.ktx").let {
            scene.skybox = KtxLoader.createSkybox(engine, it)
        }
    }

    private fun readCompressedAsset(assetName: String): ByteBuffer {
        Log.i(TAG, "readCompressedAsset: $assetName")
        val input = assets.open(assetName)
        val bytes = ByteArray(input.available())
        input.read(bytes)
        return ByteBuffer.wrap(bytes)
    }

    override fun onResume() {
        super.onResume()
        arCoreHelper.resume()
        choreographer.postFrameCallback(frameScheduler)
    }

    override fun onPause() {
        super.onPause()
        arCoreHelper.pause()
        choreographer.removeFrameCallback(frameScheduler)
    }

    override fun onDestroy() {
        super.onDestroy()
        arCoreHelper.release()
        choreographer.removeFrameCallback(frameScheduler)
        modelViewer.destroyModel()
    }

    var lEyeEntity: Int = 0
    var rEyeEntity: Int = 0
    var mouthEntity: Int = 0

    inner class FrameCallback : Choreographer.FrameCallback {
        private val startTime = System.nanoTime()

        private fun checkEntity() {

            if (lEyeEntity == 0) {
                lEyeEntity =
                    modelViewer.asset?.getFirstEntityByName("HyperNURBS_6C4dObjectSymmetry_6Polygon")
                        ?: 0
            }
//            if (rEyeEntity == 0) {
//                rEyeEntity = modelViewer.asset?.getFirstEntityByName("reye") ?: 0
//            }
            if (mouthEntity == 0) {
                mouthEntity = modelViewer.asset?.getFirstEntityByName("HyperNURBS") ?: 0
            }
        }

        private fun setAnim(frameTimeNanos: Long) {
            val elapsedTimeSeconds = (frameTimeNanos - startTime).toDouble() / 1_000_000_000
            modelViewer.engine.renderableManager.setMorphWeights(
                modelViewer.engine.renderableManager.getInstance(lEyeEntity),
                floatArrayOf(
                    abs(sin(elapsedTimeSeconds * Math.PI)).toFloat(),
                    abs(cos(elapsedTimeSeconds * Math.PI)).toFloat(),
                    0f,
                    0f
                )
            )
//            modelViewer.engine.renderableManager.setMorphWeights(
//                modelViewer.engine.renderableManager.getInstance(rEyeEntity),
//                floatArrayOf(abs(cos(elapsedTimeSeconds * Math.PI)).toFloat(), 0f, 0f, 0f)
//            )
            modelViewer.engine.renderableManager.setMorphWeights(
                modelViewer.engine.renderableManager.getInstance(mouthEntity),
                floatArrayOf(abs(sin(elapsedTimeSeconds * Math.PI)).toFloat(), 0f, 0f, 0f)
            )
        }

        override fun doFrame(frameTimeNanos: Long) {
            choreographer.postFrameCallback(this)

            checkEntity()

            modelViewer.animator?.apply {
                val elapsedTimeSeconds = (frameTimeNanos - startTime).toDouble() / 1_000_000_000
                if (animationCount > 0) {
                    for (i in 0 until animationCount) {
                        applyAnimation(i, elapsedTimeSeconds.toFloat())
                        Log.i(TAG, "doFrame: ${getAnimationName(i)}")
                    }
                }
                updateBoneMatrices()
            }
            setAnim(frameTimeNanos)

//            val entity = modelViewer.asset?.root ?: 0
//            if (entity != 0) {
//                val instance = modelViewer.engine.transformManager.getInstance(entity)
//                modelViewer.engine.transformManager.setTransform(instance, arCoreHelper.objectMatrix)
//            }
//            modelViewer.camera.setModelMatrix(arCoreHelper.viewMatrix)
//            modelViewer.camera.setCustomProjection(arCoreHelper.projectionMatrix, 0.1, 100.0)

            modelViewer.render(frameTimeNanos)
        }
    }
}

fun FloatArray.toMString(): String = fold("Matrix: ") {
    R, d ->
    "$R $d, "
}