package com.hustunique.vlive.util

import android.app.Activity
import android.content.Context
import android.content.Intent

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 4/27/21
 */

inline fun <reified T : Activity> Context.startActivity() {
    startActivity(Intent(this, T::class.java))
}