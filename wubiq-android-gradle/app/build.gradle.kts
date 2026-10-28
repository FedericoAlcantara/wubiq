plugins {
    id("com.android.application")
}

android {
    namespace = "net.sf.wubiq.android"
    compileSdk = 37

    defaultConfig {
        applicationId = "net.sf.wubiq.android"
        minSdk = 28
        targetSdk = 37
        versionCode = 73
        versionName = "2.7.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildToolsVersion = "37.0.0"
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.8.0")
    implementation("com.google.android.material:material:1.14.0")
    implementation(files("libs/wubiq-client.jar"))
    implementation(files("libs/stario-5.20.0.aar"))
    implementation(files("libs/starioextension-5.20.0.aar"))
    implementation(files("libs/StarIODeviceSetting-5.20.0.aar"))
    implementation(files("libs/ZSDK_API.jar"))

    // Worker
    val workVersion = "2.12.0"
    // (Java only)
    implementation("androidx.work:work-runtime:$workVersion")
    // Kotlin + coroutines
    implementation("androidx.work:work-runtime-ktx:$workVersion")

    // test dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}