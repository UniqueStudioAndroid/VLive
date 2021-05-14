package com.hustunique.vlive.local

import java.nio.FloatBuffer

// Properties for virtual characters
data class CharacterProperty(
    val lEyeOpenProbability: Float,
    val eEyeOpenProbability: Float,
    val mouthOpenWeight: Float,
    val objectMatrix: FloatBuffer,
)