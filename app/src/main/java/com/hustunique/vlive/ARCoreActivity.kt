package com.hustunique.vlive

import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.ar.core.CameraConfig
import com.google.ar.core.CameraConfigFilter
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.hustunique.vlive.databinding.ActivityArCoreBinding
import com.hustunique.vlive.databinding.ActivityMainBinding
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ARCoreActivity : AppCompatActivity(), GLSurfaceView.Renderer {
    private lateinit var binding: ActivityArCoreBinding
    private lateinit var session: Session
    private lateinit var surface: GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityArCoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        surface = binding.arSurface
        configSurface()

        session = Session(this, EnumSet.noneOf(Session.Feature::class.java))
        sessionConfigure()
    }

    override fun onDestroy() {
        super.onDestroy()
        session.close()
    }

    override fun onResume() {
        super.onResume()

        session.resume()
        surface.onResume()
    }

    override fun onPause() {
        super.onPause()

        session.pause()
        surface.onPause()
    }

    private fun configSurface() {
        surface.preserveEGLContextOnPause = true
        surface.setEGLContextClientVersion(2)
        surface.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        surface.setRenderer(this)
        surface.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        surface.setWillNotDraw(false)
    }

    private fun sessionConfigure()  {
        val cameraConfigFilter = CameraConfigFilter(session)
        cameraConfigFilter.facingDirection = CameraConfig.FacingDirection.FRONT
        val cameraConfigs = session.getSupportedCameraConfigs(cameraConfigFilter)
        if (cameraConfigs.isNotEmpty()) {
            session.cameraConfig = cameraConfigs[0]
        } else {
            throw Exception("Camera Not support")
        }

        val config = Config(session)
        config.augmentedFaceMode = Config.AugmentedFaceMode.MESH3D
        session.configure(config)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        TODO("Not yet implemented")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        TODO("Not yet implemented")
    }

    override fun onDrawFrame(gl: GL10?) {
        TODO("Not yet implemented")
    }
}