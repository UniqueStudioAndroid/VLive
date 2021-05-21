package com.hustunique.vlive.local

import java.nio.ByteBuffer
import java.nio.FloatBuffer

// Properties for virtual characters
data class CharacterProperty(
    val lEyeOpenProbability: Float,
    val eEyeOpenProbability: Float,
    val mouthOpenWeight: Float,
    val faceMatrix: FloatBuffer,
    var objectMatrix: FloatBuffer,
) {

    companion object {
        fun fromArray(array: ByteArray): CharacterProperty = let {
            val bf = ByteBuffer.wrap(array)
            CharacterProperty(
                bf.float,
                bf.float,
                bf.float,
                FloatBuffer.wrap(FloatArray(16) { bf.float }),
                FloatBuffer.wrap(FloatArray(16) { bf.float })
            )
        }

        fun empty(): CharacterProperty {
            return CharacterProperty(
                0f,
                0f,
                0f,
                FloatBuffer.allocate(16),
                FloatBuffer.allocate(16),
            )
        }
    }

    fun toByteArray(): ByteArray =
        ByteBuffer.allocate(35 * 4).apply {
            putFloat(lEyeOpenProbability)
            putFloat(eEyeOpenProbability)
            putFloat(mouthOpenWeight)
            faceMatrix.array().forEach {
                putFloat(it)
            }
            objectMatrix.array().forEach {
                putFloat(it)
            }
        }.array()
}