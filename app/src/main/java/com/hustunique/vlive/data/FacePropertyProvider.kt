package com.hustunique.vlive.data

interface FacePropertyProvider {
    fun getLEyeOpenWeight() : Float
    fun getREyeOpenWeight() : Float
    fun getMouthOpenWeight() : Float
}