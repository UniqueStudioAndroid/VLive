package com.hustunique.vlive.local

import java.nio.ByteBuffer
import java.nio.FloatBuffer

// Properties for virtual characters
data class CharacterProperty(
    val lEyeOpenProbability: Float,
    val eEyeOpenProbability: Float,
    val mouthOpenWeight: Float,
    val objectMatrix: FloatBuffer,
) {

    companion object {
        fun fromArray(array: ByteArray): CharacterProperty = let {
            val bf = ByteBuffer.wrap(array)
            CharacterProperty(
                bf.float,
                bf.float,
                bf.float,
                FloatBuffer.wrap(FloatArray(16) { bf.float })
            )
        }
    }

    fun toByteArray(): ByteArray =
        ByteBuffer.allocate(19 * 4).apply {
            putFloat(lEyeOpenProbability)
            putFloat(eEyeOpenProbability)
            putFloat(mouthOpenWeight)
            objectMatrix.array().forEach {
                putFloat(it)
            }
        }.array()

}