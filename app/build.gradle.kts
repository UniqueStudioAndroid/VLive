plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android")
}

android {
    compileSdkVersion(Dep.Build.COMPILE_SDK_VERSION)
    buildToolsVersion(Dep.Build.BUILD_TOOLS_VERSION)

    defaultConfig {
        applicationId = Dep.Build.APPLICATION_ID
        minSdkVersion(Dep.Build.MIN_SDK_VERSION)
        targetSdkVersion(Dep.Build.TARGET_SDK_VERSION)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        prefab = true
    }
}

dependencies {
    implementation(kotlin("stdlib", Dep.Kt.KOTLIN_VERSION))
    implementation(Dep.Kt.COROUTINE)
    implementation(Dep.AndroidX.CORE)
    implementation(Dep.AndroidX.APPCOMPAT)
    implementation(Dep.AndroidX.MATERIAL)
    implementation(Dep.AndroidX.CONSTRAINTLAYOUT)
    implementation(Dep.AndroidX.LIVEDATA)
}