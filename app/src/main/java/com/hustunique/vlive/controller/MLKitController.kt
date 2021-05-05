package com.hustunique.vlive.controller

import android.graphics.Bitmap
import android.media.Image
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.hustunique.vlive.data.FacePropertyProvider

class MLKitController : FacePropertyProvider {

    private val detectOptions = FaceDetectorOptions.Builder()
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setMinFaceSize(10f)
        .build()
    private val detector = FaceDetection.getClient(detectOptions)
    private var mouthOpenWeight = 0f
    private var lEyeOpenWeight = 0f
    private var rEyeOpenWeight = 0f

    fun stop() {
        detector.close()
    }

    private fun onProcess(faces: List<Face>) {
        Log.i(TAG, "onProcess: ")
        faces.firstOrNull()?.let {
            lEyeOpenWeight = it.leftEyeOpenProbability ?: 0f
            rEyeOpenWeight = it.rightEyeOpenProbability ?: 0f

            val contourUpperPoints =
                it.getContour(FaceContour.UPPER_LIP_BOTTOM)?.points ?: emptyList()
            val contourLowerPoints = it.getContour(FaceContour.LOWER_LIP_TOP)?.points ?: emptyList()
            val upperY = contourUpperPoints.sumByDouble { point -> point.y.toDouble() }
            val lowerY = contourLowerPoints.sumByDouble { point -> point.y.toDouble() }
            mouthOpenWeight = ((upperY - lowerY) / 20f)
                .coerceIn(0.0, 1.0)
                .toFloat()
        }
    }

    fun process(bitmap: Bitmap) {
        Log.i(TAG, "process: ")
        detector.process(
            InputImage.fromBitmap(
                bitmap,
                0
            )
        )
            .addOnSuccessListener { results -> onProcess(results) }
            .addOnFailureListener { e -> e.printStackTrace() }
//            .addOnCompleteListener { image.close() }
    }

    fun process(image: Image) {
        Log.i(TAG, "process: ")
        detector.process(
            InputImage.fromMediaImage(
                image,
                0
            )
        )
            .addOnSuccessListener { results -> onProcess(results) }
            .addOnFailureListener { e -> e.printStackTrace() }
            .addOnCompleteListener { image.close() }
    }

    override fun getLEyeOpenWeight() = lEyeOpenWeight

    override fun getREyeOpenWeight() = rEyeOpenWeight

    override fun getMouthOpenWeight() = mouthOpenWeight

    companion object {
        private const val TAG = "MLKitHelper"
    }
}