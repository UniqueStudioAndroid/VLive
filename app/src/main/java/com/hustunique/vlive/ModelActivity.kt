package com.hustunique.vlive

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Choreographer
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.filament.RenderableManager
import com.google.android.filament.utils.KtxLoader
import com.google.android.filament.utils.ModelViewer
import com.google.android.filament.utils.Utils
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

    private lateinit var surfaceView: SurfaceView
    private lateinit var choreographer: Choreographer
    private val frameScheduler = FrameCallback()
    private lateinit var modelViewer: ModelViewer
    private lateinit var titlebarHint: TextView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        titlebarHint = findViewById(R.id.user_hint)
        surfaceView = findViewById(R.id.main_sv)
        choreographer = Choreographer.getInstance()

        modelViewer = ModelViewer(surfaceView)

        surfaceView.setOnTouchListener { _, event ->
            modelViewer.onTouchEvent(event)
            true
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
    }

    private fun createRenderables() {
        val buffer = assets.open("models/bighead.glb").use { input ->
//            val buffer = assets.open("models/RobotExpressive.glb").use { input ->
//            val buffer = assets.open("models/scene.gltf").use { input ->
            val bytes = ByteArray(input.available())
            input.read(bytes)
            Log.i(TAG, "createRenderables: ${bytes.size}")
            ByteBuffer.wrap(bytes)
        }
        RenderableManager.PrimitiveType.POINTS

        modelViewer.loadModelGlb(buffer)
        modelViewer.transformToUnitCube()
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
        choreographer.postFrameCallback(frameScheduler)
    }

    override fun onPause() {
        super.onPause()
        choreographer.removeFrameCallback(frameScheduler)
    }

    override fun onDestroy() {
        super.onDestroy()
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
                lEyeEntity = modelViewer.asset?.getFirstEntityByName("leye") ?: 0
            }
            if (rEyeEntity == 0) {
                rEyeEntity = modelViewer.asset?.getFirstEntityByName("reye") ?: 0
            }
            if (mouthEntity == 0) {
                mouthEntity = modelViewer.asset?.getFirstEntityByName("mouth") ?: 0
            }
        }

        private fun setAnim(frameTimeNanos: Long) {
            val elapsedTimeSeconds = (frameTimeNanos - startTime).toDouble() / 1_000_000_000
            modelViewer.engine.renderableManager.setMorphWeights(
                modelViewer.engine.renderableManager.getInstance(lEyeEntity),
                floatArrayOf(abs(sin(elapsedTimeSeconds * Math.PI )).toFloat(), 0f, 0f, 0f)
            )
            modelViewer.engine.renderableManager.setMorphWeights(
                modelViewer.engine.renderableManager.getInstance(rEyeEntity),
                floatArrayOf(abs(cos(elapsedTimeSeconds * Math.PI)).toFloat(), 0f, 0f, 0f)
            )
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


            modelViewer.render(frameTimeNanos)
        }
    }
}