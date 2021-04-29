plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android")
}

//apply {
//    FilamentToolsPlugin().apply(project)
//}


//tasks {
//    val copyModel by registering(Copy::class) {
//        from("models/BusterDrone/")
//        from("models/RobotExpressive.glb")
//        into("src/main/assets/models")
//    }
//
//    preBuild.dependsOn(copyModel)
//
//    named("clean") {
//        doFirst {
//            delete("src/main/assets/models")
//        }
//    }
//
//}

//extensions.configure<FilamentToolsPluginExtension> {
//    cmgenArgs = "-q --format=ktx --size=256 --extract-blur=0.1 --deploy=src/main/assets/envs/default_env"
//    iblInputFile.set(project.layout.projectDirectory.file("models/env/lightroom_14b.hdr"))
//    iblOutputDir.set(project.layout.projectDirectory.dir("src/main/assets/envs"))
//}


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
    implementation(Dep.MLKit.ML_KIT_FACE)
    implementation(Dep.Kt.COROUTINE)
    implementation(Dep.AndroidX.CORE)
    implementation(Dep.AndroidX.APPCOMPAT)
    implementation(Dep.AndroidX.MATERIAL)
    implementation(Dep.AndroidX.CONSTRAINTLAYOUT)
    implementation(Dep.AndroidX.LIVEDATA)

    implementation(Dep.CameraX.CAMERA2)
    implementation(Dep.CameraX.LIFE_CYCLE)
    implementation(Dep.CameraX.CAMERA_VIEW)

    implementation(Dep.Filament.FILAMENT)
    implementation(Dep.Filament.GLTFIO)
    implementation(Dep.Filament.UTILS)
    implementation(Dep.Filament.FILAMAT)

    implementation(Dep.Agora.AGORA)
    implementation(Dep.AndroidX.Lifecycle.COMMON_KTX)
    implementation(Dep.AndroidX.Lifecycle.LD_KTX)
    implementation(Dep.AndroidX.Lifecycle.VM_KTX)
    implementation(Dep.AndroidX.Lifecycle.RTM_KTX)

    implementation(Dep.ARCore.CORE)
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
}