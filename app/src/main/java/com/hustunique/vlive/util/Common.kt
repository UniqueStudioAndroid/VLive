package com.hustunique.vlive.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import java.nio.ByteBuffer
import java.nio.channels.Channels

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 4/27/21
 */

inline fun <reified T : Activity> Context.startActivity() {
    startActivity(Intent(this, T::class.java))
}

fun Activity.readUncompressedAsset(@Suppress("SameParameterValue") assetName: String): ByteBuffer =
    assets.openFd(assetName).use { fd ->
        val input = fd.createInputStream()
        val dst = ByteBuffer.allocate(fd.length.toInt())

        val src = Channels.newChannel(input)
        src.read(dst)
        src.close()

        return dst.apply { rewind() }
    }


fun Activity.readCompressedAsset(assetName: String): ByteBuffer =
    assets.open(assetName).use { input ->
        val bytes = ByteArray(input.available())
        input.read(bytes)
        ByteBuffer.wrap(bytes)
    }
