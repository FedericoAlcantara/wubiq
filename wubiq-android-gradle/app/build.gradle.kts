plugins {
    id("com.android.application")
}

android {
    namespace = "net.sf.wubiq.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "net.sf.wubiq.android"
        minSdk = 22
        targetSdk = 34
        versionCode = 52
        versionName = "2.4.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildToolsVersion = "34.0.0"
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation(files("libs/wubiq-client.jar"))
    implementation(files("libs/jsoup-1.9.1.jar"))
    implementation(files("libs/starioextension.jar"))
    implementation(files("libs/StarIOPort3.1.jar"))
    implementation(files("libs/ZSDK_API.jar"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}