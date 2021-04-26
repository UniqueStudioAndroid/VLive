
object Dep {

    object Build {
        const val APPLICATION_ID = "com.hustunique.vlive"
        const val MIN_SDK_VERSION = 28
        const val TARGET_SDK_VERSION = 30
        const val COMPILE_SDK_VERSION = 30
        const val BUILD_TOOLS_VERSION = "30.0.3"
        const val ANDROID_TOOLS = "com.android.tools.build:gradle:4.1.3"
    }

    object Kt {
        const val KOTLIN_VERSION = "1.4.31"
        const val COROUTINE = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.2"
    }

    object Test {
        const val JUNIT = "junit:junit:4.+"
        const val EXT_JUNIT = "androidx.test.ext:junit:1.1.2"
        const val ESPRESSO = "androidx.test.espresso:espresso-core:3.3.0"
    }

    object AndroidX {
        const val CORE = "org.jetbrains.kotlin:kotlin-stdlib:1.4.31"
        const val APPCOMPAT = "androidx.appcompat:appcompat:1.2.0"
        const val MATERIAL = "com.google.android.material:material:1.3.0"
        const val CONSTRAINTLAYOUT = "androidx.constraintlayout:constraintlayout:2.0.4"
        const val LIVEDATA = "androidx.lifecycle:lifecycle-livedata:2.3.0"
    }

}