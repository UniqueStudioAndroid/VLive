package com.hustunique.sles

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 2021/5/16
 */
class SLESNative {
    companion object {
        init {
            System.loadLibrary("sles-lib")
        }
    }

    external fun test()
}